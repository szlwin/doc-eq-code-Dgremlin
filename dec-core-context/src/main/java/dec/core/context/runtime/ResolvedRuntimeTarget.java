package dec.core.context.runtime;

import dec.core.context.model.TargetKey;
import java.util.Objects;

/** resolver 冻结的唯一 runtime target；session/object/target/proof 必须作为一个不可变事实传递。 */
public final class ResolvedRuntimeTarget {
    private final RuntimeModelSessionId sessionId;
    private final RuntimeObjectId runtimeObjectId;
    private final TargetKey targetKey;
    private final RuntimeBindingProof bindingProof;

    private ResolvedRuntimeTarget(
            RuntimeModelSessionId sessionId,
            RuntimeObjectId runtimeObjectId,
            TargetKey targetKey,
            RuntimeBindingProof bindingProof) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.runtimeObjectId = Objects.requireNonNull(runtimeObjectId, "runtimeObjectId");
        this.targetKey = Objects.requireNonNull(targetKey, "targetKey");
        this.bindingProof = Objects.requireNonNull(bindingProof, "bindingProof");
    }

    public static ResolvedRuntimeTarget of(
            RuntimeModelSessionId sessionId,
            RuntimeObjectId runtimeObjectId,
            TargetKey targetKey,
            RuntimeBindingProof bindingProof) {
        return new ResolvedRuntimeTarget(sessionId, runtimeObjectId, targetKey, bindingProof);
    }

    public RuntimeModelSessionId sessionId() { return sessionId; }
    public RuntimeObjectId runtimeObjectId() { return runtimeObjectId; }
    public TargetKey targetKey() { return targetKey; }
    public RuntimeBindingProof bindingProof() { return bindingProof; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ResolvedRuntimeTarget)) return false;
        ResolvedRuntimeTarget that = (ResolvedRuntimeTarget) other;
        return sessionId.equals(that.sessionId)
                && runtimeObjectId.equals(that.runtimeObjectId)
                && targetKey.equals(that.targetKey)
                && bindingProof.equals(that.bindingProof);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, runtimeObjectId, targetKey, bindingProof);
    }
}
