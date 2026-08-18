package dec.core.model.runtime;

import java.util.Objects;
import java.util.Optional;

/** Provider 绑定结果：production 只暴露 guarded adapter；legacy/internal scope 可保留 raw port。 */
public final class RuntimeModelEffectBindingResult {
    private final RuntimeModelOperationPort operationPort;
    private final RuntimeModelGuardedOperationPort guardedOperationPort;
    private final boolean exposeRawOperationPort;
    private final RuntimeModelEffectBindingFailure failure;

    private RuntimeModelEffectBindingResult(
            RuntimeModelOperationPort operationPort,
            RuntimeModelGuardedOperationPort guardedOperationPort,
            boolean exposeRawOperationPort,
            RuntimeModelEffectBindingFailure failure) {
        this.operationPort = operationPort;
        this.guardedOperationPort = guardedOperationPort;
        this.exposeRawOperationPort = exposeRawOperationPort;
        this.failure = failure;
    }

    static RuntimeModelEffectBindingResult bound(
            RuntimeModelOperationPort operationPort,
            RuntimeModelGuardedOperationPort guardedOperationPort,
            boolean exposeRawOperationPort) {
        return new RuntimeModelEffectBindingResult(
                Objects.requireNonNull(operationPort, "operationPort"),
                Objects.requireNonNull(guardedOperationPort, "guardedOperationPort"),
                exposeRawOperationPort,
                null);
    }

    static RuntimeModelEffectBindingResult failed(RuntimeModelEffectBindingFailureCode code) {
        return new RuntimeModelEffectBindingResult(
                null, null, false, RuntimeModelEffectBindingFailure.of(code));
    }

    public boolean bound() {
        return operationPort != null;
    }

    /**
     * Compatibility-only raw extraction. Production scopes intentionally return Optional.empty().
     * MODEL package tests/legacy internal scopes keep the historical raw seam.
     */
    public Optional<RuntimeModelOperationPort> operationPort() {
        return exposeRawOperationPort ? Optional.ofNullable(operationPort) : Optional.empty();
    }

    /** Package-private guarded primitive consumed only by STARTER's friend bridge. */
    RuntimeModelGuardedOperationPort guardedOperationPort() {
        return guardedOperationPort;
    }

    public Optional<RuntimeModelEffectBindingFailure> failure() {
        return Optional.ofNullable(failure);
    }
}
