package dec.core.context.runtime;

import dec.core.context.model.ModelAccessRuleKey;
import java.util.Objects;
import java.util.Optional;

/** Neutral caller request identity/context. Possession does not grant READ/WRITE authority. */
public final class ProtectedAccessInvocation {
    private final ProtectedInvocationId invocationId;
    private final ModelAccessRuleKey modelAccessRuleKey;
    private final RuntimeExecutionFrameId frameId;
    private final RuntimeResolutionOwnerId ownerResolutionId;
    private final Optional<RuntimeCollectionCursorId> cursorId;
    private final Optional<RuntimeFactValue> writeValue;

    private ProtectedAccessInvocation(
            ProtectedInvocationId invocationId,
            ModelAccessRuleKey modelAccessRuleKey,
            RuntimeExecutionFrameId frameId,
            RuntimeResolutionOwnerId ownerResolutionId,
            Optional<RuntimeCollectionCursorId> cursorId,
            Optional<RuntimeFactValue> writeValue) {
        this.invocationId = Objects.requireNonNull(invocationId, "invocationId");
        this.modelAccessRuleKey = Objects.requireNonNull(modelAccessRuleKey, "modelAccessRuleKey");
        this.frameId = Objects.requireNonNull(frameId, "frameId");
        this.ownerResolutionId = Objects.requireNonNull(ownerResolutionId, "ownerResolutionId");
        this.cursorId = Objects.requireNonNull(cursorId, "cursorId");
        this.writeValue = Objects.requireNonNull(writeValue, "writeValue");
    }

    /** R30/R29 compatibility factory; carries no WRITE replacement value. */
    public static ProtectedAccessInvocation of(
            ProtectedInvocationId invocationId,
            ModelAccessRuleKey modelAccessRuleKey,
            RuntimeExecutionFrameId frameId,
            RuntimeResolutionOwnerId ownerResolutionId,
            Optional<RuntimeCollectionCursorId> cursorId) {
        return new ProtectedAccessInvocation(
                invocationId,
                modelAccessRuleKey,
                frameId,
                ownerResolutionId,
                cursorId,
                Optional.<RuntimeFactValue>empty());
    }

    /** R31 WRITE factory; RuntimeFactValue is data only and does not grant authority. */
    public static ProtectedAccessInvocation write(
            ProtectedInvocationId invocationId,
            ModelAccessRuleKey modelAccessRuleKey,
            RuntimeExecutionFrameId frameId,
            RuntimeResolutionOwnerId ownerResolutionId,
            Optional<RuntimeCollectionCursorId> cursorId,
            RuntimeFactValue writeValue) {
        return new ProtectedAccessInvocation(
                invocationId,
                modelAccessRuleKey,
                frameId,
                ownerResolutionId,
                cursorId,
                Optional.of(Objects.requireNonNull(writeValue, "writeValue")));
    }

    public ProtectedInvocationId invocationId() { return invocationId; }
    public ModelAccessRuleKey modelAccessRuleKey() { return modelAccessRuleKey; }
    public RuntimeExecutionFrameId frameId() { return frameId; }
    public RuntimeResolutionOwnerId ownerResolutionId() { return ownerResolutionId; }
    public Optional<RuntimeCollectionCursorId> cursorId() { return cursorId; }
    public Optional<RuntimeFactValue> writeValue() { return writeValue; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProtectedAccessInvocation)) return false;
        ProtectedAccessInvocation that = (ProtectedAccessInvocation) other;
        return invocationId.equals(that.invocationId)
                && modelAccessRuleKey.equals(that.modelAccessRuleKey)
                && frameId.equals(that.frameId)
                && ownerResolutionId.equals(that.ownerResolutionId)
                && cursorId.equals(that.cursorId)
                && writeValue.equals(that.writeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                invocationId,
                modelAccessRuleKey,
                frameId,
                ownerResolutionId,
                cursorId,
                writeValue);
    }
}
