package dec.core.model.runtime;

import dec.core.context.runtime.RuntimeCollectionCursorId;
import dec.core.context.runtime.RuntimeExecutionFrameId;
import dec.core.context.runtime.RuntimeResolutionOwnerId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** MODEL root mint 的 trusted frame；frame/owner/cursor 都不能由 caller 自报。 */
public final class RuntimeModelFrame {
    private final RuntimeExecutionFrameId frameId;
    private final RuntimeResolutionOwnerId ownerResolutionId;
    private final RuntimeCollectionCursorId cursorId;
    private final List<RuntimeModelHandle> handles;
    RuntimeModelFrame(RuntimeExecutionFrameId frameId, RuntimeResolutionOwnerId ownerResolutionId, RuntimeCollectionCursorId cursorId, List<RuntimeModelHandle> handles) {
        this.frameId = Objects.requireNonNull(frameId, "frameId");
        this.ownerResolutionId = Objects.requireNonNull(ownerResolutionId, "ownerResolutionId");
        this.cursorId = cursorId;
        this.handles = Collections.unmodifiableList(new ArrayList<RuntimeModelHandle>(Objects.requireNonNull(handles, "handles")));
    }
    public RuntimeExecutionFrameId frameId() { return frameId; }
    public RuntimeResolutionOwnerId ownerResolutionId() { return ownerResolutionId; }
    public Optional<RuntimeCollectionCursorId> cursorId() { return Optional.ofNullable(cursorId); }
    public List<RuntimeModelHandle> handles() { return handles; }
}
