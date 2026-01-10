
package capitec.fraudengine.service.rules;

import capitec.fraudengine.TestUtil;
import capitec.fraudengine.model.TransactionEntity;
import capitec.fraudengine.repository.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VelocityRuleTest {

    @Test
    void flagsWhenRecentCountMeetsOrExceedsMax() {
        TransactionRepository repo = mock(TransactionRepository.class);
        VelocityRule rule = new VelocityRule(repo);
        TestUtil.inject(rule, "windowMinutes", 5);
        TestUtil.inject(rule, "maxCount", 3);

        TransactionEntity tx = TransactionEntity.builder()
                .pan("4111111111111111")
                .timestamp(OffsetDateTime.now())
                .build();

        when(repo.countByPanAndTimestampBetween(anyString(), any(), any())).thenReturn(3L);
        assertTrue(rule.isFraudulent(tx));

        when(repo.countByPanAndTimestampBetween(anyString(), any(), any())).thenReturn(2L);
        assertFalse(rule.isFraudulent(tx));
    }

    @Test
    void doesNotFlagWhenPanOrTimestampMissing() {
        TransactionRepository repo = mock(TransactionRepository.class);
        VelocityRule rule = new VelocityRule(repo);

        assertFalse(rule.isFraudulent(TransactionEntity.builder().pan(null).timestamp(OffsetDateTime.now()).build()));
        assertFalse(rule.isFraudulent(TransactionEntity.builder().pan("4111").timestamp(null).build()));
    }
}
