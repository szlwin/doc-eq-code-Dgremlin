package dec.core.context.runtime;

import dec.core.context.model.ModelPath;
import java.util.Objects;

/** Guard ALLOW 后交给 MODEL 私有 operation port 的精确 READ 事实。 */
public final class ResolvedProtectedReadAccess {
    private final ResolvedRuntimeTarget target;
    private final ModelPath modelPath;

    private ResolvedProtectedReadAccess(ResolvedRuntimeTarget target, ModelPath modelPath) {
        this.target = Objects.requireNonNull(target, "target");
        this.modelPath = Objects.requireNonNull(modelPath, "modelPath");
    }

    /** 冻结同一 resolver target 和已授权精确路径；不允许运行时 wildcard/fallback。 */
    public static ResolvedProtectedReadAccess of(ResolvedRuntimeTarget target, ModelPath modelPath) {
        return new ResolvedProtectedReadAccess(target, modelPath);
    }

    public ResolvedRuntimeTarget target() {
        return target;
    }

    public ModelPath modelPath() {
        return modelPath;
    }
}
