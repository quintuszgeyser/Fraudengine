
package capitec.fraudengine.Controllers;

import capitec.fraudengine.model.TransactionEntity;
import capitec.fraudengine.model.dto.TransactionRequest;
import capitec.fraudengine.service.FraudDetectionService;
import capitec.fraudengine.iso.IsoUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class FraudController {

    private final FraudDetectionService fraudService;

    @PostMapping(path = "/transactions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionEntity> create(@Valid @RequestBody TransactionRequest req) {

        // Defensive: if someone ever sends numeric ISO-4217 (e.g., "710"), convert to
        // alpha-3 ("ZAR")
        String currency = req.getCurrency();
        if (currency != null && currency.matches("^\\d{3}$")) {
            currency = IsoUtils.currencyNumericToAlpha(currency);
        }

        // Ignore client-supplied id on create to avoid accidental updates
        TransactionEntity tx = TransactionEntity.builder()
                .mti(req.getMti())
                .timestamp(req.getTimestamp())
                .flagged(Boolean.TRUE.equals(req.getFlagged()))
                .category(req.getCategory())
                .pan(req.getPan())
                .de3ProcessingCode(req.getDe3ProcessingCode())
                .amount(req.getAmount())
                .de5AmountReconciliation(req.getDe5AmountReconciliation())
                .de7TransmissionDateTime(req.getDe7TransmissionDateTime())
                .de9ConversionRateSettlement(req.getDe9ConversionRateSettlement())
                .stan(req.getStan())
                .de12LocalTransactionTime(req.getDe12LocalTransactionTime())
                .de13LocalTransactionDate(req.getDe13LocalTransactionDate())
                .de14ExpirationDate(req.getDe14ExpirationDate())
                .de15SettlementDate(req.getDe15SettlementDate())
                // .de16ConversionDate(req.getDe16ConversionDate())
                .de18MerchantType(req.getDe18MerchantType())
                .de22PosEntryMode(req.getDe22PosEntryMode())
                .de23CardSequenceNumber(req.getDe23CardSequenceNumber())
                .de25PosConditionCode(req.getDe25PosConditionCode())
                .de26PosPinCaptureCode(req.getDe26PosPinCaptureCode())
                .de27AuthIdResponseLength(req.getDe27AuthIdResponseLength())
                .de28TransactionFeeAmount(req.getDe28TransactionFeeAmount())
                .de30TransactionProcessingFeeAmount(req.getDe30TransactionProcessingFeeAmount())
                .de32AcquiringInstIdCode(req.getDe32AcquiringInstIdCode())
                .de33ForwardingInstIdCode(req.getDe33ForwardingInstIdCode())
                .de35Track2Data(req.getDe35Track2Data())
                .rrn(req.getRrn())
                .responseCode(req.getResponseCode())
                .de40ServiceRestrictionCode(req.getDe40ServiceRestrictionCode())
                .terminalId(req.getTerminalId())
                .merchantId(req.getMerchantId())
                .location(req.getLocation())
                .de44AdditionalResponseData(req.getDe44AdditionalResponseData())
                .de45Track1Data(req.getDe45Track1Data())
                .de48AdditionalDataPrivate(req.getDe48AdditionalDataPrivate())
                .currency(currency != null ? currency : req.getCurrency())
                .de52PinData(req.getDe52PinData())
                .de53SecurityControlInfo(req.getDe53SecurityControlInfo())
                .de54AdditionalAmounts(req.getDe54AdditionalAmounts())
                .de56OriginalDataElements(req.getDe56OriginalDataElements())
                .de58AuthorizingAgentInstIdCode(req.getDe58AuthorizingAgentInstIdCode())
                .de59ReservedPrivate(req.getDe59ReservedPrivate())
                .de66SettlementCode(req.getDe66SettlementCode())
                .de70NetworkMgmtInfoCode(req.getDe70NetworkMgmtInfoCode())
                .de74CreditsNumber(req.getDe74CreditsNumber())
                .de75CreditsReversalNumber(req.getDe75CreditsReversalNumber())
                .de76DebitsNumber(req.getDe76DebitsNumber())
                .de77DebitsReversalNumber(req.getDe77DebitsReversalNumber())
                .de78TransferNumber(req.getDe78TransferNumber())
                .de79TransferReversalNumber(req.getDe79TransferReversalNumber())
                .de80InquiriesNumber(req.getDe80InquiriesNumber())
                .de81AuthorizationsNumber(req.getDe81AuthorizationsNumber())
                .de82CreditsProcessingFeeAmount(req.getDe82CreditsProcessingFeeAmount())
                .de83CreditsTransactionFeeAmount(req.getDe83CreditsTransactionFeeAmount())
                .de84DebitsProcessingFeeAmount(req.getDe84DebitsProcessingFeeAmount())
                .de85DebitsTransactionFeeAmount(req.getDe85DebitsTransactionFeeAmount())
                .de86CreditsAmount(req.getDe86CreditsAmount())
                .de87CreditsReversalAmount(req.getDe87CreditsReversalAmount())
                .de88DebitsAmount(req.getDe88DebitsAmount())
                .de89DebitsReversalAmount(req.getDe89DebitsReversalAmount())
                .de90OriginalDataElements(req.getDe90OriginalDataElements())
                .de91FileUpdateCode(req.getDe91FileUpdateCode())
                .de95ReplacementAmounts(req.getDe95ReplacementAmounts())
                .de101FileName(req.getDe101FileName())
                .de102AccountId1(req.getDe102AccountId1())
                .de103AccountId2(req.getDe103AccountId2())
                .de110AdditionalDataIso(req.getDe110AdditionalDataIso())
                .de118InvoiceNumber(req.getDe118InvoiceNumber())
                .de119TransactionDescription(req.getDe119TransactionDescription())
                .de123PosDataCode(req.getDe123PosDataCode())
                .de127AdditionalDataPrivate(req.getDe127AdditionalDataPrivate())
                .build();

        return ResponseEntity.ok(fraudService.process(tx));
    }

    @GetMapping("/fraud-flags")
    public ResponseEntity<List<TransactionEntity>> flags() {
        return ResponseEntity.ok(fraudService.getFlagged());
    }
}
