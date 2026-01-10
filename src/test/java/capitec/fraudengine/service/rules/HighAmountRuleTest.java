
package capitec.fraudengine.service.rules;

import capitec.fraudengine.TestUtil;
import capitec.fraudengine.model.TransactionEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HighAmountRuleTest {

    @Test
    void flagsWhenAmountExceedsThreshold_andEnabled() {
        HighAmountRule rule = new HighAmountRule();
        TestUtil.inject(rule, "enabled", true);
        TestUtil.inject(rule, "threshold", new BigDecimal("1000"));

        TransactionEntity tx = TransactionEntity.builder()
                .pan("A1")
                .amount(new BigDecimal("1500"))
                .currency("ZAR")
                .build();

        assertTrue(rule.isFraudulent(tx));
        assertEquals("HIGH_AMOUNT", rule.getName());
    }

    @Test
    void doesNotFlagWhenDisabled_orBelowThreshold_orMissingAmount() {
        HighAmountRule rule = new HighAmountRule();
        TestUtil.inject(rule, "enabled", false);
        TestUtil.inject(rule, "threshold", new BigDecimal("1000"));

        TransactionEntity big = TransactionEntity.builder()
                .amount(new BigDecimal("100000"))
                .currency("ZAR")
                .build();
        assertFalse(rule.isFraudulent(big));

        TestUtil.inject(rule, "enabled", true);
        TransactionEntity low = TransactionEntity.builder()
                .amount(new BigDecimal("999.99"))
                .currency("ZAR")
                .build();
        assertFalse(rule.isFraudulent(low));

        assertFalse(rule.isFraudulent(TransactionEntity.builder().currency("ZAR").build()));
    }
}
