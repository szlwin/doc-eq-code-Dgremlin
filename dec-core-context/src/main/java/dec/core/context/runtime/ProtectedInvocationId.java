package dec.core.context.runtime;

import java.util.Objects;

/** Opaque protected-access invocation identity; exact and case-sensitive. */
public final class ProtectedInvocationId {
    private final String value;

    private ProtectedInvocationId(String value) {
        this.value = requireExact(value);
    }

    public static ProtectedInvocationId of(String value) {
        return new ProtectedInvocationId(value);
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
