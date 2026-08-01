package dec.core.compiler.api;

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
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    public String rootSourceId() {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    public CompilationOptions options() {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    public CancellationToken cancellationToken() {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }
}
