package dec.core.starter.access;

import dec.core.context.runtime.ProtectedAccessPort;
import dec.core.context.runtime.RuntimeModelSessionId;
import dec.core.model.runtime.RuntimeModelSession;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** STARTER-owned protected-access composition sharing one guarded port and one exact MODEL Session. */
public final class ProtectedAccessComposition implements AutoCloseable {
    private final ProtectedAccessPort protectedAccessPort;
    private final RuleProtectedAccessEntry ruleEntry;
    private final ChangeProtectedAccessEntry changeEntry;
    private final CustomActionProtectedAccessEntry customActionEntry;
    private final RuntimeModelSession session;
    private final AtomicBoolean closed = new AtomicBoolean();

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
        if (closed.compareAndSet(false, true)) {
            session.close();
        }
    }

    boolean closed() {
        return closed.get();
    }
}
