package dec.core.starter.access;

import dec.core.context.runtime.ProtectedAccessPort;
import dec.core.context.runtime.RuntimeModelSessionId;

/**
 * STARTER-owned protected-access composition boundary.
 *
 * <p>DEV-P2-DEV07-SKEL-R02 freezes the R31/R33 WRITE-value rule: a RuntimeFactValue carried by an
 * invocation is immutable data only, never authority. Concrete orchestration must reject a value-less
 * WRITE before Guard/effect execution and must not retarget or replace the frozen value after intent
 * resolution. Session/provider/Guard/effect orchestration remains intentionally unimplemented here.
 */
public final class ProtectedAccessComposition implements AutoCloseable {
    ProtectedAccessComposition() {
    }

    public ProtectedAccessPort protectedAccessPort() {
        throw unimplemented();
    }

    public RuleProtectedAccessEntry ruleEntry() {
        throw unimplemented();
    }

    public ChangeProtectedAccessEntry changeEntry() {
        throw unimplemented();
    }

    public CustomActionProtectedAccessEntry customActionEntry() {
        throw unimplemented();
    }

    public RuntimeModelSessionId runtimeModelSessionId() {
        throw unimplemented();
    }

    @Override
    public void close() {
        throw unimplemented();
    }

    private static UnsupportedOperationException unimplemented() {
        return new UnsupportedOperationException("DEV-P2-DEV07-SKEL-R02: concrete implementation not installed");
    }
}
