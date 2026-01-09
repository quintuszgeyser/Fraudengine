
package capitec.fraudengine.iso;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOException;

import java.util.ArrayList;
import java.util.List;

public final class DebugIsoPrinter {
    private DebugIsoPrinter() {
    }

    /**
     * Print MTI, set bits, and all parsed fields with values and lengths (for
     * LLVARs).
     */
    public static void printParsed(ISOMsg m) throws ISOException {
        System.out.println("---- ISO DEBUG DUMP ----");
        System.out.println("MTI: " + m.getMTI());

        // Collect set fields (as jPOS reports them)
        List<Integer> setBits = new ArrayList<>();
        for (int i = 1; i <= m.getMaxField(); i++) {
            if (m.hasField(i))
                setBits.add(i);
        }
        System.out.println("Set bits: " + setBits);

        // Print each field value (string form) and length for LLVARs
        for (int i = 1; i <= m.getMaxField(); i++) {
            if (!m.hasField(i))
                continue;
            String val;
            try {
                val = m.getString(i);
            } catch (Exception e) {
                val = "<non-string or binary>";
            }
            String lenInfo = (val != null) ? " (len=" + val.length() + ")" : "";
            System.out.printf("F%-3d: %s%s%n", i, safePrintable(val), lenInfo);
        }

        // Highlight some key fields
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
                sb.append(c);
            else
                sb.append(String.format("\\x%02X", (int) c));
        }
        return sb.toString();
    }
}
