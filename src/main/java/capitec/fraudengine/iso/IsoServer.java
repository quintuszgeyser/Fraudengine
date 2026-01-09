
package capitec.fraudengine.iso;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.channel.PostChannel;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class IsoServer implements Runnable {

    private final int port;
    private final ISOPackager packager;
    private final IsoMessageHandler handler;
    private volatile boolean running = false;

    private ServerSocket serverSocket;
    private Thread serverThread;

    public IsoServer(int port, ISOPackager packager, IsoMessageHandler handler) {
        this.port = port;
        this.packager = packager;
        this.handler = handler;
    }

    /** Spring will call this as initMethod */
    public void start() {
        if (serverThread != null && serverThread.isAlive())
            return;
        running = true;
        serverThread = new Thread(this, "iso8583-server");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    /** Spring will call this as destroyMethod */
    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException ignored) {
        }
        System.out.println("ISO Server shutdown requested");
    }

    @Override
    public void run() {

        Logger channelLogger = new Logger();
        channelLogger.addListener(new SimpleLogListener(System.out));

        // Packager logger (field-by-field activity)
        if (packager instanceof ISOBasePackager) {
            ISOBasePackager p = (ISOBasePackager) packager;
            Logger packagerLogger = new Logger();
            packagerLogger.addListener(new SimpleLogListener(System.out));
            p.setLogger(packagerLogger, "packager");
        }

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("ISO Server listening on port " + port + " (BINARY framing via PostChannel)");

            while (running) {
                PostChannel channel = new PostChannel(packager); // server-side constructor
                channel.setLogger(channelLogger, "iso-channel");
                channel.setTimeout(0); // keep connection alive (no read timeout)
                channel.accept(serverSocket); // blocks until a client connects

                System.out.println("Client connected: " + channel.getSocket().getRemoteSocketAddress());
                new Thread(() -> handleClient(channel), "iso-client").start();
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("ISO Server failed: " + e.getMessage());
            } else {
                System.out.println("ISO Server shutting down.");
            }
        }
    }

    private void handleClient(PostChannel channel) {
        // Run the raw tracer once for THIS connection
        boolean traceOnceForThisConnection = true;

        try {
            while (running && channel.isConnected()) {
                try {
                    // >>> One-shot diagnostic run: read raw frame, trace, then ALSO process &
                    // respond
                    if (traceOnceForThisConnection && packager != null) {
                        ISOMsg traced = receiveOneAndTraceRawAndReturnMsg(channel, packager); // << NEW
                        traceOnceForThisConnection = false; // disable after first use

                        // Process traced first message and send a response
                        printIsoDebug(traced);
                        ISOMsg resp = handler.handle(traced);
                        channel.send(resp);
                        continue;
                    }

                    // >>> Normal flow for subsequent messages
                    ISOMsg req = channel.receive(); // framed receive (2-byte MSB/LSB)
                    printIsoDebug(req);
                    ISOMsg resp = handler.handle(req);
                    channel.send(resp);

                } catch (ISOException e) {
                    String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
                    if (msg.contains("timeout")) {
                        System.out.println("ISO receive timeout on " +
                                channel.getSocket().getRemoteSocketAddress() +
                                " — keeping connection alive.");
                        continue;
                    }
                    System.err.println("ISO parse/send error: " + e.getMessage() +
                            " from " + channel.getSocket().getRemoteSocketAddress());

                    int failingField = parseFailingField(e.getMessage());
                    if (failingField > 0) {
                        System.err.println(">> jPOS reports failure while unpacking field " + failingField +
                                " . This typically means a field *before* " + failingField +
                                " is mis-typed or has the wrong length, so the parser slid.");
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Client IO closed: " + e.getMessage());
        } finally {
            try {
                channel.disconnect();
            } catch (IOException ignored) {
            }
            System.out.println("Client handler finished");
        }
    }

    // ---------------------------
    // Debug & diagnostic helpers
    // ---------------------------

    /**
     * Print MTI, set bits, and all parsed fields with values and lengths (for
     * LLVARs).
     */
    private static void printIsoDebug(ISOMsg m) throws ISOException {
        System.out.println("---- ISO DEBUG DUMP ----");
        System.out.println("MTI: " + m.getMTI());

        // Presence list (which fields jPOS says are set)
        List<Integer> setBits = new ArrayList<>();
        for (int f = 1; f <= m.getMaxField(); f++) {
            if (m.hasField(f))
                setBits.add(f);
        }
        System.out.println("Set bits: " + setBits);

        // Values + lengths (helps spot LLVAR length mismatches)
        for (int f = 1; f <= m.getMaxField(); f++) {
            if (!m.hasField(f))
                continue;
            String v;
            try {
                v = m.getString(f);
            } catch (Exception e) {
                v = "<binary>";
            }
            int len = (v == null) ? 0 : v.length();
            System.out.printf("F%-3d: %s (len=%d)%n", f, safePrintable(v), len);
        }

        // Highlight key fields we care about
        highlight(m, 18, "Merchant Category Code");
        highlight(m, 22, "POS Entry Mode");
        highlight(m, 25, "POS Condition Code");
        highlight(m, 32, "Acquiring Institution ID");
        highlight(m, 33, "Forwarding Institution ID");
        highlight(m, 35, "Track 2");
        highlight(m, 41, "Terminal ID");
        highlight(m, 42, "Merchant ID");
        highlight(m, 43, "Merchant Name/Location");
        highlight(m, 49, "Currency Code");
        System.out.println("---- END ISO DEBUG DUMP ----");
    }

    private static void highlight(ISOMsg m, int f, String name) {
        if (m.hasField(f)) {
            String v;
            try {
                v = m.getString(f);
            } catch (Exception e) {
                v = "<binary>";
            }
            System.out.printf(">> F%-3d (%s): %s%n", f, name, safePrintable(v));
        } else {
            System.out.printf(">> F%-3d (%s): <not present>%n", f, name);
        }
    }

    private static String safePrintable(String v) {
        if (v == null)
            return "<null>";
        StringBuilder sb = new StringBuilder();
        for (char c : v.toCharArray()) {
            if (c >= 32 && c <= 126)
                sb.append(c); // printable ASCII
            else
                sb.append(String.format("\\x%02X", (int) c));
        }
        return sb.toString();
    }

    /** Parse "unpacking field=X" out of ISOException#getMessage() if present. */
    private static int parseFailingField(String msg) {
        if (msg == null)
            return -1;
        // Message format example: "... unpacking field=32, consumed=57"
        try {
            String needle = "unpacking field=";
            int p = msg.indexOf(needle);
            if (p >= 0) {
                int start = p + needle.length();
                int end = start;
                while (end < msg.length() && Character.isDigit(msg.charAt(end)))
                    end++;
                return Integer.parseInt(msg.substring(start, end));
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    // --- One-shot raw trace helper that ALSO returns the unpacked ISOMsg ---
    private ISOMsg receiveOneAndTraceRawAndReturnMsg(PostChannel channel, ISOPackager p) throws IOException {
        // Read 2-byte MSB/LSB length from the socket (PostChannel framing)
        var in = channel.getSocket().getInputStream();
        int b1 = in.read();
        int b2 = in.read();
        if (b1 < 0 || b2 < 0)
            throw new IOException("EOF reading frame length");
        int len = ((b1 & 0xFF) << 8) | (b2 & 0xFF);

        byte[] raw = in.readNBytes(len);
        System.out.println("Raw frame length=" + len);

        // Hex preview of the first 64 bytes
        int preview = Math.min(64, raw.length);
        StringBuilder sb = new StringBuilder("Raw[0.." + (preview - 1) + "]: ");
        for (int i = 0; i < preview; i++)
            sb.append(String.format("%02X", raw[i]));
        System.out.println(sb);

        // Packager-driven trace: create ISOMsg and let the packager unpack it
        System.out.println("---- TRACE UNPACK (packager-driven) ----");
        try {
            ISOMsg m = new ISOMsg();
            m.setPackager(p); // use whichever packager you're testing
            m.unpack(raw); // jPOS unpacks the whole message

            System.out.println("MTI: " + m.getMTI());
            List<Integer> fields = new ArrayList<>();
            for (int f = 1; f <= m.getMaxField(); f++)
                if (m.hasField(f))
                    fields.add(f);
            System.out.println("Set bits: " + fields);

            for (int f : fields) {
                String v;
                try {
                    v = m.getString(f);
                } catch (Exception e) {
                    v = "<binary>";
                }
                int lenF = (v == null) ? 0 : v.length();
                System.out.printf("DE%-3d: %s (len=%d)%n", f, v, lenF);
            }

            System.out.println("---- END TRACE UNPACK ----");
            return m;
        } catch (ISOException e) {
            System.err.println("FAILED: " + e.getMessage());
            throw new IOException("Packager failed: " + e.getMessage(), e);
        }
    }
}
