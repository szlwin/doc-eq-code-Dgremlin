package dec.core.compiler.api;

import dec.core.context.EngineContext;
import dec.core.context.model.Diagnostic;
import java.util.List;

/**
 * Terminal success result containing the context exposed by the publisher.
 */
public final class PublishedCompilationResult extends CompilationResult {
    private final EngineContext context;
    private final List<Diagnostic> diagnostics;

    /**
     * Captures the published context and any non-error diagnostics.
     *
     * @param context successfully published immutable context
     * @param diagnostics stable diagnostic snapshot
     */
    public PublishedCompilationResult(
            EngineContext context,
            List<Diagnostic> diagnostics) {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    public EngineContext context() {
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
