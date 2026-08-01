package dec.core.compiler.api;

import dec.core.context.EngineContext;
import dec.core.context.model.Diagnostic;
import java.util.List;
import java.util.Objects;

/**
 * Terminal success result containing the context exposed by the publisher.
 */
public final class PublishedCompilationResult extends CompilationResult {
    private final EngineContext context;

    /**
     * Captures the published context and any non-error diagnostics.
     *
     * @param context successfully published immutable context
     * @param diagnostics stable diagnostic snapshot without ERROR entries
     */
    public PublishedCompilationResult(
            EngineContext context,
            List<Diagnostic> diagnostics) {
        super(ApiContracts.publishedDiagnostics(diagnostics));
        this.context = Objects.requireNonNull(context, "context");
    }

    /**
     * Returns the immutable context that was exposed by the publisher.
     */
    public EngineContext context() {
        return context;
    }

    @Override
    public CompilationStatus status() {
        return CompilationStatus.PUBLISHED;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PublishedCompilationResult)) {
            return false;
        }
        PublishedCompilationResult that = (PublishedCompilationResult) other;
        return context.equals(that.context)
                && diagnostics().equals(that.diagnostics());
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, diagnostics());
    }

    @Override
    public String toString() {
        return "PublishedCompilationResult{"
                + "context=" + context
                + ", diagnostics=" + diagnostics().size()
                + '}';
    }
}
