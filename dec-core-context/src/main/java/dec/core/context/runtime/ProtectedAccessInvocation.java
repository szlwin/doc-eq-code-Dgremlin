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

    private ProtectedAccessInvocation(
            ProtectedInvocationId invocationId,
            ModelAccessRuleKey modelAccessRuleKey,
            RuntimeExecutionFrameId frameId,
            RuntimeResolutionOwnerId ownerResolutionId,
            Optional<RuntimeCollectionCursorId> cursorId) {
        this.invocationId = Objects.requireNonNull(invocationId, "invocationId");
        this.modelAccessRuleKey = Objects.requireNonNull(modelAccessRuleKey, "modelAccessRuleKey");
        this.frameId = Objects.requireNonNull(frameId, "frameId");
        this.ownerResolutionId = Objects.requireNonNull(ownerResolutionId, "ownerResolutionId");
        this.cursorId = Objects.requireNonNull(cursorId, "cursorId");
    }

    public static ProtectedAccessInvocation of(
            ProtectedInvocationId invocationId,
            ModelAccessRuleKey modelAccessRuleKey,
            RuntimeExecutionFrameId frameId,
            RuntimeResolutionOwnerId ownerResolutionId,
            Optional<RuntimeCollectionCursorId> cursorId) {
        return new ProtectedAccessInvocation(
                invocationId, modelAccessRuleKey, frameId, ownerResolutionId, cursorId);
    }

    public ProtectedInvocationId invocationId() { return invocationId; }
    public ModelAccessRuleKey modelAccessRuleKey() { return modelAccessRuleKey; }
    public RuntimeExecutionFrameId frameId() { return frameId; }
    public RuntimeResolutionOwnerId ownerResolutionId() { return ownerResolutionId; }
    public Optional<RuntimeCollectionCursorId> cursorId() { return cursorId; }
}
