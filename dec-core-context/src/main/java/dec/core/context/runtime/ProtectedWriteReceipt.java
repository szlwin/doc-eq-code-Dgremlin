package dec.core.context.runtime;

import dec.core.context.model.ModelPath;
import java.util.Objects;

/** 只有真实 WRITE effect 成功后才能创建的不可变 receipt。 */
public final class ProtectedWriteReceipt {
    private final ResolvedRuntimeTarget target;
    private final ModelPath modelPath;
    private final RuntimeMutationVersion version;

    private ProtectedWriteReceipt(
            ResolvedRuntimeTarget target,
            ModelPath modelPath,
            RuntimeMutationVersion version) {
        this.target = Objects.requireNonNull(target, "target");
        this.modelPath = Objects.requireNonNull(modelPath, "modelPath");
        this.version = Objects.requireNonNull(version, "version");
    }

    /** 冻结已经提交成功后的目标、路径和新 mutation 版本。 */
    public static ProtectedWriteReceipt of(
            ResolvedRuntimeTarget target,
            ModelPath modelPath,
            RuntimeMutationVersion version) {
        return new ProtectedWriteReceipt(target, modelPath, version);
    }

    public ResolvedRuntimeTarget target() {
        return target;
    }

    public ModelPath modelPath() {
        return modelPath;
    }

    public RuntimeMutationVersion version() {
        return version;
    }
}
