package dec.core.context.runtime;

import java.util.Objects;

/** Opaque resolved write-intent identity; exact and case-sensitive. */
public final class RuntimeWriteIntentId {
    private final String value;

    private RuntimeWriteIntentId(String value) {
        this.value = requireExact(value);
    }

    public static RuntimeWriteIntentId of(String value) {
        return new RuntimeWriteIntentId(value);
    }

    public String value() {
        return value;
    }

    private static String requireExact(String value) {
        Objects.requireNonNull(value, "value");
        if (value.isEmpty() || !value.equals(value.trim())) {
            throw new IllegalArgumentException("value must be non-blank and trimmed");
        }
        return value;
    }
}
