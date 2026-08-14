package dec.core.starter.access;

import dec.core.context.EngineContext;
import dec.core.model.runtime.RuntimeModelAccessScope;
import java.util.Objects;

/**
 * Production STARTER root; caller may supply only captured EngineContext and MODEL-minted Scope.
 *
 * <p>DEV-P2-DEV08-SKEL-R01 delegates all Session/effect composition to a STARTER-private coordinator.
 * Caller injection of Guard, RuntimeModelEffectProvider, RuntimeModelOperationPort or mutable MODEL internals
 * remains impossible through this public surface.
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
        return ProductionCompositionCoordinator.create(context, Objects.requireNonNull(scope, "scope"));
    }

    EngineContext capturedContext() {
        return context;
    }
}
