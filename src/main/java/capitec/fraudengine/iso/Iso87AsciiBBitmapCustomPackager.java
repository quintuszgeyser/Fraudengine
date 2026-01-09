
package capitec.fraudengine.iso;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOException;
import org.jpos.iso.packager.ISO87APackagerBBitmap;
import org.jpos.iso.IFA_NUMERIC;
import org.jpos.iso.IFA_LLNUM;
import org.jpos.iso.IFA_LLCHAR;
import org.jpos.iso.IF_CHAR;
import org.jpos.iso.ISOUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ISO-8583:1987 ASCII packager with BINARY bitmap.
 * Matches payloads with ASCII-encoded fields and an 8/16-byte binary bitmap.
 */
public class Iso87AsciiBBitmapCustomPackager extends ISO87APackagerBBitmap {

    public Iso87AsciiBBitmapCustomPackager() {
        super();

        // BEFORE 32 — ASCII fixed numerics
        fld[18] = new IFA_NUMERIC(4, "Merchant Category Code"); // N4 (ASCII)
        fld[22] = new IFA_NUMERIC(3, "POS Entry Mode"); // N3 (ASCII)
        fld[25] = new IFA_NUMERIC(2, "POS Condition Code"); // N2 (ASCII)

        // DE32: LLVAR numeric (ASCII length digits)
        fld[32] = new IFA_LLNUM(11, "Acquiring Institution ID");

        // DE33: LLVAR alphanumeric (ASCII)
        fld[33] = new IFA_LLCHAR(11, "Forwarding Institution ID");

        // DE35: Track 2 — LLVAR numeric (ASCII); '=' remains literal
        fld[35] = new IFA_LLNUM(37, "Track 2 Data");

        // Fixed alphanumerics (ASCII)
        fld[41] = new IF_CHAR(8, "Terminal ID"); // AN8
        fld[42] = new IF_CHAR(15, "Card Acceptor ID (MID)"); // AN15
        fld[43] = new IF_CHAR(40, "Card Acceptor Name/Location"); // AN40

        // Currency code (ASCII numeric)
        fld[49] = new IFA_NUMERIC(3, "Currency Code, Txn");
    }

    /**
     * unpack with per-DE trace.
     * Prints MTI, bitmap presence, each DE value/consumed bytes; stops on first
     * failure.
     */
    public void traceUnpack(byte[] raw) {
        try {
            int offset = 0;
            // MTI (ASCII 4)
            if (raw.length < 4)
                throw new ISOException("Raw too short for MTI");
            String mti = new String(raw, offset, 4, StandardCharsets.US_ASCII);
            System.out.println("---- TRACE UNPACK ----");
            System.out.println("MTI: " + mti);
            offset += 4;

            // Primary bitmap (8 bytes binary)
            if (raw.length < offset + 8)
                throw new ISOException("Raw too short for primary bitmap");
            byte[] p = new byte[8];
            System.arraycopy(raw, offset, p, 0, 8);
            offset += 8;
            boolean hasSecondary = (p[0] & 0x80) != 0;

            // Secondary bitmap (if bit 1 set)
            byte[] s = null;
            if (hasSecondary) {
                if (raw.length < offset + 8)
                    throw new ISOException("Raw too short for secondary bitmap");
                s = new byte[8];
                System.arraycopy(raw, offset, s, 0, 8);
                offset += 8;
            }
            System.out.println("Bitmap: primary=" + ISOUtil.hexString(p) +
                    ", secondary=" + (hasSecondary ? ISOUtil.hexString(s) : "none"));

            // Build presence list from bitmap(s)
            List<Integer> present = new ArrayList<>();
            fillBits(present, p, 1); // bits 1..64
            if (hasSecondary)
                fillBits(present, s, 65); // bits 65..128

            // Show presence
            System.out.println("Set bits: " + present);

            // Unpack each present field in order; skip bit 1 (bitmap)
            ISOMsg m = new ISOMsg();
            m.setMTI(mti);

            for (int f = 2; f <= 128; f++) {
                if (!present.contains(f))
                    continue;
                if (fld[f] == null) {
                    System.out.printf("DE%-3d: <no field packager>%n", f);
                    continue;
                }
                try {
                    int consumed = fld[f].unpack(m, raw, offset);
                    String val = m.hasField(f) ? m.getString(f) : null;
                    System.out.printf("DE%-3d: %s (consumed=%d, offset=%d->%d)%n",
                            f, safePrintable(val), consumed, offset, offset + consumed);
                    offset += consumed;
                } catch (ISOException ex) {
                    System.err.printf("FAILED at DE%-3d: %s (offset=%d)%n", f, ex.getMessage(), offset);
                    break;
                }
            }
            System.out.println("---- END TRACE UNPACK ----");
        } catch (Exception ex) {
            System.err.println("TRACE UNPACK error: " + ex.getMessage());
        }
    }

    private static void fillBits(List<Integer> out, byte[] bitmap, int base) {
        for (int i = 0; i < 64; i++) {
            int byteIndex = i / 8;
            int bitInByte = i % 8;
            boolean set = (bitmap[byteIndex] & (0x80 >> bitInByte)) != 0;
            if (set)
                out.add(base + i);
        }
    }

    private static String safePrintable(String v) {
        if (v == null)
            return "<null>";
        StringBuilder sb = new StringBuilder();
        for (char c : v.toCharArray()) {
            if (c >= 32 && c <= 126)
                sb.append(c);
            else
                sb.append(String.format("\\x%02X", (int) c));
        }
        return sb.toString();
    }
}