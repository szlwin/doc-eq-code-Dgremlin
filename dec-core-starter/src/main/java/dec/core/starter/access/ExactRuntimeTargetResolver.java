package dec.core.starter.access;

import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.runtime.DenialCode;
import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.RuntimeTargetResolution;
import dec.core.context.runtime.RuntimeTargetResolutionStatus;
import dec.core.model.runtime.RuntimeModelSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** STARTER-private exact 0/1/N resolver over targets registered by the composition layer. */
final class ExactRuntimeTargetResolver implements RuntimeTargetResolver {
    private final List<ResolvedRuntimeTarget> candidates;

    ExactRuntimeTargetResolver(Collection<ResolvedRuntimeTarget> candidates) {
        Objects.requireNonNull(candidates, "candidates");
        List<ResolvedRuntimeTarget> copy = new ArrayList<ResolvedRuntimeTarget>(candidates.size());
        for (ResolvedRuntimeTarget candidate : candidates) {
            copy.add(Objects.requireNonNull(candidate, "candidates contains null"));
        }
        this.candidates = Collections.unmodifiableList(copy);
    }

    @Override
    public RuntimeTargetResolution resolve(
            RuntimeBindingPlan plan,
            ProtectedAccessInvocation invocation,
            RuntimeModelSession session) {
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(invocation, "invocation");
        Objects.requireNonNull(session, "session");

        List<ResolvedRuntimeTarget> exact = new ArrayList<ResolvedRuntimeTarget>();
        boolean contextMismatch = false;
        boolean provenanceMismatch = false;

        for (ResolvedRuntimeTarget candidate : candidates) {
            if (!session.sessionId().equals(candidate.sessionId())) {
                continue;
            }
            if (!invocation.modelAccessRuleKey().target().equals(candidate.targetKey())) {
                continue;
            }
            if (!plan.sourceTargetKey().equals(candidate.targetKey())
                    || candidate.bindingProof().runtimeBindingPlan() == null
                    || !plan.equals(candidate.bindingProof().runtimeBindingPlan())
                    || candidate.compiledTargetBinding() == null
                    || !plan.compiledTargetBinding().equals(candidate.compiledTargetBinding())) {
                provenanceMismatch = true;
                continue;
            }
            if (candidate.frameId() == null
                    || candidate.ownerResolutionId() == null
                    || !invocation.frameId().equals(candidate.frameId())
                    || !invocation.ownerResolutionId().equals(candidate.ownerResolutionId())
                    || !invocation.cursorId().equals(candidate.cursorId())) {
                contextMismatch = true;
                continue;
            }
            if (session.locate(candidate) != null) {
                exact.add(candidate);
            }
        }

        if (exact.size() == 1) {
            return RuntimeTargetResolution.resolved(exact.get(0));
        }
        if (exact.size() > 1) {
            return RuntimeTargetResolution.denied(
                    RuntimeTargetResolutionStatus.AMBIGUOUS,
                    DenialCode.RUNTIME_TARGET_AMBIGUOUS);
        }
        if (contextMismatch) {
            return RuntimeTargetResolution.denied(
                    RuntimeTargetResolutionStatus.CONTEXT_MISMATCH,
                    DenialCode.RUNTIME_CONTEXT_MISMATCH);
        }
        if (provenanceMismatch) {
            return RuntimeTargetResolution.denied(
                    RuntimeTargetResolutionStatus.PROVENANCE_MISMATCH,
                    DenialCode.RUNTIME_MODEL_PROVENANCE_MISMATCH);
        }
        return RuntimeTargetResolution.denied(
                RuntimeTargetResolutionStatus.NOT_FOUND,
                DenialCode.RUNTIME_TARGET_NOT_FOUND);
    }
}
