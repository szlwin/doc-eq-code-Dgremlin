package dec.core.context.runtime;

/** Neutral protected-access entry port. Authorization remains a STARTER responsibility. */
public interface ProtectedAccessPort {
    ProtectedAccessResult invoke(ProtectedAccessInvocation invocation);
}
