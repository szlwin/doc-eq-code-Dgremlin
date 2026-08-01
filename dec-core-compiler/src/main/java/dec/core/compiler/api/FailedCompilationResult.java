package dec.core.compiler.api;

import dec.core.context.model.Diagnostic;
import java.util.List;

/**
 * Terminal failure result. It intentionally exposes no model, context, or
 * digest accessor, preventing callers from observing a partial candidate.
 */
public final class FailedCompilationResult extends CompilationResult {
    private final List<Diagnostic> diagnostics;

    /**
     * Captures the diagnostics that explain why publication did not occur.
     *
     * @param diagnostics non-empty stable failure diagnostics
     */
    public FailedCompilationResult(List<Diagnostic> diagnostics) {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    @Override
    public CompilationStatus status() {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    @Override
    public List<Diagnostic> diagnostics() {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }
}
