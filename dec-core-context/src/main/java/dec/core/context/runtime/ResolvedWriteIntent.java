package dec.core.context.runtime;

import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.RuleKey;
import java.util.Objects;
import java.util.Optional;

/** Frozen exact WRITE intent; it carries no operation port or mutable authority. */
public final class ResolvedWriteIntent {
    private final RuntimeWriteIntentId id;
    private final ModelAccessRuleKey modelAccessRuleKey;
    private final Optional<RuleKey> ruleKeyProvenance;
    private final ResolvedRuntimeTarget resolvedRuntimeTarget;
    private final RuntimeMutationStamp mutationStamp;

    private ResolvedWriteIntent(
            RuntimeWriteIntentId id,
            ModelAccessRuleKey modelAccessRuleKey,
            Optional<RuleKey> ruleKeyProvenance,
            ResolvedRuntimeTarget resolvedRuntimeTarget,
            RuntimeMutationStamp mutationStamp) {
        this.id = Objects.requireNonNull(id, "id");
        this.modelAccessRuleKey = Objects.requireNonNull(modelAccessRuleKey, "modelAccessRuleKey");
        this.ruleKeyProvenance = Objects.requireNonNull(ruleKeyProvenance, "ruleKeyProvenance");
        this.resolvedRuntimeTarget = Objects.requireNonNull(resolvedRuntimeTarget, "resolvedRuntimeTarget");
        this.mutationStamp = Objects.requireNonNull(mutationStamp, "mutationStamp");
    }

    public static ResolvedWriteIntent of(
            RuntimeWriteIntentId id,
            ModelAccessRuleKey modelAccessRuleKey,
            Optional<RuleKey> ruleKeyProvenance,
            ResolvedRuntimeTarget resolvedRuntimeTarget,
            RuntimeMutationStamp mutationStamp) {
        return new ResolvedWriteIntent(
                id, modelAccessRuleKey, ruleKeyProvenance, resolvedRuntimeTarget, mutationStamp);
    }

    public RuntimeWriteIntentId id() { return id; }
    public ModelAccessRuleKey modelAccessRuleKey() { return modelAccessRuleKey; }
    public Optional<RuleKey> ruleKeyProvenance() { return ruleKeyProvenance; }
    public ResolvedRuntimeTarget resolvedRuntimeTarget() { return resolvedRuntimeTarget; }
    public RuntimeMutationStamp mutationStamp() { return mutationStamp; }
}
