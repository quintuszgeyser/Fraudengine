
package capitec.fraudengine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Starts the full app, hits REST endpoints, persists to Postgres (via
 * Testcontainers).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FraudFlowIT {

    @LocalServerPort
    int port;
    @Autowired
    TestRestTemplate rest;

    @Test
    void postTransaction_thenGetFraudFlags() {
        String json = """
                {
                  "pan":"4111111111111111",
                  "amount":1500,
                  "currency":"ZAR",
                  "location":"UNKNOWN",
                  "category":"POS",
                  "timestamp":"2026-01-10T09:00:00Z"
                }
                """;

        ResponseEntity<String> create = rest.postForEntity("http://localhost:" + port + "/api/transactions",
                new HttpEntity<>(json, new HttpHeaders() {
                    {
                        setContentType(MediaType.APPLICATION_JSON);
                    }
                }),
                String.class);

        assertThat(create.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.CREATED);

        ResponseEntity<String> flags = rest.getForEntity("http://localhost:" + port + "/api/fraud-flags", String.class);
        assertThat(flags.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(flags.getBody()).contains("\"pan\":\"4111111111111111\"");
    }
}
