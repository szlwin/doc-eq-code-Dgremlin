package dec.core.context.runtime;

import java.util.Objects;

/** Immutable successful protected READ result. */
public final class ProtectedReadValue {
    private final ProtectedInvocationId invocationId;
    private final RuntimeFactValue value;

    private ProtectedReadValue(ProtectedInvocationId invocationId, RuntimeFactValue value) {
        this.invocationId = Objects.requireNonNull(invocationId, "invocationId");
        this.value = Objects.requireNonNull(value, "value");
    }

    public static ProtectedReadValue of(ProtectedInvocationId invocationId, RuntimeFactValue value) {
        return new ProtectedReadValue(invocationId, value);
    }

    public ProtectedInvocationId invocationId() { return invocationId; }
    public RuntimeFactValue value() { return value; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProtectedReadValue)) return false;
        ProtectedReadValue that = (ProtectedReadValue) other;
        return invocationId.equals(that.invocationId) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invocationId, value);
    }
}
