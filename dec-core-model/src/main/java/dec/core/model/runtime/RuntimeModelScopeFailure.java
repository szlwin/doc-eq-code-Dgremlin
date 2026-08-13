package dec.core.model.runtime;

import java.util.Objects;

/** scope failure 不泄露 Handle/ModelData 身份。 */
public final class RuntimeModelScopeFailure {
    private final RuntimeModelScopeFailureCode code;
    private RuntimeModelScopeFailure(RuntimeModelScopeFailureCode code) { this.code = Objects.requireNonNull(code, "code"); }
    public static RuntimeModelScopeFailure of(RuntimeModelScopeFailureCode code) { return new RuntimeModelScopeFailure(code); }
    public RuntimeModelScopeFailureCode code() { return code; }
}
