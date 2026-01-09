
package capitec.fraudengine.service.rules;

import capitec.fraudengine.model.TransactionEntity;
import capitec.fraudengine.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class VelocityRule implements FraudRule {

    private final TransactionRepository repo;

    public VelocityRule(TransactionRepository repo) {
        this.repo = repo;
    }

    @Value("${fraud.rules.velocity.windowMinutes:15}")
    private int windowMinutes;

    @Value("${fraud.rules.velocity.maxCount:5}")
    private int maxCount;

    @Override
    public boolean isFraudulent(TransactionEntity tx) {
        if (tx.getPan() == null || tx.getTimestamp() == null) {
            return false; // insufficient data
        }

        OffsetDateTime to = tx.getTimestamp();
        OffsetDateTime from = to.minus(windowMinutes, ChronoUnit.MINUTES);

        long recent = repo.countByPanAndTimestampBetween(tx.getPan(), from, to);

        return recent >= maxCount;
    }

    @Override
    public String getName() {
        return "VELOCITY_ACCOUNT";
    }
}
