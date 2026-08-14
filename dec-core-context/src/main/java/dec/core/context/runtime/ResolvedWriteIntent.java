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
    private final Optional<RuntimeFactValue> writeValue;

    private ResolvedWriteIntent(
            RuntimeWriteIntentId id,
            ModelAccessRuleKey modelAccessRuleKey,
            Optional<RuleKey> ruleKeyProvenance,
            ResolvedRuntimeTarget resolvedRuntimeTarget,
            RuntimeMutationStamp mutationStamp,
            Optional<RuntimeFactValue> writeValue) {
        this.id = Objects.requireNonNull(id, "id");
        this.modelAccessRuleKey = Objects.requireNonNull(modelAccessRuleKey, "modelAccessRuleKey");
        this.ruleKeyProvenance = Objects.requireNonNull(ruleKeyProvenance, "ruleKeyProvenance");
        this.resolvedRuntimeTarget = Objects.requireNonNull(resolvedRuntimeTarget, "resolvedRuntimeTarget");
        this.mutationStamp = Objects.requireNonNull(mutationStamp, "mutationStamp");
        this.writeValue = Objects.requireNonNull(writeValue, "writeValue");
    }

    /** R30/R29 compatibility factory; value-less intent is not executable as an R31 WRITE. */
    public static ResolvedWriteIntent of(
            RuntimeWriteIntentId id,
            ModelAccessRuleKey modelAccessRuleKey,
            Optional<RuleKey> ruleKeyProvenance,
            ResolvedRuntimeTarget resolvedRuntimeTarget,
            RuntimeMutationStamp mutationStamp) {
        return new ResolvedWriteIntent(
                id,
                modelAccessRuleKey,
                ruleKeyProvenance,
                resolvedRuntimeTarget,
                mutationStamp,
                Optional.<RuntimeFactValue>empty());
    }

    /** R31 executable WRITE intent freezes the immutable replacement value with target/path/version. */
    public static ResolvedWriteIntent of(
            RuntimeWriteIntentId id,
            ModelAccessRuleKey modelAccessRuleKey,
            Optional<RuleKey> ruleKeyProvenance,
            ResolvedRuntimeTarget resolvedRuntimeTarget,
            RuntimeMutationStamp mutationStamp,
            RuntimeFactValue writeValue) {
        return new ResolvedWriteIntent(
                id,
                modelAccessRuleKey,
                ruleKeyProvenance,
                resolvedRuntimeTarget,
                mutationStamp,
                Optional.of(Objects.requireNonNull(writeValue, "writeValue")));
    }

    public RuntimeWriteIntentId id() { return id; }
    public ModelAccessRuleKey modelAccessRuleKey() { return modelAccessRuleKey; }
    public Optional<RuleKey> ruleKeyProvenance() { return ruleKeyProvenance; }
    public ResolvedRuntimeTarget resolvedRuntimeTarget() { return resolvedRuntimeTarget; }
    public RuntimeMutationStamp mutationStamp() { return mutationStamp; }
    public Optional<RuntimeFactValue> writeValue() { return writeValue; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ResolvedWriteIntent)) return false;
        ResolvedWriteIntent that = (ResolvedWriteIntent) other;
        return id.equals(that.id)
                && modelAccessRuleKey.equals(that.modelAccessRuleKey)
                && ruleKeyProvenance.equals(that.ruleKeyProvenance)
                && resolvedRuntimeTarget.equals(that.resolvedRuntimeTarget)
                && mutationStamp.equals(that.mutationStamp)
                && writeValue.equals(that.writeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                modelAccessRuleKey,
                ruleKeyProvenance,
                resolvedRuntimeTarget,
                mutationStamp,
                writeValue);
    }
}
