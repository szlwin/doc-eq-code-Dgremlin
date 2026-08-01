package dec.core.compiler.api;

import dec.core.context.model.Diagnostic;
import java.util.List;

/**
 * Immutable terminal result shared by published and failed compilations.
 */
public abstract class CompilationResult {
    /**
     * Returns the terminal session status.
     *
     * @return {@link CompilationStatus#PUBLISHED} or
     *         {@link CompilationStatus#FAILED}
     */
    public abstract CompilationStatus status();

    /**
     * Returns the stable, caller-read-only diagnostic snapshot.
     *
     * @return immutable diagnostics in deterministic order
     */
    public abstract List<Diagnostic> diagnostics();

    /**
     * Convenience predicate that does not expose a second status model.
     *
     * @return whether this result represents successful publication
     */
    public final boolean isPublished() {
        return status() == CompilationStatus.PUBLISHED;
    }
}
