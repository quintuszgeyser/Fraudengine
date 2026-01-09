
package capitec.fraudengine.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionRequest {

    // -------- CORE (required) --------
    @NotNull(message = "timestamp is required")
    private OffsetDateTime timestamp;

    @NotBlank(message = "pan is required")
    private String pan;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.00", message = "amount must be >= 0.00")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be alpha-3 (e.g., ZAR)")
    private String currency;

    @NotBlank(message = "location is required")
    private String location;

    @NotBlank(message = "category is required")
    private String category;

    // -------- OPTIONALS (all nullable) --------
    private String mti;
    private Boolean flagged;
    private Long id;

    private String de3ProcessingCode;
    private String de5AmountReconciliation;
    private String de7TransmissionDateTime;
    private String de9ConversionRateSettlement;
    private String stan;
    private String de12LocalTransactionTime;
    private String de13LocalTransactionDate;
    private String de14ExpirationDate;
    private String de15SettlementDate;
    private String de16ConversionDate;
    private String de18MerchantType;
    private String de22PosEntryMode;
    private String de23CardSequenceNumber;
    private String de25PosConditionCode;
    private String de26PosPinCaptureCode;
    private String de27AuthIdResponseLength;
    private String de28TransactionFeeAmount;
    private String de30TransactionProcessingFeeAmount;
    private String de32AcquiringInstIdCode;
    private String de33ForwardingInstIdCode;
    private String de35Track2Data;
    private String rrn;
    private String responseCode;
    private String de40ServiceRestrictionCode;
    private String terminalId;
    private String merchantId;
    private String de44AdditionalResponseData;
    private String de45Track1Data;
    private String de48AdditionalDataPrivate;
    private String de52PinData;
    private String de53SecurityControlInfo;
    private String de54AdditionalAmounts;
    private String de56OriginalDataElements;
    private String de58AuthorizingAgentInstIdCode;
    private String de59ReservedPrivate;
    private String de66SettlementCode;
    private String de70NetworkMgmtInfoCode;
    private String de74CreditsNumber;
    private String de75CreditsReversalNumber;
    private String de76DebitsNumber;
    private String de77DebitsReversalNumber;
    private String de78TransferNumber;
    private String de79TransferReversalNumber;
    private String de80InquiriesNumber;
    private String de81AuthorizationsNumber;
    private String de82CreditsProcessingFeeAmount;
    private String de83CreditsTransactionFeeAmount;
    private String de84DebitsProcessingFeeAmount;
    private String de85DebitsTransactionFeeAmount;
    private String de86CreditsAmount;
    private String de87CreditsReversalAmount;
    private String de88DebitsAmount;
    private String de89DebitsReversalAmount;
    private String de90OriginalDataElements;
    private String de91FileUpdateCode;
    private String de95ReplacementAmounts;
    private String de101FileName;
    private String de102AccountId1;
    private String de103AccountId2;
    private String de110AdditionalDataIso;
    private String de118InvoiceNumber;
    private String de119TransactionDescription;
    private String de123PosDataCode;
    private String de127AdditionalDataPrivate;
}
