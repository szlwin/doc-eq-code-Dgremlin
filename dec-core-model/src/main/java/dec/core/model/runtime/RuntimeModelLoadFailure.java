package dec.core.model.runtime;

import java.util.Objects;

/** 不泄露 ModelData/origin 身份的稳定 MODEL load failure。 */
public final class RuntimeModelLoadFailure {
    private final RuntimeModelLoadFailureCode code;
    private RuntimeModelLoadFailure(RuntimeModelLoadFailureCode code) { this.code = Objects.requireNonNull(code, "code"); }
    public static RuntimeModelLoadFailure of(RuntimeModelLoadFailureCode code) { return new RuntimeModelLoadFailure(code); }
    public RuntimeModelLoadFailureCode code() { return code; }
}
