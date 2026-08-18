package dec.core.model.runtime;

import dec.core.context.runtime.ProtectedWriteReceipt;
import dec.core.context.runtime.ResolvedProtectedReadAccess;
import dec.core.context.runtime.ResolvedProtectedWriteAccess;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.starter.access.GuardAuthorizedModelEffectPort;
import dec.core.starter.access.ModelEffectAuthorization;
import java.util.Objects;

/**
 * STARTER-owned friend bridge compiled in the MODEL runtime package.
 *
 * <p>It can reach MODEL's package-private guarded primitive, but never returns that primitive. The
 * only exported adapter consumes an opaque one-shot authorization before entering MODEL effect
 * code. This preserves the dependency direction starter -> model + context.
 */
public final class RuntimeModelStarterEffectBridge {
    private RuntimeModelStarterEffectBridge() {
    }

    public static GuardAuthorizedModelEffectPort guardedEffectPort(
            RuntimeModelEffectBindingResult binding) {
        Objects.requireNonNull(binding, "binding");
        RuntimeModelGuardedOperationPort guarded = binding.guardedOperationPort();
        if (guarded == null) {
            throw new IllegalStateException("binding does not carry a guarded MODEL operation port");
        }
        return new GuardedAdapter(guarded);
    }

    private static final class GuardedAdapter implements GuardAuthorizedModelEffectPort {
        private final RuntimeModelGuardedOperationPort guarded;

        private GuardedAdapter(RuntimeModelGuardedOperationPort guarded) {
            this.guarded = Objects.requireNonNull(guarded, "guarded");
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
            return guarded.readAuthorized(ResolvedProtectedReadAccess.of(
                    claim.invocationId(), claim.modelAccessRuleKey(), claim.target()));
        }

        @Override
        public ProtectedWriteReceipt write(ModelEffectAuthorization authorization) {
            if (authorization == null) {
                return null;
            }
            ModelEffectAuthorization.WriteClaim claim = authorization.consumeWrite();
            if (claim == null) {
                return null;
            }
            if (!claim.modelAccessRuleKey().equals(claim.writeIntent().modelAccessRuleKey())
                    || !claim.target().equals(claim.writeIntent().resolvedRuntimeTarget())) {
                return null;
            }
            return guarded.writeAuthorized(ResolvedProtectedWriteAccess.of(
                    claim.invocationId(), claim.writeIntent()));
        }
    }
}
