package dec.core.context.runtime;

import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.ModelPath;
import java.util.Objects;

/** Guard ALLOW 后交给 MODEL 私有 operation port 的精确 READ 事实。 */
public final class ResolvedProtectedReadAccess {
    private final ProtectedInvocationId invocationId;
    private final ModelAccessRuleKey modelAccessRuleKey;
    private final ResolvedRuntimeTarget target;
    private final ModelPath modelPath;

    private ResolvedProtectedReadAccess(
            ProtectedInvocationId invocationId,
            ModelAccessRuleKey modelAccessRuleKey,
            ResolvedRuntimeTarget target,
            ModelPath modelPath) {
        this.invocationId = invocationId;
        this.modelAccessRuleKey = modelAccessRuleKey;
        this.target = Objects.requireNonNull(target, "target");
        this.modelPath = Objects.requireNonNull(modelPath, "modelPath");
    }

    /** DEV-06 MODEL-compatible seam. */
    public static ResolvedProtectedReadAccess of(ResolvedRuntimeTarget target, ModelPath modelPath) {
        return new ResolvedProtectedReadAccess(null, null, target, modelPath);
    }

    /** DESIGN-P2-R30 preserved R29 seam. */
    public static ResolvedProtectedReadAccess of(
            ProtectedInvocationId invocationId,
            ModelAccessRuleKey modelAccessRuleKey,
            ResolvedRuntimeTarget target) {
        ModelAccessRuleKey key = Objects.requireNonNull(modelAccessRuleKey, "modelAccessRuleKey");
        return new ResolvedProtectedReadAccess(
                Objects.requireNonNull(invocationId, "invocationId"),
                key,
                target,
                key.path());
    }

    public ProtectedInvocationId invocationId() { return invocationId; }
    public ModelAccessRuleKey modelAccessRuleKey() { return modelAccessRuleKey; }
    public ResolvedRuntimeTarget resolvedRuntimeTarget() { return target; }
    public ResolvedRuntimeTarget target() { return target; }
    public ModelPath modelPath() { return modelPath; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ResolvedProtectedReadAccess)) return false;
        ResolvedProtectedReadAccess that = (ResolvedProtectedReadAccess) other;
        return Objects.equals(invocationId, that.invocationId)
                && Objects.equals(modelAccessRuleKey, that.modelAccessRuleKey)
                && target.equals(that.target)
                && modelPath.equals(that.modelPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invocationId, modelAccessRuleKey, target, modelPath);
    }
}
