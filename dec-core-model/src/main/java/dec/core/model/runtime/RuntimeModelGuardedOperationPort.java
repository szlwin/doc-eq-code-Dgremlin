package dec.core.model.runtime;

import dec.core.context.runtime.ProtectedWriteReceipt;
import dec.core.context.runtime.ResolvedProtectedReadAccess;
import dec.core.context.runtime.ResolvedProtectedWriteAccess;
import dec.core.context.runtime.RuntimeFactValue;

/**
 * MODEL package-private effect primitive for STARTER's guarded composition bridge.
 *
 * <p>The public raw port is disabled for production scopes. Only a bridge class that is compiled
 * into this MODEL package can reach these methods after it has consumed a Guard-minted
 * authorization.
 */
interface RuntimeModelGuardedOperationPort {
    RuntimeFactValue readAuthorized(ResolvedProtectedReadAccess access);
    ProtectedWriteReceipt writeAuthorized(ResolvedProtectedWriteAccess access);
}
