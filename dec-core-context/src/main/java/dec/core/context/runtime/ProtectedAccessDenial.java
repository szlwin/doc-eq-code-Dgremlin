package dec.core.context.runtime;

import java.util.Objects;

/** Stable fail-closed protected-access denial. */
public final class ProtectedAccessDenial {
    private final ProtectedInvocationId invocationId;
    private final DenialCode code;
    private final String stableMessage;

    private ProtectedAccessDenial(
            ProtectedInvocationId invocationId,
            DenialCode code,
            String stableMessage) {
        this.invocationId = Objects.requireNonNull(invocationId, "invocationId");
        this.code = Objects.requireNonNull(code, "code");
        this.stableMessage = requireMessage(stableMessage);
    }

    public static ProtectedAccessDenial of(
            ProtectedInvocationId invocationId,
            DenialCode code,
            String stableMessage) {
        return new ProtectedAccessDenial(invocationId, code, stableMessage);
    }

    public ProtectedInvocationId invocationId() { return invocationId; }
    public DenialCode code() { return code; }
    public String stableMessage() { return stableMessage; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProtectedAccessDenial)) return false;
        ProtectedAccessDenial that = (ProtectedAccessDenial) other;
        return invocationId.equals(that.invocationId)
                && code == that.code
                && stableMessage.equals(that.stableMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invocationId, code, stableMessage);
    }

    private static String requireMessage(String value) {
        Objects.requireNonNull(value, "stableMessage");
        if (value.trim().isEmpty() || !value.equals(value.trim())) {
            throw new IllegalArgumentException("stableMessage must be non-blank and trimmed");
        }
        return value;
    }
}
