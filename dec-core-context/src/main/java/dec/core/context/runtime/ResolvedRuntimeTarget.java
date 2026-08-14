package dec.core.context.runtime;

import dec.core.context.model.CompiledTargetBinding;
import dec.core.context.model.TargetKey;
import java.util.Objects;
import java.util.Optional;

/** resolver 冻结的唯一 runtime target；session/object/target/proof 必须作为一个不可变事实传递。 */
public final class ResolvedRuntimeTarget {
    private final RuntimeModelSessionId sessionId;
    private final RuntimeObjectId runtimeObjectId;
    private final TargetKey targetKey;
    private final CompiledTargetBinding compiledTargetBinding;
    private final RuntimeExecutionFrameId frameId;
    private final RuntimeResolutionOwnerId ownerResolutionId;
    private final Optional<RuntimeCollectionCursorId> cursorId;
    private final RuntimeBindingProof bindingProof;

    private ResolvedRuntimeTarget(
            RuntimeModelSessionId sessionId,
            RuntimeObjectId runtimeObjectId,
            TargetKey targetKey,
            CompiledTargetBinding compiledTargetBinding,
            RuntimeExecutionFrameId frameId,
            RuntimeResolutionOwnerId ownerResolutionId,
            Optional<RuntimeCollectionCursorId> cursorId,
            RuntimeBindingProof bindingProof) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.runtimeObjectId = Objects.requireNonNull(runtimeObjectId, "runtimeObjectId");
        this.targetKey = Objects.requireNonNull(targetKey, "targetKey");
        this.compiledTargetBinding = compiledTargetBinding;
        this.frameId = frameId;
        this.ownerResolutionId = ownerResolutionId;
        this.cursorId = cursorId == null ? Optional.<RuntimeCollectionCursorId>empty() : cursorId;
        this.bindingProof = Objects.requireNonNull(bindingProof, "bindingProof");
    }

    /** DEV-06 MODEL-compatible construction seam. */
    public static ResolvedRuntimeTarget of(
            RuntimeModelSessionId sessionId,
            RuntimeObjectId runtimeObjectId,
            TargetKey targetKey,
            RuntimeBindingProof bindingProof) {
        CompiledTargetBinding binding = bindingProof.runtimeBindingPlan() == null
                ? null
                : bindingProof.runtimeBindingPlan().compiledTargetBinding();
        return new ResolvedRuntimeTarget(
                sessionId,
                runtimeObjectId,
                targetKey,
                binding,
                null,
                null,
                Optional.<RuntimeCollectionCursorId>empty(),
                bindingProof);
    }

    /** DESIGN-P2-R30 preserved R29 construction seam. */
    public static ResolvedRuntimeTarget of(
            RuntimeModelSessionId sessionId,
            RuntimeObjectId runtimeObjectId,
            TargetKey targetKey,
            CompiledTargetBinding compiledTargetBinding,
            RuntimeExecutionFrameId frameId,
            RuntimeResolutionOwnerId ownerResolutionId,
            Optional<RuntimeCollectionCursorId> cursorId,
            RuntimeBindingProof bindingProof) {
        return new ResolvedRuntimeTarget(
                sessionId,
                runtimeObjectId,
                targetKey,
                Objects.requireNonNull(compiledTargetBinding, "compiledTargetBinding"),
                Objects.requireNonNull(frameId, "frameId"),
                Objects.requireNonNull(ownerResolutionId, "ownerResolutionId"),
                Objects.requireNonNull(cursorId, "cursorId"),
                bindingProof);
    }

    public RuntimeModelSessionId sessionId() { return sessionId; }
    public RuntimeObjectId runtimeObjectId() { return runtimeObjectId; }
    public TargetKey targetKey() { return targetKey; }
    public CompiledTargetBinding compiledTargetBinding() { return compiledTargetBinding; }
    public RuntimeExecutionFrameId frameId() { return frameId; }
    public RuntimeResolutionOwnerId ownerResolutionId() { return ownerResolutionId; }
    public Optional<RuntimeCollectionCursorId> cursorId() { return cursorId; }
    public RuntimeBindingProof bindingProof() { return bindingProof; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ResolvedRuntimeTarget)) return false;
        ResolvedRuntimeTarget that = (ResolvedRuntimeTarget) other;
        return sessionId.equals(that.sessionId)
                && runtimeObjectId.equals(that.runtimeObjectId)
                && targetKey.equals(that.targetKey)
                && Objects.equals(compiledTargetBinding, that.compiledTargetBinding)
                && Objects.equals(frameId, that.frameId)
                && Objects.equals(ownerResolutionId, that.ownerResolutionId)
                && cursorId.equals(that.cursorId)
                && bindingProof.equals(that.bindingProof);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sessionId,
                runtimeObjectId,
                targetKey,
                compiledTargetBinding,
                frameId,
                ownerResolutionId,
                cursorId,
                bindingProof);
    }
}
