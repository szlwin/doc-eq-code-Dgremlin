package dec.core.starter.access;

import dec.core.context.EngineContext;
import dec.core.context.model.AccessOperation;
import dec.core.context.model.CompiledModelAccessRule;
import dec.core.context.runtime.DenialCode;
import dec.core.context.runtime.ProtectedAccessDenial;
import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.ProtectedAccessPort;
import dec.core.context.runtime.ProtectedAccessResult;
import dec.core.context.runtime.ProtectedReadValue;
import dec.core.context.runtime.ProtectedWriteReceipt;
import dec.core.context.runtime.ResolvedProtectedReadAccess;
import dec.core.context.runtime.ResolvedProtectedWriteAccess;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.ResolvedWriteIntent;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.context.runtime.RuntimeMutationStamp;
import dec.core.context.runtime.RuntimeMutationVersion;
import dec.core.context.runtime.RuntimeTargetResolution;
import dec.core.context.runtime.RuntimeWriteIntentId;
import dec.core.model.runtime.RuntimeModelOperationPort;
import dec.core.model.runtime.RuntimeModelSession;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DEV-07 guarded core. Construction is package-private so the MODEL operation port cannot become a
 * caller-injection seam; DEV-08 production composition is responsible for obtaining/binding it.
 */
final class GuardedProtectedAccessPort implements ProtectedAccessPort {
    private static final AtomicLong WRITE_INTENT_SEQUENCE = new AtomicLong();

    private final ExactModelAccessGuard guard;
    private final RuntimeTargetResolver resolver;
    private final RuntimeModelSession session;
    private final RuntimeModelOperationPort operationPort;

    GuardedProtectedAccessPort(
            EngineContext context,
            RuntimeTargetResolver resolver,
            RuntimeModelSession session,
            RuntimeModelOperationPort operationPort) {
        this.guard = new ExactModelAccessGuard(Objects.requireNonNull(context, "context"));
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.session = Objects.requireNonNull(session, "session");
        this.operationPort = Objects.requireNonNull(operationPort, "operationPort");
    }

    @Override
    public ProtectedAccessResult invoke(ProtectedAccessInvocation invocation) {
        Objects.requireNonNull(invocation, "invocation");
        Optional<CompiledModelAccessRule> found = guard.exactRule(invocation);
        if (!found.isPresent()) {
            return deny(invocation, DenialCode.POLICY_NOT_FOUND, "exact model-access policy not found");
        }
        CompiledModelAccessRule rule = found.get();
        RuntimeTargetResolution resolution = resolver.resolve(
                rule.runtimeBindingPlan(), invocation, session);
        if (!resolution.target().isPresent()) {
            DenialCode code = resolution.denialCode().orElse(DenialCode.RUNTIME_TARGET_NOT_FOUND);
            return deny(invocation, code, "runtime target resolution denied");
        }
        ResolvedRuntimeTarget target = resolution.target().get();
        DenialCode guardDenial = guard.denial(rule, invocation, target);
        if (guardDenial != null) {
            return deny(invocation, guardDenial, "exact model-access guard denied");
        }

        if (invocation.modelAccessRuleKey().operation() == AccessOperation.READ) {
            return read(invocation, target);
        }
        if (invocation.modelAccessRuleKey().operation() == AccessOperation.WRITE) {
            return write(invocation, target);
        }
        return deny(invocation, DenialCode.POLICY_MISMATCH, "unsupported model-access operation");
    }

    private ProtectedAccessResult read(
            ProtectedAccessInvocation invocation,
            ResolvedRuntimeTarget target) {
        RuntimeFactValue value = operationPort.read(ResolvedProtectedReadAccess.of(
                invocation.invocationId(), invocation.modelAccessRuleKey(), target));
        if (value == null) {
            return deny(invocation, DenialCode.RUNTIME_OBJECT_NOT_FOUND, "runtime READ target unavailable");
        }
        return ProtectedAccessResult.allowRead(
                ProtectedReadValue.of(invocation.invocationId(), value));
    }

    private ProtectedAccessResult write(
            ProtectedAccessInvocation invocation,
            ResolvedRuntimeTarget target) {
        if (!invocation.writeValue().isPresent()) {
            return deny(invocation, DenialCode.WRITE_INTENT_NOT_FOUND, "WRITE value is required");
        }
        RuntimeMutationVersion version = session.currentVersion(
                target, invocation.modelAccessRuleKey().path());
        if (version == null) {
            return deny(invocation, DenialCode.WRITE_INTENT_STALE, "WRITE target version unavailable");
        }
        RuntimeMutationStamp stamp = RuntimeMutationStamp.of(
                target.sessionId(),
                target.runtimeObjectId(),
                invocation.modelAccessRuleKey().path(),
                version);
        RuntimeWriteIntentId writeIntentId = RuntimeWriteIntentId.of(
                "starter-write-" + WRITE_INTENT_SEQUENCE.incrementAndGet());
        ResolvedWriteIntent frozenIntent = ResolvedWriteIntent.of(
                writeIntentId,
                invocation.modelAccessRuleKey(),
                Optional.empty(),
                target,
                stamp,
                invocation.writeValue().get());
        OneShotWriteCapability capability = new OneShotWriteCapability(frozenIntent);
        ResolvedWriteIntent consumed = capability.consume();
        if (consumed == null) {
            return deny(invocation, DenialCode.CAPABILITY_ALREADY_CONSUMED, "WRITE capability already consumed");
        }
        ProtectedWriteReceipt modelReceipt = operationPort.write(
                ResolvedProtectedWriteAccess.of(invocation.invocationId(), consumed));
        if (modelReceipt == null) {
            return deny(invocation, DenialCode.RUNTIME_WRITE_FAILED, "runtime WRITE failed");
        }
        return ProtectedAccessResult.allowWrite(ProtectedWriteReceipt.of(
                invocation.invocationId(),
                writeIntentId,
                modelReceipt.version()));
    }

    private static ProtectedAccessResult deny(
            ProtectedAccessInvocation invocation,
            DenialCode code,
            String message) {
        return ProtectedAccessResult.deny(
                ProtectedAccessDenial.of(invocation.invocationId(), code, message));
    }
}
