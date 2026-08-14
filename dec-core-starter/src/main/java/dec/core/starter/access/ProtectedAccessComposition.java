package dec.core.starter.access;

import dec.core.context.runtime.ProtectedAccessPort;
import dec.core.context.runtime.RuntimeModelSessionId;

/**
 * STARTER-owned protected-access composition boundary.
 * Concrete session/provider/Guard/effect orchestration is intentionally unimplemented in the skeleton.
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
        return new UnsupportedOperationException("DEV-07 concrete implementation not installed");
    }
}
