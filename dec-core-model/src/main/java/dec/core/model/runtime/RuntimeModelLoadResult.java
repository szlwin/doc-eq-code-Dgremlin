package dec.core.model.runtime;

import java.util.Objects;
import java.util.Optional;

/** load 成功只返回 MODEL minted Handle；失败只返回稳定失败码。 */
public final class RuntimeModelLoadResult {
    private final RuntimeModelHandle handle;
    private final RuntimeModelLoadFailure failure;
    private RuntimeModelLoadResult(RuntimeModelHandle handle, RuntimeModelLoadFailure failure) { this.handle = handle; this.failure = failure; }
    static RuntimeModelLoadResult loaded(RuntimeModelHandle handle) { return new RuntimeModelLoadResult(Objects.requireNonNull(handle, "handle"), null); }
    static RuntimeModelLoadResult failed(RuntimeModelLoadFailureCode code) { return new RuntimeModelLoadResult(null, RuntimeModelLoadFailure.of(code)); }
    public boolean loaded() { return handle != null; }
    public Optional<RuntimeModelHandle> handle() { return Optional.ofNullable(handle); }
    public Optional<RuntimeModelLoadFailure> failure() { return Optional.ofNullable(failure); }
}
