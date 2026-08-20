package dec.core.starter.access;

import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.ProtectedAccessResult;

public interface CustomActionProtectedAccessEntry {
    ProtectedAccessResult invoke(ProtectedAccessInvocation invocation);
}
