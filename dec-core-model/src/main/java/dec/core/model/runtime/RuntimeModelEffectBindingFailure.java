package dec.core.model.runtime;

import java.util.Objects;

/** effect binding 失败只暴露稳定原因；失败时 operation port 必须为空。 */
public final class RuntimeModelEffectBindingFailure {
    private final RuntimeModelEffectBindingFailureCode code;

    private RuntimeModelEffectBindingFailure(RuntimeModelEffectBindingFailureCode code) {
        this.code = Objects.requireNonNull(code, "code");
    }

    public static RuntimeModelEffectBindingFailure of(RuntimeModelEffectBindingFailureCode code) {
        return new RuntimeModelEffectBindingFailure(code);
    }

    public RuntimeModelEffectBindingFailureCode code() {
        return code;
    }
}
