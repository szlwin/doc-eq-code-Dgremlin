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

    public static ProtectedWriteReceipt of(
            ResolvedRuntimeTarget target,
            ModelPath modelPath,
            RuntimeMutationVersion version) {
        return new ProtectedWriteReceipt(target, modelPath, version);
    }

    public ResolvedRuntimeTarget target() { return target; }
    public ModelPath modelPath() { return modelPath; }
    public RuntimeMutationVersion version() { return version; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProtectedWriteReceipt)) return false;
        ProtectedWriteReceipt that = (ProtectedWriteReceipt) other;
        return target.equals(that.target)
                && modelPath.equals(that.modelPath)
                && version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, modelPath, version);
    }
}
