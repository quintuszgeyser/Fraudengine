
package capitec.fraudengine.iso;

import capitec.fraudengine.model.TransactionEntity;
import capitec.fraudengine.repository.TransactionRepository;
import capitec.fraudengine.service.FraudDetectionService;
import jakarta.transaction.Transactional;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

/**
 * Maps ISO8583 requests to TransactionEntity, runs fraud rules via service,
 * persists, and builds the 0210 response.
 */
@Component
public class IsoMessageHandler {
  private final TransactionRepository repo;
  private final FraudDetectionService fraudService;

  public IsoMessageHandler(TransactionRepository repo, FraudDetectionService fraudService) {
    this.repo = repo;
    this.fraudService = fraudService;
  }

  @Transactional
  public ISOMsg handle(ISOMsg req) throws ISOException {
    // --- Read fields we care about
    String mti = req.getMTI();
    String de3 = get(req, 3);
    String de4 = get(req, 4);
    String de7 = get(req, 7);
    String de11 = get(req, 11);
    String de37 = get(req, 37);
    String de41 = get(req, 41);
    String de42 = get(req, 42);
    // String de43 = get(req, 43);
    String de49 = get(req, 49);

    // --- Build entity
    TransactionEntity tx = new TransactionEntity();
    tx.setMti(mti);
    tx.setTimestamp(IsoUtils.parseDe7OrNow(de7)); // MMddHHmmss → OffsetDateTime (UTC, current year)
    tx.setFlagged(false);
    tx.setCategory(null);

    // accountId: DE2 if present; otherwise PAN from DE35 Track2 (before 'D' or '=')
    String pan = get(req, 2);
    if (pan == null) {
      pan = IsoUtils.extractPanFromTrack2(get(req, 35));
    }
    tx.setPan(pan);

    tx.setDe3ProcessingCode(de3);
    tx.setAmount(IsoUtils.parseAmount(de4)); // cents → BigDecimal
    tx.setDe7TransmissionDateTime(de7);
    tx.setStan(de11);
    tx.setDe12LocalTransactionTime(get(req, 12));
    tx.setDe13LocalTransactionDate(get(req, 13));
    tx.setDe14ExpirationDate(get(req, 14));
    tx.setDe15SettlementDate(get(req, 15));
    // If you added DE16 to the entity, you can set it here:
    // tx.setDe16ConversionDate(get(req, 16));
    tx.setDe18MerchantType(get(req, 18));
    tx.setDe22PosEntryMode(get(req, 22));
    tx.setDe23CardSequenceNumber(get(req, 23));
    tx.setDe25PosConditionCode(get(req, 25));
    tx.setDe32AcquiringInstIdCode(get(req, 32));
    tx.setDe33ForwardingInstIdCode(get(req, 33));
    tx.setDe35Track2Data(get(req, 35));
    tx.setRrn(de37);
    tx.setDe40ServiceRestrictionCode(get(req, 40));
    tx.setTerminalId(de41);
    tx.setMerchantId(de42);
    tx.setLocation(get(req, 43));

    // currency: map numeric ISO-4217 (e.g., 710) → alpha-3 (ZAR)
    tx.setCurrency(IsoUtils.currencyNumericToAlpha(de49));

    // Persist any incoming DE44/others if you need them for audit
    tx.setDe44AdditionalResponseData(get(req, 44));
    tx.setDe45Track1Data(get(req, 45));
    tx.setDe48AdditionalDataPrivate(get(req, 48));
    tx.setDe52PinData(get(req, 52));
    tx.setDe53SecurityControlInfo(get(req, 53));
    tx.setDe54AdditionalAmounts(get(req, 54));
    tx.setDe56OriginalDataElements(get(req, 56)); // store if present (request)
    tx.setDe58AuthorizingAgentInstIdCode(get(req, 58));
    tx.setDe59ReservedPrivate(get(req, 59));
    tx.setDe66SettlementCode(get(req, 66));
    tx.setDe70NetworkMgmtInfoCode(get(req, 70));
    tx.setDe74CreditsNumber(get(req, 74));
    tx.setDe75CreditsReversalNumber(get(req, 75));
    tx.setDe76DebitsNumber(get(req, 76));
    tx.setDe77DebitsReversalNumber(get(req, 77));
    tx.setDe78TransferNumber(get(req, 78));
    tx.setDe79TransferReversalNumber(get(req, 79));
    tx.setDe80InquiriesNumber(get(req, 80));
    tx.setDe81AuthorizationsNumber(get(req, 81));
    tx.setDe82CreditsProcessingFeeAmount(get(req, 82));
    tx.setDe83CreditsTransactionFeeAmount(get(req, 83));
    tx.setDe84DebitsProcessingFeeAmount(get(req, 84));
    tx.setDe85DebitsTransactionFeeAmount(get(req, 85));
    tx.setDe86CreditsAmount(get(req, 86));
    tx.setDe87CreditsReversalAmount(get(req, 87));
    tx.setDe88DebitsAmount(get(req, 88));
    tx.setDe89DebitsReversalAmount(get(req, 89));
    tx.setDe90OriginalDataElements(get(req, 90));
    tx.setDe91FileUpdateCode(get(req, 91));
    tx.setDe95ReplacementAmounts(get(req, 95));
    tx.setDe101FileName(get(req, 101));
    tx.setDe102AccountId1(get(req, 102));
    tx.setDe103AccountId2(get(req, 103));
    tx.setDe110AdditionalDataIso(get(req, 110));
    tx.setDe118InvoiceNumber(get(req, 118));
    tx.setDe119TransactionDescription(get(req, 119));
    tx.setDe123PosDataCode(get(req, 123));
    tx.setDe127AdditionalDataPrivate(get(req, 127)); // store if present (request)

    // --- Run fraud rules + persist via the service (saves & returns persisted
    // entity)
    TransactionEntity saved = fraudService.process(tx);

    // --- Build ISO response (0210 for 0200)
    ISOMsg resp = (ISOMsg) req.clone();
    resp.setResponseMTI(); // 0200 → 0210

    // Set decision code (DE39)
    String rc = saved.isFlagged() ? "05" : "00";
    resp.set(39, rc);

    // Echo DE7 and DE11 where appropriate
    if (!resp.hasField(7))
      resp.set(7, IsoUtils.utcDe7Now());
    if (!resp.hasField(11) && de11 != null)
      resp.set(11, de11);

    // Include DE44 with rule names (if you populated it in the service)
    if (saved.isFlagged() && saved.getDe44AdditionalResponseData() != null) {
      resp.set(44, saved.getDe44AdditionalResponseData());
    }

    // IMPORTANT: unset fields that cause analyzer errors in 0210
    // - DE56 often marked "surplus" for Transaction Response unless scheme requires
    // it
    if (resp.hasField(56)) {
      resp.unset(56);
    }
    // - DE127 private data causes fatal if receiver expects 6-digit length
    // LLLLLLVAR and your packager doesn't match
    if (resp.hasField(127)) {
      resp.unset(127);
    }

    return resp;
  }

  private static String get(ISOMsg m, int f) {
    if (m == null || !m.hasField(f))
      return null;
    try {
      return m.getString(f);
    } catch (Exception e) {
      return null;
    }
  }
}
