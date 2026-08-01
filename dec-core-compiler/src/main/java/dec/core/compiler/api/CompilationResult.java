package dec.core.compiler.api;

import dec.core.context.model.Diagnostic;
import java.util.List;

/**
 * Immutable terminal result shared by published and failed compilations.
 */
public abstract class CompilationResult {
    private final String sessionId;
    private final List<Diagnostic> diagnostics;

    /**
     * Freezes the session identity and diagnostic snapshot for every result.
     *
     * @param sessionId stable identity of the isolated compilation session
     * @param diagnostics diagnostics already validated by the concrete subtype
     */
    protected CompilationResult(String sessionId, List<Diagnostic> diagnostics) {
        this.sessionId = ApiContracts.requireText(sessionId, "sessionId");
        this.diagnostics = ApiContracts.immutableDiagnostics(diagnostics);
    }

    /**
     * Returns the stable identity of the compilation session that produced this
     * terminal result.
     */
    public final String sessionId() {
        return sessionId;
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
