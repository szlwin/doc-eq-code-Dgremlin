package dec.core.model.runtime;

import java.util.Objects;

/** Session 生命周期失败；只暴露稳定失败码，不泄露 ModelData/Handle 身份。 */
public final class RuntimeModelSessionException extends Exception {
    private static final long serialVersionUID = 1L;
    private final RuntimeModelSessionFailureCode code;

    RuntimeModelSessionException(RuntimeModelSessionFailureCode code) {
        super(Objects.requireNonNull(code, "code").name());
        this.code = code;
    }

    public RuntimeModelSessionFailureCode code() {
        return code;
    }
}
