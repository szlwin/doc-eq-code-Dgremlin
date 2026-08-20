package dec.core.context.runtime;

import dec.core.context.model.ModelPath;
import java.util.Objects;

/** WRITE intent 冻结的 session/object/path/version 原子事实。 */
public final class RuntimeMutationStamp {
    private final RuntimeModelSessionId sessionId;
    private final RuntimeObjectId runtimeObjectId;
    private final ModelPath modelPath;
    private final RuntimeMutationVersion version;

    private RuntimeMutationStamp(
            RuntimeModelSessionId sessionId,
            RuntimeObjectId runtimeObjectId,
            ModelPath modelPath,
            RuntimeMutationVersion version) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.runtimeObjectId = Objects.requireNonNull(runtimeObjectId, "runtimeObjectId");
        this.modelPath = Objects.requireNonNull(modelPath, "modelPath");
        this.version = Objects.requireNonNull(version, "version");
    }

    public static RuntimeMutationStamp of(
            RuntimeModelSessionId sessionId,
            RuntimeObjectId runtimeObjectId,
            ModelPath modelPath,
            RuntimeMutationVersion version) {
        return new RuntimeMutationStamp(sessionId, runtimeObjectId, modelPath, version);
    }

    public RuntimeModelSessionId sessionId() { return sessionId; }
    public RuntimeObjectId runtimeObjectId() { return runtimeObjectId; }
    public ModelPath modelPath() { return modelPath; }
    public RuntimeMutationVersion version() { return version; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof RuntimeMutationStamp)) return false;
        RuntimeMutationStamp that = (RuntimeMutationStamp) other;
        return sessionId.equals(that.sessionId)
                && runtimeObjectId.equals(that.runtimeObjectId)
                && modelPath.equals(that.modelPath)
                && version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, runtimeObjectId, modelPath, version);
    }
}
