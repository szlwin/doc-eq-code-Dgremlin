package dec.core.starter.access;

import dec.core.context.model.AccessOperation;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.runtime.ProtectedInvocationId;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.ResolvedWriteIntent;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Opaque, one-shot authorization minted only by the package-private {@link ExactModelAccessGuard}.
 *
 * <p>The type is public only so the STARTER-owned MODEL package bridge can carry it across the
 * module boundary. It has no public constructor or public minting factory, and callers cannot
 * replace target/path/operation facts after minting.
 */
public final class ModelEffectAuthorization {
    private final AccessOperation operation;
    private final ProtectedInvocationId invocationId;
    private final ModelAccessRuleKey modelAccessRuleKey;
    private final ResolvedRuntimeTarget target;
    private final ResolvedWriteIntent writeIntent;
    private final AtomicBoolean consumed = new AtomicBoolean(false);

    private ModelEffectAuthorization(
            AccessOperation operation,
            ProtectedInvocationId invocationId,
            ModelAccessRuleKey modelAccessRuleKey,
            ResolvedRuntimeTarget target,
            ResolvedWriteIntent writeIntent) {
        this.operation = Objects.requireNonNull(operation, "operation");
        this.invocationId = Objects.requireNonNull(invocationId, "invocationId");
        this.modelAccessRuleKey = Objects.requireNonNull(modelAccessRuleKey, "modelAccessRuleKey");
        this.target = Objects.requireNonNull(target, "target");
        this.writeIntent = writeIntent;
    }

    static ModelEffectAuthorization read(
            ProtectedInvocationId invocationId,
            ModelAccessRuleKey modelAccessRuleKey,
            ResolvedRuntimeTarget target) {
        return new ModelEffectAuthorization(
                AccessOperation.READ, invocationId, modelAccessRuleKey, target, null);
    }

    static ModelEffectAuthorization write(
            ProtectedInvocationId invocationId,
            ModelAccessRuleKey modelAccessRuleKey,
            ResolvedRuntimeTarget target,
            ResolvedWriteIntent writeIntent) {
        return new ModelEffectAuthorization(
                AccessOperation.WRITE,
                invocationId,
                modelAccessRuleKey,
                target,
                Objects.requireNonNull(writeIntent, "writeIntent"));
    }

    /** Consume exactly one READ authorization; wrong-operation/replay returns null. */
    public ReadClaim consumeRead() {
        if (operation != AccessOperation.READ || !consumed.compareAndSet(false, true)) {
            return null;
        }
        return new ReadClaim(invocationId, modelAccessRuleKey, target);
    }

    /** Consume exactly one WRITE authorization; wrong-operation/replay returns null. */
    public WriteClaim consumeWrite() {
        if (operation != AccessOperation.WRITE || writeIntent == null
                || !consumed.compareAndSet(false, true)) {
            return null;
        }
        return new WriteClaim(invocationId, modelAccessRuleKey, target, writeIntent);
    }

    /** Immutable READ claim exposed only after successful one-shot consumption. */
    public static final class ReadClaim {
        private final ProtectedInvocationId invocationId;
        private final ModelAccessRuleKey modelAccessRuleKey;
        private final ResolvedRuntimeTarget target;

        private ReadClaim(
                ProtectedInvocationId invocationId,
                ModelAccessRuleKey modelAccessRuleKey,
                ResolvedRuntimeTarget target) {
            this.invocationId = invocationId;
            this.modelAccessRuleKey = modelAccessRuleKey;
            this.target = target;
        }

        public ProtectedInvocationId invocationId() { return invocationId; }
        public ModelAccessRuleKey modelAccessRuleKey() { return modelAccessRuleKey; }
        public ResolvedRuntimeTarget target() { return target; }
    }

    /** Immutable WRITE claim exposed only after successful one-shot consumption. */
    public static final class WriteClaim {
        private final ProtectedInvocationId invocationId;
        private final ModelAccessRuleKey modelAccessRuleKey;
        private final ResolvedRuntimeTarget target;
        private final ResolvedWriteIntent writeIntent;

        private WriteClaim(
                ProtectedInvocationId invocationId,
                ModelAccessRuleKey modelAccessRuleKey,
                ResolvedRuntimeTarget target,
                ResolvedWriteIntent writeIntent) {
            this.invocationId = invocationId;
            this.modelAccessRuleKey = modelAccessRuleKey;
            this.target = target;
            this.writeIntent = writeIntent;
        }

        public ProtectedInvocationId invocationId() { return invocationId; }
        public ModelAccessRuleKey modelAccessRuleKey() { return modelAccessRuleKey; }
        public ResolvedRuntimeTarget target() { return target; }
        public ResolvedWriteIntent writeIntent() { return writeIntent; }
    }
}
