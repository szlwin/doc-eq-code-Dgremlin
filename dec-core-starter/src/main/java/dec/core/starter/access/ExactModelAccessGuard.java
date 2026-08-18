package dec.core.starter.access;

import dec.core.context.EngineContext;
import dec.core.context.model.AccessOperation;
import dec.core.context.model.CompiledModelAccessRule;
import dec.core.context.runtime.DenialCode;
import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.ResolvedWriteIntent;
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

    /** Mint a one-shot READ authorization only after the exact Guard decision succeeds. */
    ModelEffectAuthorization authorizeRead(
            CompiledModelAccessRule rule,
            ProtectedAccessInvocation invocation,
            ResolvedRuntimeTarget target) {
        if (rule == null || invocation == null || target == null
                || rule.key().operation() != AccessOperation.READ
                || denial(rule, invocation, target) != null) {
            return null;
        }
        return ModelEffectAuthorization.read(
                invocation.invocationId(), rule.key(), target);
    }

    /**
     * Mint a one-shot WRITE authorization bound to the exact Guard-approved target and frozen
     * R31 write intent (operation/path/session/object/value/version).
     */
    ModelEffectAuthorization authorizeWrite(
            CompiledModelAccessRule rule,
            ProtectedAccessInvocation invocation,
            ResolvedRuntimeTarget target,
            ResolvedWriteIntent writeIntent) {
        if (rule == null || invocation == null || target == null || writeIntent == null
                || rule.key().operation() != AccessOperation.WRITE
                || denial(rule, invocation, target) != null
                || !rule.key().equals(writeIntent.modelAccessRuleKey())
                || !target.equals(writeIntent.resolvedRuntimeTarget())
                || !invocation.writeValue().isPresent()
                || !writeIntent.writeValue().isPresent()
                || !invocation.writeValue().get().equals(writeIntent.writeValue().get())
                || !writeIntent.mutationStamp().sessionId().equals(target.sessionId())
                || !writeIntent.mutationStamp().runtimeObjectId().equals(target.runtimeObjectId())
                || !writeIntent.mutationStamp().modelPath().equals(rule.key().path())) {
            return null;
        }
        return ModelEffectAuthorization.write(
                invocation.invocationId(), rule.key(), target, writeIntent);
    }
}
