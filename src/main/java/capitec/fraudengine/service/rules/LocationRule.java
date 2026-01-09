
package capitec.fraudengine.service.rules;

import capitec.fraudengine.model.TransactionEntity;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LocationRule implements FraudRule {

    private final Set<String> risky = Set.of("UNKNOWN", "RISKY-COUNTRY");

    @Override
    public boolean isFraudulent(TransactionEntity tx) {
        String loc = tx.getLocation();
        return loc != null && risky.contains(loc);
    }

    @Override
    public String getName() {
        return "LocationRule";
    }
}
