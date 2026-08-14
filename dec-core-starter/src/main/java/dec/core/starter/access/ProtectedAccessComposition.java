package dec.core.starter.access;

import dec.core.context.runtime.ProtectedAccessPort;
import dec.core.context.runtime.RuntimeModelSessionId;
import dec.core.model.runtime.RuntimeModelSession;
import java.util.Objects;

/**
 * STARTER-owned protected-access composition boundary.
 *
 * <p>DEV-P2-DEV08-SKEL-R01 freezes one shared guarded port, consumer-entry parity and one exact MODEL
 * Session identity. Concrete close/stale lifecycle remains intentionally unimplemented until the skeleton
 * review gate passes.
 */
public final class ProtectedAccessComposition implements AutoCloseable {
    private final ProtectedAccessPort protectedAccessPort;
    private final RuleProtectedAccessEntry ruleEntry;
    private final ChangeProtectedAccessEntry changeEntry;
    private final CustomActionProtectedAccessEntry customActionEntry;
    private final RuntimeModelSession session;

    ProtectedAccessComposition(
            ProtectedAccessPort protectedAccessPort,
            RuleProtectedAccessEntry ruleEntry,
            ChangeProtectedAccessEntry changeEntry,
            CustomActionProtectedAccessEntry customActionEntry,
            RuntimeModelSession session) {
        this.protectedAccessPort = Objects.requireNonNull(protectedAccessPort, "protectedAccessPort");
        this.ruleEntry = Objects.requireNonNull(ruleEntry, "ruleEntry");
        this.changeEntry = Objects.requireNonNull(changeEntry, "changeEntry");
        this.customActionEntry = Objects.requireNonNull(customActionEntry, "customActionEntry");
        this.session = Objects.requireNonNull(session, "session");
    }

    public ProtectedAccessPort protectedAccessPort() {
        return protectedAccessPort;
    }

    public RuleProtectedAccessEntry ruleEntry() {
        return ruleEntry;
    }

    public ChangeProtectedAccessEntry changeEntry() {
        return changeEntry;
    }

    public CustomActionProtectedAccessEntry customActionEntry() {
        return customActionEntry;
    }

    public RuntimeModelSessionId runtimeModelSessionId() {
        return session.sessionId();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException(
                "DEV-P2-DEV08-SKEL-R01: composition close/stale lifecycle not installed");
    }
}
