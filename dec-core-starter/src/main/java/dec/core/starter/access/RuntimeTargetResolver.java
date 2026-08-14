package dec.core.starter.access;

import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.RuntimeTargetResolution;
import dec.core.model.runtime.RuntimeModelSession;

/** Resolves exactly one trusted runtime target before Guard evaluation. */
public interface RuntimeTargetResolver {
    RuntimeTargetResolution resolve(
            RuntimeBindingPlan plan,
            ProtectedAccessInvocation invocation,
            RuntimeModelSession session);
}
