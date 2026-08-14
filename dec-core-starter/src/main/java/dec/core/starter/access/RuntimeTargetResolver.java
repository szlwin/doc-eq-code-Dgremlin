package dec.core.starter.access;

import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.RuntimeTargetResolution;
import dec.core.model.runtime.RuntimeModelSession;

/**
 * Resolves exactly one trusted runtime target before Guard evaluation.
 *
 * <p>DEV-P2-DEV07-SKEL-R02 keeps resolution authority-neutral: RuntimeFactValue is not inspected as
 * authority and cannot select a different target/path/version. A successful resolution must remain
 * bound to the invocation's exact ModelAccessRuleKey and runtime proof; 0 or N candidates terminate
 * fail-closed before any effect.
 */
public interface RuntimeTargetResolver {
    RuntimeTargetResolution resolve(
            RuntimeBindingPlan plan,
            ProtectedAccessInvocation invocation,
            RuntimeModelSession session);
}
