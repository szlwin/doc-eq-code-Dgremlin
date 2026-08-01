package dec.core.compiler.api;

import dec.core.context.EngineContext;

/**
 * Owns the single conditional publication side effect for a compiler call.
 */
public interface ContextPublisher {
    /**
     * Atomically publishes {@code candidate} only when the caller's expected
     * context still matches the currently exposed context.
     *
     * @param expectedCurrent nullable expected context for compare-and-set
     * @param candidate fully constructed immutable candidate context
     * @return publication success or a compare-and-set conflict
     */
    PublicationResult publish(EngineContext expectedCurrent, EngineContext candidate);
}
