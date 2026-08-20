package dec.core.model.runtime;

import dec.core.context.runtime.ProtectedWriteReceipt;
import dec.core.context.runtime.ResolvedProtectedReadAccess;
import dec.core.context.runtime.ResolvedProtectedWriteAccess;
import dec.core.context.runtime.RuntimeFactValue;

/** STARTER composition 私有持有的 MODEL effect port；每次调用仍必须重验 session/object/handle。 */
public interface RuntimeModelOperationPort {
    RuntimeFactValue read(ResolvedProtectedReadAccess access);
    ProtectedWriteReceipt write(ResolvedProtectedWriteAccess access);
}
