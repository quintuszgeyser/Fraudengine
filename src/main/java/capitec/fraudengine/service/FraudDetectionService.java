
package capitec.fraudengine.service;

import capitec.fraudengine.model.TransactionEntity;
import capitec.fraudengine.repository.TransactionRepository;
import capitec.fraudengine.service.rules.FraudRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final TransactionRepository repo;
    private final List<FraudRule> rules;

    public TransactionEntity process(TransactionEntity tx) {
        boolean flagged = false;
        List<String> hitRules = new ArrayList<>();

        for (FraudRule rule : rules) {
            boolean hit = false;
            try {
                hit = rule.isFraudulent(tx);
            } catch (Exception e) {
                log.error("Rule {} failed on tx: {}", rule.getName(), tx, e);
            }
            if (hit) {
                flagged = true;
                hitRules.add(rule.getName());
                log.info("Rule {} flagged transaction (pan={}, amount={}, location={})",
                        rule.getName(), tx.getPan(), tx.getAmount(), tx.getLocation());
            }
        }

        tx.setFlagged(flagged);
        tx.setResponseCode(flagged ? "05" : "00");

        // Populate DE44 with rule names (JSON array). Example:
        // ["HighAmountRule","SuspiciousAcquirerRule"]
        if (flagged) {

            if (tx.getDe44AdditionalResponseData() == null || tx.getDe44AdditionalResponseData().isBlank()) {
                tx.setDe44AdditionalResponseData(toJsonArray(hitRules));
            } else {

                tx.setDe44AdditionalResponseData(tx.getDe44AdditionalResponseData() + " " + toJsonArray(hitRules));
            }
        } else {

            tx.setDe44AdditionalResponseData("APPROVED");
        }

        return repo.save(tx);
    }

    private static String toJsonArray(List<String> items) {
        if (items == null || items.isEmpty())
            return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append('"').append(items.get(i)).append('"');
            if (i < items.size() - 1)
                sb.append(',');
        }
        sb.append(']');
        return sb.toString();
    }

    public List<TransactionEntity> getFlagged() {
        return repo.findByFlaggedTrue();
    }
}
