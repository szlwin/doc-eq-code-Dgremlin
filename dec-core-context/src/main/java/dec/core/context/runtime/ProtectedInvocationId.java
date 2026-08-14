package dec.core.context.runtime;

import java.util.Objects;

/** Opaque protected-access invocation identity; exact and case-sensitive. */
public final class ProtectedInvocationId implements Comparable<ProtectedInvocationId> {
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

    @Override
    public int compareTo(ProtectedInvocationId other) {
        return value.compareTo(Objects.requireNonNull(other, "other").value);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof ProtectedInvocationId
                && value.equals(((ProtectedInvocationId) other).value);
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
