
package capitec.fraudengine.service.rules;

import capitec.fraudengine.model.TransactionEntity;

public interface FraudRuleTest {
    boolean isFraudulent(TransactionEntity tx);

    String getName();
}
