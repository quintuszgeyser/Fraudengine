
package capitec.fraudengine.controllers;

import capitec.fraudengine.model.TransactionEntity;
import capitec.fraudengine.service.FraudDetectionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests the REST controller only (web slice), with a Mockito mock of the
 * service.
 */
@WebMvcTest(FraudController.class)
class FraudControllerTest {

    @Autowired
    MockMvc mvc;

	@MockBean
	FraudDetectionService fraudService;

    @Test
    void postTransactionReturnsProcessedEntity() throws Exception {
        // Stub: service returns a flagged entity
        Mockito.when(fraudService.process(Mockito.any())).thenAnswer(inv -> {
            TransactionEntity tx = inv.getArgument(0);
            tx.setId(1L);
            tx.setFlagged(true);
            tx.setResponseCode("05");
            return tx;
        });

        String body = """
                {

                  "pan": "4111111111111111",
                  "amount": 1500,
                  "currency": "ZAR",
                  "location": "UNKNOWN",
                  "category": "POS",
                  "timestamp": "2026-01-10T09:00:00Z"
                }
                """;

        mvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.flagged").value(true))
                .andExpect(jsonPath("$.responseCode").value("05"));
    }

    @Test
    void getFraudFlagsReturnsList() throws Exception {
        Mockito.when(fraudService.getFlagged()).thenReturn(List.of(
                TransactionEntity.builder()
                        .id(2L)
                        .pan("4111111111111111")
                        .amount(new BigDecimal("1500"))
                        .currency("ZAR")
                        .location("UNKNOWN")
                        .flagged(true)
                        .build()));

        mvc.perform(get("/api/fraud-flags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pan").value("4111111111111111"))
                .andExpect(jsonPath("$[0].flagged").value(true));
    }

}
