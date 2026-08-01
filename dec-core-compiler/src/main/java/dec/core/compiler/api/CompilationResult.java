package dec.core.compiler.api;

import dec.core.context.model.Diagnostic;
import java.util.List;

/**
 * Immutable terminal result shared by published and failed compilations.
 */
public abstract class CompilationResult {
    private final List<Diagnostic> diagnostics;

    /**
     * Defensively freezes the diagnostic snapshot for all result subtypes.
     *
     * @param diagnostics diagnostics already validated by the concrete subtype
     */
    protected CompilationResult(List<Diagnostic> diagnostics) {
        this.diagnostics = ApiContracts.immutableDiagnostics(diagnostics);
    }

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
    public final List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /**
     * Convenience predicate that does not expose a second status model.
     *
     * @return whether this result represents successful publication
     */
    public final boolean isPublished() {
        return status() == CompilationStatus.PUBLISHED;
    }
}
