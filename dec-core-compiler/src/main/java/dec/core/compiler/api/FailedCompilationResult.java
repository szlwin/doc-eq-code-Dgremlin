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
     * Captures the diagnostics that explain why publication did not occur.
     *
     * @param diagnostics non-empty snapshot containing at least one ERROR
     */
    public FailedCompilationResult(List<Diagnostic> diagnostics) {
        super(ApiContracts.failedDiagnostics(diagnostics));
    }

    @Override
    public CompilationStatus status() {
        return CompilationStatus.FAILED;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof FailedCompilationResult
                && diagnostics().equals(((FailedCompilationResult) other).diagnostics()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(diagnostics());
    }

    @Override
    public String toString() {
        return "FailedCompilationResult{diagnostics=" + diagnostics().size() + '}';
    }
}
