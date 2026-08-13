package dec.core.context.runtime;

import dec.core.context.model.ModelPath;
import java.util.Objects;

/** Guard ALLOW 后交给 MODEL 私有 operation port 的精确 WRITE 事实。 */
public final class ResolvedProtectedWriteAccess {
    private final ResolvedRuntimeTarget target;
    private final ModelPath modelPath;
    private final RuntimeFactValue value;
    private final RuntimeMutationStamp mutationStamp;

    private ResolvedProtectedWriteAccess(
            ResolvedRuntimeTarget target,
            ModelPath modelPath,
            RuntimeFactValue value,
            RuntimeMutationStamp mutationStamp) {
        this.target = Objects.requireNonNull(target, "target");
        this.modelPath = Objects.requireNonNull(modelPath, "modelPath");
        this.value = Objects.requireNonNull(value, "value");
        this.mutationStamp = Objects.requireNonNull(mutationStamp, "mutationStamp");
    }

    /** 冻结同一 target/path/value/stamp；MODEL effect 前仍必须重验 session/object/version。 */
    public static ResolvedProtectedWriteAccess of(
            ResolvedRuntimeTarget target,
            ModelPath modelPath,
            RuntimeFactValue value,
            RuntimeMutationStamp mutationStamp) {
        return new ResolvedProtectedWriteAccess(target, modelPath, value, mutationStamp);
    }

    public ResolvedRuntimeTarget target() {
        return target;
    }

    public ModelPath modelPath() {
        return modelPath;
    }

    public RuntimeFactValue value() {
        return value;
    }

    public RuntimeMutationStamp mutationStamp() {
        return mutationStamp;
    }
}
