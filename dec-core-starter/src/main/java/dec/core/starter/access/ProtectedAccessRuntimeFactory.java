package dec.core.starter.access;

import dec.core.context.EngineContext;
import dec.core.model.runtime.RuntimeModelAccessScope;
import java.util.Objects;

/**
 * Production STARTER root; caller may supply only captured EngineContext and MODEL-minted Scope.
 *
 * <p>DEV-P2-DEV07-SKEL-R02 preserves the no-injection boundary: callers cannot provide Guard,
 * RuntimeModelEffectProvider, RuntimeModelOperationPort, or mutable MODEL internals. R31 WRITE value
 * transport remains invocation data and does not expand this factory surface.
 */
public final class ProtectedAccessRuntimeFactory {
    private final EngineContext context;

    private ProtectedAccessRuntimeFactory(EngineContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public static ProtectedAccessRuntimeFactory production(EngineContext context) {
        return new ProtectedAccessRuntimeFactory(context);
    }

    public ProtectedAccessCompositionResult create(RuntimeModelAccessScope scope) {
        Objects.requireNonNull(scope, "scope");
        throw new UnsupportedOperationException("DEV-P2-DEV07-SKEL-R02: concrete composition not installed");
    }

    EngineContext capturedContext() {
        return context;
    }
}
