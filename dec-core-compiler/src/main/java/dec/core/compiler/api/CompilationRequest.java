package dec.core.compiler.api;

import java.util.Objects;

/**
 * Immutable input for one compiler session.
 */
public final class CompilationRequest {
    private final String rootSourceId;
    private final CompilationOptions options;
    private final CancellationToken cancellationToken;

    /**
     * Captures the root source and all caller-controlled execution boundaries.
     *
     * @param rootSourceId normalized identifier of the root configuration source
     * @param options immutable schema, option, and deadline values
     * @param cancellationToken session-local cooperative cancellation seam
     */
    public CompilationRequest(
            String rootSourceId,
            CompilationOptions options,
            CancellationToken cancellationToken) {
        this.rootSourceId = ApiContracts.requireText(rootSourceId, "rootSourceId");
        this.options = Objects.requireNonNull(options, "options");
        this.cancellationToken = Objects.requireNonNull(
                cancellationToken,
                "cancellationToken");
    }

    /**
     * Returns the normalized root source identifier.
     */
    public String rootSourceId() {
        return rootSourceId;
    }

    /**
     * Returns the immutable option and deadline boundary for this session.
     */
    public CompilationOptions options() {
        return options;
    }

    /**
     * Returns the cancellation token owned by this compilation request.
     */
    public CancellationToken cancellationToken() {
        return cancellationToken;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CompilationRequest)) {
            return false;
        }
        CompilationRequest that = (CompilationRequest) other;
        return rootSourceId.equals(that.rootSourceId)
                && options.equals(that.options)
                && cancellationToken.equals(that.cancellationToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootSourceId, options, cancellationToken);
    }

    @Override
    public String toString() {
        return "CompilationRequest{"
                + "rootSourceId='" + rootSourceId + '\''
                + ", options=" + options
                + '}';
    }
}
