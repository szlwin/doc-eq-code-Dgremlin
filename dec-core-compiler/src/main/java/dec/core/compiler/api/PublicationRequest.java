package dec.core.compiler.api;

import dec.core.context.EngineContext;
import java.util.Objects;

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
        this.expectedCurrent = expectedCurrent;
        this.publisher = Objects.requireNonNull(publisher, "publisher");
    }

    /**
     * Returns the nullable compare-and-set expectation supplied by the caller.
     */
    public EngineContext expectedCurrent() {
        return expectedCurrent;
    }

    /**
     * Returns the only publisher that may expose the candidate context.
     */
    public ContextPublisher publisher() {
        return publisher;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PublicationRequest)) {
            return false;
        }
        PublicationRequest that = (PublicationRequest) other;
        return Objects.equals(expectedCurrent, that.expectedCurrent)
                && publisher.equals(that.publisher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expectedCurrent, publisher);
    }

    @Override
    public String toString() {
        return "PublicationRequest{"
                + "expectedCurrent=" + expectedCurrent
                + ", publisher=" + publisher.getClass().getName()
                + '}';
    }
}
