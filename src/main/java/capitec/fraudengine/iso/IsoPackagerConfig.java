
// src/main/java/capitec/fraudengine/iso/IsoPackagerConfig.java
package capitec.fraudengine.iso;

import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.packager.ISO87APackagerBBitmap; // ASCII fields + BINARY bitmap
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

public final class IsoPackagerConfig {
    private IsoPackagerConfig() {
    }

    public static ISOBasePackager defaultPackager() {
        ISO87APackagerBBitmap packager = new ISO87APackagerBBitmap();

        // Attach a packager logger so jPOS prints field-by-field activity
        Logger plogger = new Logger();
        plogger.addListener(new SimpleLogListener(System.out));
        packager.setLogger(plogger, "packager");

        return packager;
    }
}