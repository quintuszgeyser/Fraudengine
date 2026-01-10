
package capitec.fraudengine.service.rules;

import capitec.fraudengine.TestUtil;
import capitec.fraudengine.model.TransactionEntity;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LocationRuleTest {

    @Test
    void flagsRiskyLocation_whenEnabled_andNotWhitelisted() {
        LocationRule rule = new LocationRule();
        TestUtil.inject(rule, "enabled", true);
        TestUtil.inject(rule, "risky", Set.of("RISKY-COUNTRY", "UNKNOWN"));
        TestUtil.inject(rule, "whitelist", Set.of("SOUTH-AFRICA"));

        TransactionEntity tx = TransactionEntity.builder()
                .location("RISKY-COUNTRY")
                .build();

        assertTrue(rule.isFraudulent(tx));
        assertEquals("LOCATION_RISK", rule.getName());
    }

    @Test
    void doesNotFlagWhenWhitelisted_orBlank_orDisabled() {
        LocationRule rule = new LocationRule();
        TestUtil.inject(rule, "enabled", true);
        TestUtil.inject(rule, "risky", Set.of("UNKNOWN"));
        TestUtil.inject(rule, "whitelist", Set.of("UNKNOWN"));

        assertFalse(rule.isFraudulent(TransactionEntity.builder().location("UNKNOWN").build()));
        assertFalse(rule.isFraudulent(TransactionEntity.builder().location(" ").build()));

        TestUtil.inject(rule, "enabled", false);
        assertFalse(rule.isFraudulent(TransactionEntity.builder().location("RISKY-COUNTRY").build()));
    }
}
