
package capitec.fraudengine.repository;

import capitec.fraudengine.model.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findByFlaggedTrue();

    long countByPanAndTimestampBetween(
            String pan,
            OffsetDateTime from,
            OffsetDateTime to);

    List<TransactionEntity> findByPanAndTimestampBetweenOrderByTimestampDesc(
            String pan,
            OffsetDateTime from,
            OffsetDateTime to);

    Optional<TransactionEntity> findTop1ByPanOrderByTimestampDesc(String pan);
}
