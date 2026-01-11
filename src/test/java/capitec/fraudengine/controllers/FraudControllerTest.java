package capitec.fraudengine.controllers;

import capitec.fraudengine.model.Transaction;
import capitec.fraudengine.service.FraudDetectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FraudController.class)  // Only loads your controller, not the full context
class FraudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void getFraudFlagsReturnsList() throws Exception {
        String pan = "4111111111111111";
        when(fraudDetectionService.getFraudFlags(pan))
                .thenReturn(Arrays.asList("HIGH_AMOUNT", "SUSPICIOUS_LOCATION"));

        mockMvc.perform(get("/api/fraud/flags")
                        .param("pan", pan))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("HIGH_AMOUNT"))
                .andExpect(jsonPath("$[1]").value("SUSPICIOUS_LOCATION"));

        verify(fraudDetectionService, times(1)).getFraudFlags(pan);
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void postTransactionReturnsProcessedEntity() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setPan("4111111111111111");
        transaction.setAmount(1500.0);

        Transaction processed = new Transaction();
        processed.setPan("4111111111111111");
        processed.setAmount(1500.0);
        processed.setFlagged(true);

        when(fraudDetectionService.processTransaction(any(Transaction.class)))
                .thenReturn(processed);

        mockMvc.perform(post("/api/fraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flagged").value(true))
                .andExpect(jsonPath("$.pan").value("4111111111111111"));

        verify(fraudDetectionService, times(1)).processTransaction(any(Transaction.class));
    }
}
