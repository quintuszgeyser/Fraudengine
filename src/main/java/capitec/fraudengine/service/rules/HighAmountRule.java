
package capitec.fraudengine.service.rules;

import capitec.fraudengine.model.TransactionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRule implements FraudRule {

    @Value("${fraud.rule.high-amount.enabled:true}")
    private boolean enabled;

    @Value("${fraud.rule.high-amount.threshold:1000}")
    private BigDecimal threshold;

    @Override
    public boolean isFraudulent(TransactionEntity tx) {
        if (!enabled)
            return false;
        return tx.getAmount() != null && tx.getAmount().compareTo(threshold) > 0;
    }

    @Override
    public String getName() {
        return "HIGH_AMOUNT";
    }
}
