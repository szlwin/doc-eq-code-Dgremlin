package dec.core.compiler.api;

/**
 * Coordinates one isolated compilation and its conditional context publication.
 *
 * <p>The public API deliberately exposes no compile-only success path. Internal
 * compiler passes may use package-private test seams in later tasks, while all
 * callers must cross the same publication state boundary.</p>
 */
public interface ModelCompiler {
    /**
     * Compiles the requested root source and publishes the candidate context as
     * part of the same call.
     *
     * @param request immutable source, option, cancellation, and deadline input
     * @param publicationRequest expected-current and publisher boundary
     * @return either a published result or a failed result
     */
    CompilationResult compileAndPublish(
            CompilationRequest request,
            PublicationRequest publicationRequest);
}
