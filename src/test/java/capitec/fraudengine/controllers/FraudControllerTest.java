
package capitec.fraudengine.controllers;

import capitec.fraudengine.model.TransactionEntity;
import capitec.fraudengine.service.FraudDetectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// If you want to migrate later: import org.springframework.test.context.mockito.MockitoBean;
// For now, MockBean is fine (deprecated but still works on Boot 3.4.x).
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FraudController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters if you use Spring Security
class FraudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postTransactionReturnsProcessedEntity() throws Exception {
        Mockito.when(fraudDetectionService.process(Mockito.any()))
               .thenAnswer(invocation -> {
                   TransactionEntity tx = invocation.getArgument(0);
                   tx.setId(1L);
                   tx.setFlagged(true);
                   tx.setResponseCode("05");
                   return tx;
               });

        TransactionEntity request = TransactionEntity.builder()
                .pan("4111111111111111")
                .amount(new BigDecimal("1500"))
                .currency("ZAR")
                .location("UNKNOWN")
                .category("POS")
                .timestamp(OffsetDateTime.parse("2026-01-10T09:00:00Z")) // ✅ OffsetDateTime
                .build();

        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.flagged").value(true))
               .andExpect(jsonPath("$.responseCode").value("05"));
    }

    @Test
    void getFraudFlagsReturnsList() throws Exception {
        Mockito.when(fraudDetectionService.getFlagged())
               .thenReturn(List.of(
                   TransactionEntity.builder()
                       .id(2L)
                       .pan("4111111111111111")
                       .amount(new BigDecimal("1500"))
                       .currency("ZAR")
                       .location("UNKNOWN")
                       .category("POS")
                       .flagged(true)
                       .responseCode("05")
                       .timestamp(OffsetDateTime.parse("2026-01-10T09:00:00Z")) // ✅ OffsetDateTime
                       .build()
               ));

        mockMvc.perform(get("/api/fraud-flags"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$[0].pan").value("4111111111111111"))
               .andExpect(jsonPath("$[0].flagged").value(true));
    }
}
