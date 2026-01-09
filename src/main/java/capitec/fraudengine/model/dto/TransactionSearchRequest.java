
package capitec.fraudengine.model.dto;

import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public record TransactionSearchRequest(
        String pan,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime from,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime to) {
}
