package dec.core.starter.access;

import dec.core.context.EngineContext;
import dec.core.context.model.CompiledModelAccessRule;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.RuntimeBindingProof;
import dec.core.context.runtime.RuntimeObjectId;
import dec.core.model.runtime.RuntimeModelAccessScope;
import dec.core.model.runtime.RuntimeModelEffectBindingFailureCode;
import dec.core.model.runtime.RuntimeModelEffectBindingResult;
import dec.core.model.runtime.RuntimeModelFrame;
import dec.core.model.runtime.RuntimeModelHandle;
import dec.core.model.runtime.RuntimeModelOperationPort;
import dec.core.model.runtime.RuntimeModelSession;
import dec.core.model.runtime.RuntimeModelSessionException;
import dec.core.model.runtime.RuntimeModelSessionFailureCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** STARTER-private production wiring for one exact trusted Scope/Session/effect chain. */
final class ProductionCompositionCoordinator {
    private ProductionCompositionCoordinator() {
    }

    static ProtectedAccessCompositionResult create(
            EngineContext context,
            RuntimeModelAccessScope scope) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(scope, "scope");

        RuntimeModelFrame frame = scope.frame();
        if (frame.handles().isEmpty()) {
            return failed(ProtectedAccessCompositionFailureCode.SCOPE_STALE);
        }
        for (RuntimeModelHandle handle : frame.handles()) {
            if (!belongsToCapturedContext(context, handle)) {
                return failed(ProtectedAccessCompositionFailureCode.PROVENANCE_MISMATCH);
            }
        }

        RuntimeModelSession session = null;
        try {
            session = scope.beginSession();
            List<ResolvedRuntimeTarget> targets = new ArrayList<ResolvedRuntimeTarget>();
            for (RuntimeModelHandle handle : frame.handles()) {
                RuntimeObjectId objectId = session.register(handle);
                RuntimeBindingPlan plan = handle.provenance().runtimeBindingPlan();
                targets.add(ResolvedRuntimeTarget.of(
                        session.sessionId(),
                        objectId,
                        plan.sourceTargetKey(),
                        plan.compiledTargetBinding(),
                        frame.frameId(),
                        frame.ownerResolutionId(),
                        frame.cursorId(),
                        RuntimeBindingProof.exact(plan)));
            }
            session.seal();

            RuntimeModelEffectBindingResult binding = scope.effectProvider().bind(session);
            if (!binding.bound()) {
                ProtectedAccessCompositionFailureCode failureCode = mapEffectFailure(
                        binding.failure().get().code());
                session.close();
                return failed(failureCode);
            }
            RuntimeModelOperationPort operationPort = binding.operationPort().get();
            GuardedProtectedAccessPort guarded = new GuardedProtectedAccessPort(
                    context,
                    new ExactRuntimeTargetResolver(targets),
                    session,
                    operationPort);
            UnifiedProtectedAccessEntry entry = new UnifiedProtectedAccessEntry(guarded);
            return ProtectedAccessCompositionResult.created(new ProtectedAccessComposition(
                    guarded,
                    entry,
                    entry,
                    entry,
                    session));
        } catch (RuntimeModelSessionException failure) {
            if (session != null) {
                session.close();
            }
            return failed(mapSessionFailure(failure.code()));
        }
    }

    private static boolean belongsToCapturedContext(
            EngineContext context,
            RuntimeModelHandle handle) {
        RuntimeBindingPlan handlePlan = handle.provenance().runtimeBindingPlan();
        for (ModelAccessRuleKey key : context.modelAccessPolicyIndex().keys()) {
            Optional<CompiledModelAccessRule> rule = context.modelAccessPolicyIndex().find(key);
            if (rule.isPresent() && handlePlan.equals(rule.get().runtimeBindingPlan())) {
                return true;
            }
        }
        return false;
    }

    private static ProtectedAccessCompositionFailureCode mapSessionFailure(
            RuntimeModelSessionFailureCode code) {
        switch (code) {
            case SCOPE_INACTIVE:
                return ProtectedAccessCompositionFailureCode.SCOPE_INACTIVE;
            case SESSION_CLOSED:
                return ProtectedAccessCompositionFailureCode.SESSION_CLOSED;
            case SESSION_ALREADY_SEALED:
                return ProtectedAccessCompositionFailureCode.SESSION_ALREADY_SEALED;
            case DUPLICATE_REGISTRATION:
                return ProtectedAccessCompositionFailureCode.SESSION_DUPLICATE_REGISTRATION;
            case OWNERSHIP_CONFLICT:
                return ProtectedAccessCompositionFailureCode.SESSION_OWNERSHIP_CONFLICT;
            default:
                throw new IllegalArgumentException("unsupported session failure: " + code);
        }
    }

    private static ProtectedAccessCompositionFailureCode mapEffectFailure(
            RuntimeModelEffectBindingFailureCode code) {
        switch (code) {
            case SCOPE_INACTIVE:
                return ProtectedAccessCompositionFailureCode.SCOPE_INACTIVE;
            case SESSION_NOT_SEALED:
                return ProtectedAccessCompositionFailureCode.EFFECT_SESSION_NOT_SEALED;
            case SESSION_CLOSED:
                return ProtectedAccessCompositionFailureCode.EFFECT_SESSION_CLOSED;
            case SESSION_SCOPE_MISMATCH:
                return ProtectedAccessCompositionFailureCode.EFFECT_SESSION_SCOPE_MISMATCH;
            default:
                throw new IllegalArgumentException("unsupported effect failure: " + code);
        }
    }

    private static ProtectedAccessCompositionResult failed(
            ProtectedAccessCompositionFailureCode code) {
        return ProtectedAccessCompositionResult.failed(
                ProtectedAccessCompositionFailure.of(code));
    }
}
