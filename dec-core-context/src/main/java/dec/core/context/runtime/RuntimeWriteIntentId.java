package dec.core.context.runtime;

import java.util.Objects;

/** Opaque resolved write-intent identity; exact and case-sensitive. */
public final class RuntimeWriteIntentId implements Comparable<RuntimeWriteIntentId> {
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

    @Override
    public int compareTo(RuntimeWriteIntentId other) {
        return value.compareTo(Objects.requireNonNull(other, "other").value);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof RuntimeWriteIntentId
                && value.equals(((RuntimeWriteIntentId) other).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    private static String requireExact(String value) {
        Objects.requireNonNull(value, "value");
        if (value.trim().isEmpty() || !value.equals(value.trim())) {
            throw new IllegalArgumentException("value must be non-blank and trimmed");
        }
        return value;
    }
}
