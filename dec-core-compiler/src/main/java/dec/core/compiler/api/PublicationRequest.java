package dec.core.compiler.api;

import dec.core.context.EngineContext;

/**
 * Immutable conditional-publication boundary supplied by the caller.
 */
public final class PublicationRequest {
    private final EngineContext expectedCurrent;
    private final ContextPublisher publisher;

    /**
     * Binds the expected current context to the only allowed publication side
     * effect. A {@code null} expected context represents initial publication.
     *
     * @param expectedCurrent nullable compare-and-set expectation
     * @param publisher non-null atomic publisher
     */
    public PublicationRequest(EngineContext expectedCurrent, ContextPublisher publisher) {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    public EngineContext expectedCurrent() {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    public ContextPublisher publisher() {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }
}
