package dec.core.starter.access;

import dec.core.context.runtime.ResolvedWriteIntent;
import java.util.Objects;

/** STARTER-private one-shot capability; it cannot change target/path/version/value after freezing. */
final class OneShotWriteCapability {
    private final ResolvedWriteIntent intent;
    private boolean consumed;

    OneShotWriteCapability(ResolvedWriteIntent intent) {
        this.intent = Objects.requireNonNull(intent, "intent");
    }

    synchronized ResolvedWriteIntent consume() {
        if (consumed) {
            return null;
        }
        consumed = true;
        return intent;
    }

    synchronized boolean consumed() {
        return consumed;
    }
}
