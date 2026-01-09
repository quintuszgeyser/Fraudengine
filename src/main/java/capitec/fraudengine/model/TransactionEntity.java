
package capitec.fraudengine.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_account_ts", columnList = "pan,timestamp"),
        @Index(name = "idx_tx_flagged", columnList = "flagged"),
        @Index(name = "idx_tx_rrn", columnList = "rrn")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    // ---------------- MTI (before DEs) ----------------
    @Column(name = "mti", length = 4)
    private String mti; // ISO8583 MTI

    // ---------------- Core domain fields ----------------
    /** Stored event time (DE7 converted or fallback to now) */
    @Column(name = "timestamp", nullable = false)
    @NotNull
    private OffsetDateTime timestamp;

    /** Result after fraud rule evaluation */
    @Column(name = "flagged", nullable = false)
    private boolean flagged;

    /** Optional category derived later (from DE3 or merchant type) */
    @Column(name = "category", length = 64)
    private String category;

    // ---------------- Primary key ----------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------------- ISO8583 fields (ordered by DE number) ----------------
    /** DE2: Primary Account Number (PAN) */
    @Column(name = "pan", nullable = false, length = 32) // your chosen column name
    @NotBlank
    private String pan;

    /** DE3: Processing Code */
    @Column(name = "de3_processing_code", length = 6)
    private String de3ProcessingCode;

    /** DE4: Amount, Transaction (stored as decimal; DE4 is cents) */
    @Column(name = "amount", nullable = false, precision = 19, scale = 2) // keep your column
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;

    /** DE5: Amount, Reconciliation */
    @Column(name = "de5_amount_reconciliation", length = 12)
    private String de5AmountReconciliation;

    /** DE7: Transmission Date & Time (MMDDhhmmss source) */
    @Column(name = "de7_transmission_datetime", length = 10)
    private String de7TransmissionDateTime;

    /** DE9: Conversion Rate, Settlement */
    @Column(name = "de9_conversion_rate_settlement", length = 8)
    private String de9ConversionRateSettlement;

    /** DE11: STAN */
    @Column(name = "stan", length = 6) // keep your column name
    private String stan;

    /** DE12: Local Transaction Time (hhmmss) */
    @Column(name = "de12_local_transaction_time", length = 6)
    private String de12LocalTransactionTime;

    /** DE13: Local Transaction Date (MMDD) */
    @Column(name = "de13_local_transaction_date", length = 4)
    private String de13LocalTransactionDate;

    /** DE14: Expiration Date (YYMM) */
    @Column(name = "de14_expiration_date", length = 4)
    private String de14ExpirationDate;

    /** DE15: Settlement Date (MMDD) */
    @Column(name = "de15_settlement_date", length = 4)
    private String de15SettlementDate;

    /** DE18: Merchant Type */
    @Column(name = "de18_merchant_type", length = 4)
    private String de18MerchantType;

    /** DE22: POS Entry Mode */
    @Column(name = "de22_pos_entry_mode", length = 3)
    private String de22PosEntryMode;

    /** DE23: Card Sequence Number */
    @Column(name = "de23_card_sequence_number", length = 3)
    private String de23CardSequenceNumber;

    /** DE25: POS Condition Code */
    @Column(name = "de25_pos_condition_code", length = 2)
    private String de25PosConditionCode;

    /** DE26: POS PIN Capture Code */
    @Column(name = "de26_pos_pin_capture_code", length = 2)
    private String de26PosPinCaptureCode;

    /** DE27: Authorization ID Response Length */
    @Column(name = "de27_auth_id_response_length", length = 1)
    private String de27AuthIdResponseLength;

    /** DE28: Transaction Fee Amount */
    @Column(name = "de28_transaction_fee_amount", length = 9)
    private String de28TransactionFeeAmount;

    /** DE30: Transaction Processing Fee Amount */
    @Column(name = "de30_transaction_processing_fee_amount", length = 9)
    private String de30TransactionProcessingFeeAmount;

    /** DE32: Acquiring Institution ID Code */
    @Column(name = "de32_acquiring_inst_id_code")
    private String de32AcquiringInstIdCode;

    /** DE33: Forwarding Institution ID Code */
    @Column(name = "de33_forwarding_inst_id_code")
    private String de33ForwardingInstIdCode;

    /** DE35: Track 2 Data */
    @Column(name = "de35_track_2_data")
    private String de35Track2Data;

    /** DE37: Retrieval Reference Number */
    @Column(name = "rrn", length = 12) // keep your column name
    private String rrn;

    /** DE39: Response Code */
    @Column(name = "response_code", length = 2) // keep your column name
    private String responseCode;

    /** DE40: Service Restriction Code */
    @Column(name = "de40_service_restriction_code", length = 3)
    private String de40ServiceRestrictionCode;

    /** DE41: Card Acceptor Terminal ID */
    @Column(name = "terminal_id", length = 8) // keep your column name
    private String terminalId;

    /** DE42: Card Acceptor ID Code */
    @Column(name = "merchant_id", length = 15) // keep your column name
    private String merchantId;

    /** DE43: Card Acceptor Name/Location */
    @Column(name = "location", length = 128) // keep your column name
    private String location;

    /** DE44: Additional Response Data */
    @Column(name = "de44_additional_response_data")
    private String de44AdditionalResponseData;

    /** DE45: Track 1 Data */
    @Column(name = "de45_track_1_data")
    private String de45Track1Data;

    /** DE48: Additional Data - Private */
    @Column(name = "de48_additional_data_private")
    private String de48AdditionalDataPrivate;

    /** DE49: Currency Code, Transaction */
    @Column(name = "currency", nullable = false, length = 3) // keep your column
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$")
    private String currency;

    /** DE52: PIN Data (encrypted) */
    @Column(name = "de52_pin_data", length = 8)
    private String de52PinData;

    /** DE53: Security Related Control Information */
    @Column(name = "de53_security_control_info", length = 48)
    private String de53SecurityControlInfo;

    /** DE54: Additional Amounts */
    @Column(name = "de54_additional_amounts")
    private String de54AdditionalAmounts;

    /** DE56: Original Data Elements (LLLVAR) */
    @Column(name = "de56_original_data_elements")
    private String de56OriginalDataElements;

    /** DE58: Authorizing Agent Institution ID Code */
    @Column(name = "de58_authorizing_agent_inst_id_code")
    private String de58AuthorizingAgentInstIdCode;

    /** DE59: Reserved (Private) */
    @Column(name = "de59_reserved_private")
    private String de59ReservedPrivate;

    /** DE66: Settlement Code */
    @Column(name = "de66_settlement_code", length = 1)
    private String de66SettlementCode;

    /** DE70: Network Management Information Code */
    @Column(name = "de70_network_mgmt_info_code", length = 3)
    private String de70NetworkMgmtInfoCode;

    /** DE74–DE81 counts/metrics */
    @Column(name = "de74_credits_number", length = 10)
    private String de74CreditsNumber;

    @Column(name = "de75_credits_reversal_number", length = 10)
    private String de75CreditsReversalNumber;

    @Column(name = "de76_debits_number", length = 10)
    private String de76DebitsNumber;

    @Column(name = "de77_debits_reversal_number", length = 10)
    private String de77DebitsReversalNumber;

    @Column(name = "de78_transfer_number", length = 10)
    private String de78TransferNumber;

    @Column(name = "de79_transfer_reversal_number", length = 10)
    private String de79TransferReversalNumber;

    @Column(name = "de80_inquiries_number", length = 10)
    private String de80InquiriesNumber;

    @Column(name = "de81_authorizations_number", length = 10)
    private String de81AuthorizationsNumber;

    /** DE82–DE85 fee amounts */
    @Column(name = "de82_credits_processing_fee_amount", length = 12)
    private String de82CreditsProcessingFeeAmount;

    @Column(name = "de83_credits_transaction_fee_amount", length = 12)
    private String de83CreditsTransactionFeeAmount;

    @Column(name = "de84_debits_processing_fee_amount", length = 12)
    private String de84DebitsProcessingFeeAmount;

    @Column(name = "de85_debits_transaction_fee_amount", length = 12)
    private String de85DebitsTransactionFeeAmount;

    /** DE86–DE89 amounts */
    @Column(name = "de86_credits_amount", length = 16)
    private String de86CreditsAmount;

    @Column(name = "de87_credits_reversal_amount", length = 16)
    private String de87CreditsReversalAmount;

    @Column(name = "de88_debits_amount", length = 16)
    private String de88DebitsAmount;

    @Column(name = "de89_debits_reversal_amount", length = 16)
    private String de89DebitsReversalAmount;

    /** DE90: Original Data Elements (fixed 42) */
    @Column(name = "de90_original_data_elements", length = 42)
    private String de90OriginalDataElements;

    /** DE91: File Update Code */
    @Column(name = "de91_file_update_code", length = 1)
    private String de91FileUpdateCode;

    /** DE95: Replacement Amounts (fixed 42) */
    @Column(name = "de95_replacement_amounts", length = 42)
    private String de95ReplacementAmounts;

    /** DE101: File Name */
    @Column(name = "de101_file_name")
    private String de101FileName;

    /** DE102–DE103: Account IDs */
    @Column(name = "de102_account_id_1")
    private String de102AccountId1;

    @Column(name = "de103_account_id_2")
    private String de103AccountId2;

    /** DE110: Additional Data ISO */
    @Column(name = "de110_additional_data_iso")
    private String de110AdditionalDataIso;

    /** DE118: Invoice Number */
    @Column(name = "de118_invoice_number")
    private String de118InvoiceNumber;

    /** DE119: Transaction Description */
    @Column(name = "de119_transaction_description")
    private String de119TransactionDescription;

    /** DE123: POS Data Code (LLLVAR) */
    @Column(name = "de123_pos_data_code")
    private String de123PosDataCode;

    /**
     * DE127: Additional Data - Private (LLLVAR with 6-digit length in your parser)
     */
    @Column(name = "de127_additional_data_private")
    private String de127AdditionalDataPrivate;
}
