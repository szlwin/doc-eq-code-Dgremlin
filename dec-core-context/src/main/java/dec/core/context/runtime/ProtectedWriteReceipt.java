package dec.core.context.runtime;

import dec.core.context.model.ModelPath;
import java.util.Objects;

/** 只有真实 WRITE effect 成功后才能创建的不可变 receipt。 */
public final class ProtectedWriteReceipt {
    private final ProtectedInvocationId invocationId;
    private final RuntimeWriteIntentId writeIntentId;
    private final ResolvedRuntimeTarget target;
    private final ModelPath modelPath;
    private final RuntimeMutationVersion version;

    private ProtectedWriteReceipt(
            ProtectedInvocationId invocationId,
            RuntimeWriteIntentId writeIntentId,
            ResolvedRuntimeTarget target,
            ModelPath modelPath,
            RuntimeMutationVersion version) {
        this.invocationId = invocationId;
        this.writeIntentId = writeIntentId;
        this.target = target;
        this.modelPath = modelPath;
        this.version = Objects.requireNonNull(version, "version");
    }

    /** DEV-06 MODEL-compatible receipt carrying exact target/path/new version. */
    public static ProtectedWriteReceipt of(
            ResolvedRuntimeTarget target,
            ModelPath modelPath,
            RuntimeMutationVersion version) {
        return new ProtectedWriteReceipt(
                null,
                null,
                Objects.requireNonNull(target, "target"),
                Objects.requireNonNull(modelPath, "modelPath"),
                version);
    }

    /** DESIGN-P2-R30 preserved R29 consumer receipt. */
    public static ProtectedWriteReceipt of(
            ProtectedInvocationId invocationId,
            RuntimeWriteIntentId writeIntentId,
            RuntimeMutationVersion committedVersion) {
        return new ProtectedWriteReceipt(
                Objects.requireNonNull(invocationId, "invocationId"),
                Objects.requireNonNull(writeIntentId, "writeIntentId"),
                null,
                null,
                committedVersion);
    }

    public ProtectedInvocationId invocationId() { return invocationId; }
    public RuntimeWriteIntentId writeIntentId() { return writeIntentId; }
    public RuntimeMutationVersion committedVersion() { return version; }
    public ResolvedRuntimeTarget target() { return target; }
    public ModelPath modelPath() { return modelPath; }
    public RuntimeMutationVersion version() { return version; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProtectedWriteReceipt)) return false;
        ProtectedWriteReceipt that = (ProtectedWriteReceipt) other;
        return Objects.equals(invocationId, that.invocationId)
                && Objects.equals(writeIntentId, that.writeIntentId)
                && Objects.equals(target, that.target)
                && Objects.equals(modelPath, that.modelPath)
                && version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invocationId, writeIntentId, target, modelPath, version);
    }
}
