package dec.core.model.runtime;

import java.util.Objects;
import java.util.Optional;

/** Provider 绑定结果：成功只携带 scope-private port，失败只携带稳定失败。 */
public final class RuntimeModelEffectBindingResult {
    private final RuntimeModelOperationPort operationPort;
    private final RuntimeModelEffectBindingFailure failure;

    private RuntimeModelEffectBindingResult(
            RuntimeModelOperationPort operationPort,
            RuntimeModelEffectBindingFailure failure) {
        this.operationPort = operationPort;
        this.failure = failure;
    }

    static RuntimeModelEffectBindingResult bound(RuntimeModelOperationPort operationPort) {
        return new RuntimeModelEffectBindingResult(
                Objects.requireNonNull(operationPort, "operationPort"), null);
    }

    static RuntimeModelEffectBindingResult failed(RuntimeModelEffectBindingFailureCode code) {
        return new RuntimeModelEffectBindingResult(
                null, RuntimeModelEffectBindingFailure.of(code));
    }

    public boolean bound() {
        return operationPort != null;
    }

    public Optional<RuntimeModelOperationPort> operationPort() {
        return Optional.ofNullable(operationPort);
    }

    public Optional<RuntimeModelEffectBindingFailure> failure() {
        return Optional.ofNullable(failure);
    }
}
