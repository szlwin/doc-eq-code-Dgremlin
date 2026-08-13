package dec.core.model.runtime;

import java.util.Objects;
import java.util.Optional;

/** scope 只能在至少一个 trusted Handle 已成功加载后产生。 */
public final class RuntimeModelScopeResult {
    private final RuntimeModelAccessScope scope;
    private final RuntimeModelScopeFailure failure;
    private RuntimeModelScopeResult(RuntimeModelAccessScope scope, RuntimeModelScopeFailure failure) { this.scope = scope; this.failure = failure; }
    static RuntimeModelScopeResult available(RuntimeModelAccessScope scope) { return new RuntimeModelScopeResult(Objects.requireNonNull(scope, "scope"), null); }
    static RuntimeModelScopeResult failed(RuntimeModelScopeFailureCode code) { return new RuntimeModelScopeResult(null, RuntimeModelScopeFailure.of(code)); }
    public boolean available() { return scope != null; }
    public Optional<RuntimeModelAccessScope> scope() { return Optional.ofNullable(scope); }
    public Optional<RuntimeModelScopeFailure> failure() { return Optional.ofNullable(failure); }
}
