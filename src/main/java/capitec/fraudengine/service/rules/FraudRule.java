
package capitec.fraudengine.service.rules;

import capitec.fraudengine.model.TransactionEntity;

public interface FraudRule {
    boolean isFraudulent(TransactionEntity tx);

    String getName();
}
