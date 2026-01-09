
package capitec.fraudengine.iso;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class IsoUtils {
  private IsoUtils() {
  }

  /**
   * Parse DE7 (MMDDhhmmss) as UTC using current year; fallback to 'now' if
   * invalid/missing.
   */
  public static OffsetDateTime parseDe7OrNow(String de7) {
    if (de7 == null || de7.length() != 10)
      return OffsetDateTime.now(ZoneOffset.UTC);
    var now = OffsetDateTime.now(ZoneOffset.UTC);
    int mm = safeInt(de7.substring(0, 2), now.getMonthValue());
    int dd = safeInt(de7.substring(2, 4), now.getDayOfMonth());
    int hh = safeInt(de7.substring(4, 6), now.getHour());
    int mi = safeInt(de7.substring(6, 8), now.getMinute());
    int ss = safeInt(de7.substring(8, 10), now.getSecond());
    return OffsetDateTime.of(now.getYear(), mm, dd, hh, mi, ss, 0, ZoneOffset.UTC);
  }

  /** Current DE7 in UTC (MMDDhhmmss). */
  public static String utcDe7Now() {
    return DateTimeFormatter.ofPattern("MMddHHmmss").format(OffsetDateTime.now(ZoneOffset.UTC));
  }

  /** ISO DE4 is cents (numeric). Convert to BigDecimal decimal amount. */
  public static BigDecimal parseAmount(String de4) {
    if (de4 == null || !de4.matches("\\d+"))
      return BigDecimal.ZERO;
    return new BigDecimal(de4).movePointLeft(2);
  }

  /**
   * Extract PAN from Track 2 (DE35) — substring before 'D'. Returns null if not
   * found.
   */
  public static String extractPanFromTrack2(String track2) {
    if (track2 == null)
      return null;
    int idx = track2.indexOf('D'); // jPOS commonly uses 'D' as separator
    if (idx > 0)
      return track2.substring(0, idx);
    // Some systems use '=' instead of 'D'
    int eqIdx = track2.indexOf('=');
    if (eqIdx > 0)
      return track2.substring(0, eqIdx);
    return null;
  }

  /** Map numeric ISO-4217 (e.g., "710") to alpha-3 (e.g., "ZAR"). */
  public static String currencyNumericToAlpha(String n) {
    if (n == null)
      return null;
    switch (n.trim()) {
      case "710":
        return "ZAR"; // South African Rand
      case "404":
        return "KES"; // Kenyan Shilling
      case "840":
        return "USD";
      case "978":
        return "EUR";
      case "826":
        return "GBP";
      // add more codes your scheme requires
      default:
        return n; // fallback: leave as-is (beware of entity @Pattern)
    }
  }

  private static int safeInt(String s, int fallback) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return fallback;
    }
  }
}
