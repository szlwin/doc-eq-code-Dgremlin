package dec.core.compiler.api;

import dec.core.context.model.Diagnostic;
import java.util.List;
import java.util.Objects;

/**
 * Terminal failure result. It intentionally exposes no model, context, or
 * digest accessor, preventing callers from observing a partial candidate.
 */
public final class FailedCompilationResult extends CompilationResult {
    /**
     * Captures the session identity and diagnostics that explain why publication
     * did not occur.
     *
     * @param sessionId stable identity of the failed compilation session
     * @param diagnostics non-empty snapshot containing at least one ERROR
     */
    public FailedCompilationResult(String sessionId, List<Diagnostic> diagnostics) {
        super(sessionId, ApiContracts.failedDiagnostics(diagnostics));
    }

    @Override
    public CompilationStatus status() {
        return CompilationStatus.FAILED;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof FailedCompilationResult
                && sessionId().equals(((FailedCompilationResult) other).sessionId())
                && diagnostics().equals(((FailedCompilationResult) other).diagnostics()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId(), diagnostics());
    }

    @Override
    public String toString() {
        return "FailedCompilationResult{"
                + "sessionId='" + sessionId() + '\''
                + ", diagnostics=" + diagnostics().size()
                + '}';
    }
}
