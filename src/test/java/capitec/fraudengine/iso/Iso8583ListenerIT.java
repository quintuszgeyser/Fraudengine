
package capitec.fraudengine.iso;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Iso8583ListenerIT {

    static int TEST_PORT = 30000 + (int) (Math.random() * 10000);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("iso8583.listener.enabled", () -> "true");
        r.add("iso8583.port", () -> TEST_PORT);
    }

    @Test
    void listenerAcceptsTcpConnection() {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            try (Socket s = new Socket("127.0.0.1", TEST_PORT)) {
                assertTrue(s.isConnected());
            }
        });
    }
}
