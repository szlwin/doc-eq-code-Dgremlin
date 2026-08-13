package dec.core.model.runtime;

import java.util.Objects;

/** MODEL minted cross-module trusted scope；DEV-06 会在已冻结 frame 边界内增加 Session/EffectProvider。 */
public final class RuntimeModelAccessScope {
    private final RuntimeModelFrame frame;
    private boolean active = true;
    RuntimeModelAccessScope(RuntimeModelFrame frame) { this.frame = Objects.requireNonNull(frame, "frame"); }
    public RuntimeModelFrame frame() { return frame; }
    /** root 关闭时由 MODEL 内部失活；caller 不能恢复 scope。 */
    void deactivate() { active = false; }
    boolean active() { return active; }
}
