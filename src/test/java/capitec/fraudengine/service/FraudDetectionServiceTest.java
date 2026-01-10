
package capitec.fraudengine.service;

import capitec.fraudengine.model.TransactionEntity;
import capitec.fraudengine.repository.TransactionRepository;
import capitec.fraudengine.service.rules.FraudRule;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FraudDetectionServiceTest {

    @Test
    void flagsAndSavesWhenAnyRuleMatches() {
        TransactionRepository repo = mock(TransactionRepository.class);

        // Anonymous class that implements BOTH abstract methods in FraudRule
        FraudRule ruleFalse = new FraudRule() {
            @Override
            public boolean isFraudulent(TransactionEntity tx) {
                return false;
            }

            @Override
            public String getName() {
                return "FALSE_RULE";
            }
        };
        FraudRule ruleTrue = new FraudRule() {
            @Override
            public boolean isFraudulent(TransactionEntity tx) {
                return true;
            }

            @Override
            public String getName() {
                return "TRUE_RULE";
            }
        };

        // IMPORTANT: List<FraudRule>, not List<Object>
        FraudDetectionService service = new FraudDetectionService(repo, List.of(ruleFalse, ruleTrue));

        TransactionEntity tx = TransactionEntity.builder()
                .pan("4111111111111111")
                .amount(new BigDecimal("1500"))
                .currency("ZAR")
                .location("UNKNOWN")
                .build();

        service.process(tx);

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(repo).save(captor.capture());
        TransactionEntity saved = captor.getValue();
        assertTrue(saved.isFlagged());
        assertEquals("05", saved.getResponseCode()); // decline when flagged
    }

    @Test
    void approvesWhenNoRulesMatch() {
        TransactionRepository repo = mock(TransactionRepository.class);

        FraudRule ruleAlwaysFalse = new FraudRule() {
            @Override
            public boolean isFraudulent(TransactionEntity tx) {
                return false;
            }

            @Override
            public String getName() {
                return "FALSE_RULE";
            }
        };

        FraudDetectionService service = new FraudDetectionService(repo, List.of(ruleAlwaysFalse));

        TransactionEntity tx = TransactionEntity.builder()
                .pan("4111111111111111")
                .amount(new BigDecimal("10"))
                .currency("ZAR")
                .location("SOUTH-AFRICA")
                .build();

        service.process(tx);

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(repo).save(captor.capture());
        TransactionEntity saved = captor.getValue();
        assertFalse(saved.isFlagged());
        assertEquals("00", saved.getResponseCode()); // approved
    }
}
