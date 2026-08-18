package dec.core.starter.access;

import dec.core.context.runtime.ProtectedWriteReceipt;
import dec.core.context.runtime.RuntimeFactValue;

/** STARTER internal-facing MODEL effect port that accepts only Guard-minted one-shot authority. */
public interface GuardAuthorizedModelEffectPort {
    RuntimeFactValue read(ModelEffectAuthorization authorization);
    ProtectedWriteReceipt write(ModelEffectAuthorization authorization);
}
