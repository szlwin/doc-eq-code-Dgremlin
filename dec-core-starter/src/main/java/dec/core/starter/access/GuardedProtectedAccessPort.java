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
 * STARTER guarded core. Construction is package-private so the MODEL operation primitive cannot
 * become a caller-injection seam. Production uses a Guard-authorized effect adapter; the raw-port
 * overload remains only for historical/internal test fixtures and still receives Guard-minted
 * authorization before calling the raw primitive.
 */
final class GuardedProtectedAccessPort implements ProtectedAccessPort {
    private static final AtomicLong WRITE_INTENT_SEQUENCE = new AtomicLong();

    private final ExactModelAccessGuard guard;
    private final RuntimeTargetResolver resolver;
    private final RuntimeModelSession session;
    private final GuardAuthorizedModelEffectPort effectPort;
    private final WriteCoordinationDomain writeCoordination = new WriteCoordinationDomain();

    GuardedProtectedAccessPort(
            EngineContext context,
            RuntimeTargetResolver resolver,
            RuntimeModelSession session,
            GuardAuthorizedModelEffectPort effectPort) {
        this.guard = new ExactModelAccessGuard(Objects.requireNonNull(context, "context"));
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.session = Objects.requireNonNull(session, "session");
        this.effectPort = Objects.requireNonNull(effectPort, "effectPort");
    }

    /** Compatibility constructor for historical test fixtures that own an internal raw MODEL port. */
    GuardedProtectedAccessPort(
            EngineContext context,
            RuntimeTargetResolver resolver,
            RuntimeModelSession session,
            RuntimeModelOperationPort operationPort) {
        this(context, resolver, session, new LegacyRawEffectAdapter(operationPort));
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
            return read(rule, invocation, target);
        }
        if (invocation.modelAccessRuleKey().operation() == AccessOperation.WRITE) {
            return write(rule, invocation, target);
        }
        return deny(invocation, DenialCode.POLICY_MISMATCH, "unsupported model-access operation");
    }

    private ProtectedAccessResult read(
            CompiledModelAccessRule rule,
            ProtectedAccessInvocation invocation,
            ResolvedRuntimeTarget target) {
        ModelEffectAuthorization authorization = guard.authorizeRead(rule, invocation, target);
        if (authorization == null) {
            return deny(invocation, DenialCode.POLICY_MISMATCH, "READ authorization mint denied");
        }
        RuntimeFactValue value = effectPort.read(authorization);
        if (value == null) {
            return deny(invocation, DenialCode.RUNTIME_OBJECT_NOT_FOUND, "runtime READ target unavailable");
        }
        return ProtectedAccessResult.allowRead(
                ProtectedReadValue.of(invocation.invocationId(), value));
    }

    private ProtectedAccessResult write(
            CompiledModelAccessRule rule,
            ProtectedAccessInvocation invocation,
            ResolvedRuntimeTarget target) {
        if (!invocation.writeValue().isPresent()) {
            return deny(invocation, DenialCode.WRITE_INTENT_NOT_FOUND, "WRITE value is required");
        }

        WriteCoordinationDomain.Claim coordination = writeCoordination.tryAcquire(
                target, invocation.modelAccessRuleKey().path());
        if (coordination == null) {
            return deny(
                    invocation,
                    DenialCode.CAPABILITY_ALREADY_CONSUMED,
                    "overlapping WRITE already owns the exact runtime object/path");
        }
        try {
            RuntimeMutationVersion version = session.currentVersion(
                    target, invocation.modelAccessRuleKey().path());
            if (version == null) {
                return deny(invocation, DenialCode.WRITE_INTENT_STALE, "WRITE target version unavailable");
            }
            coordination.freeze(version);

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
                return deny(
                        invocation,
                        DenialCode.CAPABILITY_ALREADY_CONSUMED,
                        "WRITE capability already consumed");
            }

            ModelEffectAuthorization authorization = guard.authorizeWrite(
                    rule, invocation, target, consumed);
            if (authorization == null) {
                return deny(invocation, DenialCode.POLICY_MISMATCH, "WRITE authorization mint denied");
            }
            ProtectedWriteReceipt modelReceipt = effectPort.write(authorization);
            if (modelReceipt == null) {
                return deny(invocation, DenialCode.RUNTIME_WRITE_FAILED, "runtime WRITE failed");
            }
            return ProtectedAccessResult.allowWrite(ProtectedWriteReceipt.of(
                    invocation.invocationId(),
                    writeIntentId,
                    modelReceipt.version()));
        } finally {
            coordination.close();
        }
    }

    private static ProtectedAccessResult deny(
            ProtectedAccessInvocation invocation,
            DenialCode code,
            String message) {
        return ProtectedAccessResult.deny(
                ProtectedAccessDenial.of(invocation.invocationId(), code, message));
    }

    /** Legacy/internal adapter: Guard authorization is consumed before the raw primitive is called. */
    private static final class LegacyRawEffectAdapter implements GuardAuthorizedModelEffectPort {
        private final RuntimeModelOperationPort raw;

        private LegacyRawEffectAdapter(RuntimeModelOperationPort raw) {
            this.raw = Objects.requireNonNull(raw, "raw");
        }

        @Override
        public RuntimeFactValue read(ModelEffectAuthorization authorization) {
            if (authorization == null) {
                return null;
            }
            ModelEffectAuthorization.ReadClaim claim = authorization.consumeRead();
            if (claim == null) {
                return null;
            }
            return raw.read(ResolvedProtectedReadAccess.of(
                    claim.invocationId(), claim.modelAccessRuleKey(), claim.target()));
        }

        @Override
        public ProtectedWriteReceipt write(ModelEffectAuthorization authorization) {
            if (authorization == null) {
                return null;
            }
            ModelEffectAuthorization.WriteClaim claim = authorization.consumeWrite();
            if (claim == null
                    || !claim.modelAccessRuleKey().equals(claim.writeIntent().modelAccessRuleKey())
                    || !claim.target().equals(claim.writeIntent().resolvedRuntimeTarget())) {
                return null;
            }
            return raw.write(ResolvedProtectedWriteAccess.of(
                    claim.invocationId(), claim.writeIntent()));
        }
    }
}
