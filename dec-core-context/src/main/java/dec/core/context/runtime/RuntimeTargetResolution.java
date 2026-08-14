package dec.core.context.runtime;

import java.util.Objects;
import java.util.Optional;

/** Closed target-resolution result; denial never carries a resolved target. */
public final class RuntimeTargetResolution {
    private final RuntimeTargetResolutionStatus status;
    private final ResolvedRuntimeTarget target;
    private final DenialCode denialCode;

    private RuntimeTargetResolution(
            RuntimeTargetResolutionStatus status,
            ResolvedRuntimeTarget target,
            DenialCode denialCode) {
        this.status = Objects.requireNonNull(status, "status");
        this.target = target;
        this.denialCode = denialCode;
    }

    public static RuntimeTargetResolution resolved(ResolvedRuntimeTarget target) {
        return new RuntimeTargetResolution(
                RuntimeTargetResolutionStatus.RESOLVED,
                Objects.requireNonNull(target, "target"),
                null);
    }

    public static RuntimeTargetResolution denied(
            RuntimeTargetResolutionStatus status,
            DenialCode denialCode) {
        Objects.requireNonNull(status, "status");
        if (status == RuntimeTargetResolutionStatus.RESOLVED) {
            throw new IllegalArgumentException("denied status cannot be RESOLVED");
        }
        return new RuntimeTargetResolution(
                status, null, Objects.requireNonNull(denialCode, "denialCode"));
    }

    public RuntimeTargetResolutionStatus status() { return status; }
    public Optional<ResolvedRuntimeTarget> target() { return Optional.ofNullable(target); }
    public Optional<DenialCode> denialCode() { return Optional.ofNullable(denialCode); }
}
