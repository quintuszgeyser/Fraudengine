
package capitec.fraudengine.repository;

import capitec.fraudengine.model.TransactionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
// Allow Spring Boot to use the embedded database for tests
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TransactionRepositoryTest {

        @Autowired
        TransactionRepository repo;

        @Test
        void savesAndFindsFlaggedTransactions() {
                TransactionEntity t1 = TransactionEntity.builder()
                                .pan("4111111111111111")
                                .amount(new BigDecimal("1250"))
                                .currency("ZAR")
                                .location("UNKNOWN")
                                .timestamp(OffsetDateTime.now())
                                .flagged(true)
                                .build();

                repo.save(t1);

                List<TransactionEntity> flagged = repo.findByFlaggedTrue();
                assertThat(flagged).hasSize(1);
        }
}
