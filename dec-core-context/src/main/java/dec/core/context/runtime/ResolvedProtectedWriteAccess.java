package dec.core.context.runtime;

import dec.core.context.model.ModelPath;
import java.util.Objects;

/** Guard ALLOW 后交给 MODEL 私有 operation port 的精确 WRITE 事实。 */
public final class ResolvedProtectedWriteAccess {
    private final ProtectedInvocationId invocationId;
    private final ResolvedWriteIntent writeIntent;
    private final ResolvedRuntimeTarget target;
    private final ModelPath modelPath;
    private final RuntimeFactValue value;
    private final RuntimeMutationStamp mutationStamp;

    private ResolvedProtectedWriteAccess(
            ProtectedInvocationId invocationId,
            ResolvedWriteIntent writeIntent,
            ResolvedRuntimeTarget target,
            ModelPath modelPath,
            RuntimeFactValue value,
            RuntimeMutationStamp mutationStamp) {
        this.invocationId = invocationId;
        this.writeIntent = writeIntent;
        this.target = Objects.requireNonNull(target, "target");
        this.modelPath = Objects.requireNonNull(modelPath, "modelPath");
        this.value = value;
        this.mutationStamp = Objects.requireNonNull(mutationStamp, "mutationStamp");
    }

    /** DEV-06 MODEL-compatible seam carrying the concrete replacement value. */
    public static ResolvedProtectedWriteAccess of(
            ResolvedRuntimeTarget target,
            ModelPath modelPath,
            RuntimeFactValue value,
            RuntimeMutationStamp mutationStamp) {
        return new ResolvedProtectedWriteAccess(
                null,
                null,
                target,
                modelPath,
                Objects.requireNonNull(value, "value"),
                mutationStamp);
    }

    /** R30/R31 neutral seam: executable R31 intent projects its already-frozen value. */
    public static ResolvedProtectedWriteAccess of(
            ProtectedInvocationId invocationId,
            ResolvedWriteIntent writeIntent) {
        ResolvedWriteIntent intent = Objects.requireNonNull(writeIntent, "writeIntent");
        return new ResolvedProtectedWriteAccess(
                Objects.requireNonNull(invocationId, "invocationId"),
                intent,
                intent.resolvedRuntimeTarget(),
                intent.modelAccessRuleKey().path(),
                intent.writeValue().orElse(null),
                intent.mutationStamp());
    }

    public ProtectedInvocationId invocationId() { return invocationId; }
    public ResolvedWriteIntent writeIntent() { return writeIntent; }
    public ResolvedRuntimeTarget target() { return target; }
    public ModelPath modelPath() { return modelPath; }
    public RuntimeFactValue value() { return value; }
    public RuntimeMutationStamp mutationStamp() { return mutationStamp; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ResolvedProtectedWriteAccess)) return false;
        ResolvedProtectedWriteAccess that = (ResolvedProtectedWriteAccess) other;
        return Objects.equals(invocationId, that.invocationId)
                && Objects.equals(writeIntent, that.writeIntent)
                && target.equals(that.target)
                && modelPath.equals(that.modelPath)
                && Objects.equals(value, that.value)
                && mutationStamp.equals(that.mutationStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invocationId, writeIntent, target, modelPath, value, mutationStamp);
    }
}
