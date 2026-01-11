
package capitec.fraudengine.controllers;

import capitec.fraudengine.model.TransactionEntity;
import capitec.fraudengine.model.dto.TransactionRequest;
import capitec.fraudengine.model.dto.TransactionSearchRequest;
import capitec.fraudengine.service.FraudDetectionService;
import capitec.fraudengine.iso.IsoUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class FraudController {

        private final FraudDetectionService fraudService;

        @Operation(summary = "Submit a transaction for fraud evaluation", description = "Processes a transaction and returns whether it is flagged.",

                        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionRequest.class), examples = {
                                        // ───────────── Simple Insomnia example ─────────────
                                        @ExampleObject(name = "Simple POS", summary = "Minimal payload (Insomnia)", value = """
                                                        {
                                                          "timestamp": "2026-01-08T14:15:00+00:00",
                                                          "pan": "5284971100010063",
                                                          "amount": 50000.00,
                                                          "currency": "ZAR",
                                                          "location": "CPT",
                                                          "category": "POS"
                                                        }
                                                        """),
                                        // ───────────── All fields (Approved) from Insomnia ─────────────
                                        @ExampleObject(name = "All fields - Approved", summary = "Full ISO-style payload that approves", value = """
                                                        {
                                                            "mti": "0200",
                                                            "timestamp": "2026-01-09T10:37:58Z",
                                                            "flagged": false,
                                                            "category": "ATM",
                                                            "id": 28,
                                                            "pan": "5284971100131851",
                                                            "de3ProcessingCode": "000000",
                                                            "amount": 5.00,
                                                            "de5AmountReconciliation": null,
                                                            "de7TransmissionDateTime": "0109103758",
                                                            "de9ConversionRateSettlement": null,
                                                            "stan": "030388",
                                                            "de12LocalTransactionTime": "123758",
                                                            "de13LocalTransactionDate": "0109",
                                                            "de14ExpirationDate": "2611",
                                                            "de15SettlementDate": "0109",
                                                            "de18MerchantType": "5411",
                                                            "de22PosEntryMode": "051",
                                                            "de23CardSequenceNumber": "000",
                                                            "de25PosConditionCode": "00",
                                                            "de26PosPinCaptureCode": null,
                                                            "de27AuthIdResponseLength": null,
                                                            "de28TransactionFeeAmount": null,
                                                            "de30TransactionProcessingFeeAmount": null,
                                                            "de32AcquiringInstIdCode": "528497",
                                                            "de33ForwardingInstIdCode": "330000",
                                                            "de35Track2Data": "5284971100131851D2611206000007310000",
                                                            "rrn": "600912030388",
                                                            "responseCode": "00",
                                                            "de40ServiceRestrictionCode": "206",
                                                            "terminalId": "BAIBG301",
                                                            "merchantId": "000000110030103",
                                                            "location": "PWC TEST MERCHANT (TermStellenbosch WeZA",
                                                            "de44AdditionalResponseData": "APPROVED",
                                                            "de45Track1Data": null,
                                                            "de48AdditionalDataPrivate": null,
                                                            "currency": "ZAR",
                                                            "de52PinData": null,
                                                            "de53SecurityControlInfo": null,
                                                            "de54AdditionalAmounts": null,
                                                            "de56OriginalDataElements": "1510",
                                                            "de58AuthorizingAgentInstIdCode": null,
                                                            "de59ReservedPrivate": null,
                                                            "de66SettlementCode": null,
                                                            "de70NetworkMgmtInfoCode": null,
                                                            "de74CreditsNumber": null,
                                                            "de75CreditsReversalNumber": null,
                                                            "de76DebitsNumber": null,
                                                            "de77DebitsReversalNumber": null,
                                                            "de78TransferNumber": null,
                                                            "de79TransferReversalNumber": null,
                                                            "de80InquiriesNumber": null,
                                                            "de81AuthorizationsNumber": null,
                                                            "de82CreditsProcessingFeeAmount": null,
                                                            "de83CreditsTransactionFeeAmount": null,
                                                            "de84DebitsProcessingFeeAmount": null,
                                                            "de85DebitsTransactionFeeAmount": null,
                                                            "de86CreditsAmount": null,
                                                            "de87CreditsReversalAmount": null,
                                                            "de88DebitsAmount": null,
                                                            "de89DebitsReversalAmount": null,
                                                            "de90OriginalDataElements": null,
                                                            "de91FileUpdateCode": null,
                                                            "de95ReplacementAmounts": null,
                                                            "de9ConversionRateSettlement": null,
                                                            "de101FileName": null,
                                                            "de102AccountId1": null,
                                                            "de103AccountId2": null,
                                                            "de110AdditionalDataIso": null,
                                                            "de118InvoiceNumber": null,
                                                            "de119TransactionDescription": null,
                                                            "de123PosDataCode": "A1010151134C100",
                                                            "de127AdditionalDataPrivate": null
                                                        }
                                                        """),
                                        // ───────────── High value (flagged) from Insomnia ─────────────
                                        @ExampleObject(name = "High value – Flagged", summary = "Triggers high-amount rule and is flagged", value = """
                                                        {
                                                            "mti": "0200",
                                                            "timestamp": "2026-01-06T10:15:38Z",
                                                            "flagged": true,
                                                            "category": "POS",
                                                            "id": 56,
                                                            "pan": "5284971100010063",
                                                            "de3ProcessingCode": "000000",
                                                            "amount": 100000.00,
                                                            "de5AmountReconciliation": null,
                                                            "de7TransmissionDateTime": "0106101538",
                                                            "de9ConversionRateSettlement": null,
                                                            "stan": "030294",
                                                            "de12LocalTransactionTime": "121538",
                                                            "de13LocalTransactionDate": "0106",
                                                            "de14ExpirationDate": "2611",
                                                            "de15SettlementDate": "0106",
                                                            "de18MerchantType": "5411",
                                                            "de22PosEntryMode": "051",
                                                            "de23CardSequenceNumber": "000",
                                                            "de25PosConditionCode": "00",
                                                            "de26PosPinCaptureCode": null,
                                                            "de27AuthIdResponseLength": null,
                                                            "de28TransactionFeeAmount": null,
                                                            "de30TransactionProcessingFeeAmount": null,
                                                            "de32AcquiringInstIdCode": "528497",
                                                            "de33ForwardingInstIdCode": "330000",
                                                            "de35Track2Data": "5284971100010063D2611226000005220000",
                                                            "rrn": "600612030294",
                                                            "responseCode": null,
                                                            "de40ServiceRestrictionCode": "226",
                                                            "terminalId": "BAIBG301",
                                                            "merchantId": "000000110030103",
                                                            "location": "PWC TEST MERCHANT (TermStellenbosch WeZA",
                                                            "de44AdditionalResponseData": null,
                                                            "de45Track1Data": null,
                                                            "de48AdditionalDataPrivate": null,
                                                            "currency": "ZAR",
                                                            "de52PinData": null,
                                                            "de53SecurityControlInfo": null,
                                                            "de54AdditionalAmounts": null,
                                                            "de56OriginalDataElements": "1510",
                                                            "de58AuthorizingAgentInstIdCode": null,
                                                            "de59ReservedPrivate": null,
                                                            "de66SettlementCode": null,
                                                            "de70NetworkMgmtInfoCode": null,
                                                            "de74CreditsNumber": null,
                                                            "de75CreditsReversalNumber": null,
                                                            "de76DebitsNumber": null,
                                                            "de77DebitsReversalNumber": null,
                                                            "de78TransferNumber": null,
                                                            "de79TransferReversalNumber": null,
                                                            "de80InquiriesNumber": null,
                                                            "de81AuthorizationsNumber": null,
                                                            "de82CreditsProcessingFeeAmount": null,
                                                            "de83CreditsTransactionFeeAmount": null,
                                                            "de84DebitsProcessingFeeAmount": null,
                                                            "de85DebitsTransactionFeeAmount": null,
                                                            "de86CreditsAmount": null,
                                                            "de87CreditsReversalAmount": null,
                                                            "de88DebitsAmount": null,
                                                            "de89DebitsReversalAmount": null,
                                                            "de90OriginalDataElements": null,
                                                            "de91FileUpdateCode": null,
                                                            "de95ReplacementAmounts": null,
                                                            "de110AdditionalDataIso": null,
                                                            "de118InvoiceNumber": null,
                                                            "de119TransactionDescription": null,
                                                            "de123PosDataCode": "A1010151134C100",
                                                            "de127AdditionalDataPrivate": null
                                                        }
                                                        """)
                        })), responses = {
                                        @ApiResponse(responseCode = "200", description = "Processed transaction response", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionEntity.class), examples = {
                                                        @ExampleObject(name = "Approved response", summary = "Minimal approval response", value = """
                                                                        {
                                                                          "id": 28,
                                                                          "flagged": false,
                                                                          "responseCode": "00",
                                                                          "pan": "5284971100131851",
                                                                          "amount": 5.00,
                                                                          "currency": "ZAR",
                                                                          "timestamp": "2026-01-09T10:37:58Z",
                                                                          "category": "ATM",
                                                                          "location": "PWC TEST MERCHANT (TermStellenbosch WeZA"
                                                                        }
                                                                        """),
                                                        @ExampleObject(name = "Flagged response", summary = "Decline due to risk rules", value = """
                                                                        {
                                                                          "id": 56,
                                                                          "flagged": true,
                                                                          "responseCode": "05",
                                                                          "pan": "5284971100010063",
                                                                          "amount": 100000.00,
                                                                          "currency": "ZAR",
                                                                          "timestamp": "2026-01-06T10:15:38Z",
                                                                          "category": "POS",
                                                                          "location": "UNKNOWN"
                                                                        }
                                                                        """)
                                        }))
                        })
        @PostMapping(path = "/transactions", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<TransactionEntity> create(
                        // Keep Spring's RequestBody for binding
                        @Valid @org.springframework.web.bind.annotation.RequestBody TransactionRequest req) {

                // Defensive: numeric ISO-4217 -> alpha-3 ("710" -> "ZAR")
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

        @Operation(summary = "List flagged transactions", responses = {
                        @ApiResponse(responseCode = "200", description = "Array of flagged transactions", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionEntity.class), examples = @ExampleObject(name = "Flagged list example", value = """
                                        [
                                          {
                                            "id": 56,
                                            "flagged": true,
                                            "responseCode": "05",
                                            "pan": "5284971100010063",
                                            "amount": 100000.00,
                                            "currency": "ZAR",
                                            "timestamp": "2026-01-06T10:15:38Z",
                                            "category": "POS",
                                            "location": "UNKNOWN"
                                          }
                                        ]
                                        """)))
        })
        @GetMapping("/fraud-flags")
        public ResponseEntity<List<TransactionEntity>> flags() {
                return ResponseEntity.ok(fraudService.getFlagged());
        }

        @Operation(summary = "Search transactions by PAN and time window", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionSearchRequest.class), examples = @ExampleObject(name = "Search window example", value = """
                        {
                          "pan": "5284971100131851",
                          "from": "2026-01-05T14:15:00+00:00",
                          "to":   "2026-01-09T14:15:00+00:00"
                        }
                        """))), responses = {
                        @ApiResponse(responseCode = "200", description = "List of matching transactions", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionEntity.class)))
        })
        @PostMapping(path = "/transactions/search", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<List<TransactionEntity>> transactionsByPanAndRangeBody(
                        @Valid @org.springframework.web.bind.annotation.RequestBody TransactionSearchRequest req) {
                return ResponseEntity.ok(
                                fraudService.getTransactionsByPanAndRange(req.pan(), req.from(), req.to()));
        }
}
