
package capitec.fraudengine.iso;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISODate;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.iso.channel.ASCIIChannel;

public class IsoTestClient {
    public static void main(String[] args) throws Exception {
        String host = System.getProperty("iso.host", "localhost");
        int port = Integer.getInteger("iso.port", 8037);

        ISO87APackager packager = new ISO87APackager(); // MUST match server packager
        ASCIIChannel channel = new ASCIIChannel(host, port, packager);

        channel.connect();

        ISOMsg m = new ISOMsg();
        m.setMTI("0800"); // network management request (echo test)
        m.set(7, ISODate.formatDate(new java.util.Date(), "MMddHHmmss")); // DE7 (UTC)
        m.set(11, "000001"); // STAN
        m.set(70, "001"); // echo test code

        channel.send(m); // ASCIIChannel adds ASCII length prefix automatically
        ISOMsg resp = channel.receive();

        System.out.println("Response MTI: " + resp.getMTI());
        System.out.println("DE39 (Response Code): " + resp.getString(39));

        channel.disconnect();
    }
}
