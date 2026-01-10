
package capitec.fraudengine;

import java.lang.reflect.Field;

/**
 * Tiny helper to inject @Value fields without starting Spring in unit tests.
 */
public final class TestUtil {
    private TestUtil() {
    }

    public static void inject(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Injection failed for field '" + field + "'", e);
        }
    }
}
