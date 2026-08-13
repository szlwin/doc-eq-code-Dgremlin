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

    /** 把 Guard 前观察到的精确 mutation 版本冻结到同一目标和路径。 */
    public static RuntimeMutationStamp of(
            RuntimeModelSessionId sessionId,
            RuntimeObjectId runtimeObjectId,
            ModelPath modelPath,
            RuntimeMutationVersion version) {
        return new RuntimeMutationStamp(sessionId, runtimeObjectId, modelPath, version);
    }

    public RuntimeModelSessionId sessionId() {
        return sessionId;
    }

    public RuntimeObjectId runtimeObjectId() {
        return runtimeObjectId;
    }

    public ModelPath modelPath() {
        return modelPath;
    }

    public RuntimeMutationVersion version() {
        return version;
    }
}
