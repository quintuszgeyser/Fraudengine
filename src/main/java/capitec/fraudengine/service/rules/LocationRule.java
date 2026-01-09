
package capitec.fraudengine.service.rules;

import capitec.fraudengine.model.TransactionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LocationRule implements FraudRule {

    @Value("${fraud.rules.location.risky-values:UNKNOWN,RISKY-COUNTRY}")
    private Set<String> risky;

    @Value("${fraud.rules.location.whitelist-values:}")
    private Set<String> whitelist;

    @Value("${fraud.rules.location.enabled:true}")
    private boolean enabled;

    @Override
    public boolean isFraudulent(TransactionEntity tx) {
        if (!enabled)
            return false;
        String loc = tx.getLocation();
        if (loc == null || loc.isBlank())
            return false;

        if (!whitelist.isEmpty() && whitelist.contains(loc))
            return false;

        return risky.contains(loc);
    }

    @Override
    public String getName() {
        return "LOCATION_RISK";
    }
}
