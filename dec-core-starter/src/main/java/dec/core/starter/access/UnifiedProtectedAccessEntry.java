package dec.core.starter.access;

import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.ProtectedAccessPort;
import dec.core.context.runtime.ProtectedAccessResult;
import java.util.Objects;

/** One consumer adapter shared by Rule, Change and CustomAction; no consumer-specific bypass exists. */
final class UnifiedProtectedAccessEntry
        implements RuleProtectedAccessEntry, ChangeProtectedAccessEntry, CustomActionProtectedAccessEntry {
    private final ProtectedAccessPort protectedAccessPort;

    UnifiedProtectedAccessEntry(ProtectedAccessPort protectedAccessPort) {
        this.protectedAccessPort = Objects.requireNonNull(protectedAccessPort, "protectedAccessPort");
    }

    @Override
    public ProtectedAccessResult invoke(ProtectedAccessInvocation invocation) {
        return protectedAccessPort.invoke(Objects.requireNonNull(invocation, "invocation"));
    }
}
