package dec.core.starter.access;

import dec.core.context.EngineContext;
import dec.core.model.runtime.RuntimeModelAccessScope;
import java.util.Objects;

/**
 * DEV-P2-DEV08-SKEL-R01 production composition skeleton.
 *
 * <p>Frozen call order for concrete implementation:
 * <ol>
 *   <li>Validate the caller supplied only captured EngineContext + MODEL-minted RuntimeModelAccessScope.</li>
 *   <li>Begin exactly one Session from that Scope.</li>
 *   <li>Register every trusted frame Handle into that same Session; any duplicate/ownership/stale failure is terminal.</li>
 *   <li>Freeze one ResolvedRuntimeTarget per registered Handle using the frame id/owner/cursor and handle provenance.</li>
 *   <li>Seal the Session before effect binding.</li>
 *   <li>Bind the Scope-owned RuntimeModelEffectProvider to that exact sealed Session.</li>
 *   <li>Create one STARTER-private GuardedProtectedAccessPort and three consumer entries that all delegate to it.</li>
 *   <li>Publish the composition only after all prior steps succeed; otherwise close the partially-created Session and return a stable failure.</li>
 * </ol>
 *
 * <p>Concurrency boundary is also frozen here: same-capability one-shot remains STARTER-owned; same
 * Handle/path/version mutation serialization/stale rejection remains MODEL-owned. No second global lock,
 * caller-injected operation port, or cross-scope fallback may be introduced.
 */
final class ProductionCompositionCoordinator {
    private ProductionCompositionCoordinator() {
    }

    static ProtectedAccessCompositionResult create(
            EngineContext context,
            RuntimeModelAccessScope scope) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(scope, "scope");
        throw new UnsupportedOperationException(
                "DEV-P2-DEV08-SKEL-R01: production composition algorithm not installed");
    }
}
