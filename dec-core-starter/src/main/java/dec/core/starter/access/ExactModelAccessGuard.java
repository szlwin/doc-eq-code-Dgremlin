package dec.core.starter.access;

import dec.core.context.EngineContext;
import dec.core.context.model.CompiledModelAccessRule;
import dec.core.context.runtime.DenialCode;
import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import java.util.Objects;
import java.util.Optional;

/** STARTER-private Guard; authorization is keyed only by the exact ModelAccessRuleKey. */
final class ExactModelAccessGuard {
    private final EngineContext context;

    ExactModelAccessGuard(EngineContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    Optional<CompiledModelAccessRule> exactRule(ProtectedAccessInvocation invocation) {
        Objects.requireNonNull(invocation, "invocation");
        return context.modelAccessPolicyIndex().find(invocation.modelAccessRuleKey());
    }

    /** Returns null only when the exact published rule and the resolved runtime proof are identical. */
    DenialCode denial(
            CompiledModelAccessRule rule,
            ProtectedAccessInvocation invocation,
            ResolvedRuntimeTarget target) {
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(invocation, "invocation");
        Objects.requireNonNull(target, "target");
        if (!rule.key().equals(invocation.modelAccessRuleKey())) {
            return DenialCode.POLICY_MISMATCH;
        }
        if (!rule.key().target().equals(target.targetKey())) {
            return DenialCode.POLICY_MISMATCH;
        }
        if (target.bindingProof().runtimeBindingPlan() == null
                || !rule.runtimeBindingPlan().equals(target.bindingProof().runtimeBindingPlan())) {
            return DenialCode.RUNTIME_PLAN_MISMATCH;
        }
        if (target.frameId() == null
                || target.ownerResolutionId() == null
                || !invocation.frameId().equals(target.frameId())
                || !invocation.ownerResolutionId().equals(target.ownerResolutionId())
                || !invocation.cursorId().equals(target.cursorId())) {
            return DenialCode.RUNTIME_CONTEXT_MISMATCH;
        }
        return null;
    }
}
