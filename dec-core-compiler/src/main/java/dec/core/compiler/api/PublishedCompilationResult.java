package dec.core.compiler.api;

import dec.core.context.EngineContext;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DigestPair;
import java.util.List;
import java.util.Objects;

/**
 * Terminal success result containing the model and context exposed by the
 * publisher.
 */
public final class PublishedCompilationResult extends CompilationResult {
    private final CompiledModelSet compiledModelSet;
    private final EngineContext context;

    /**
     * Captures the complete published model/context pair and non-error
     * diagnostics. The explicit consistency check prevents a caller from
     * observing a Context backed by a different model candidate.
     *
     * @param sessionId stable identity of the completed compilation session
     * @param compiledModelSet immutable model set that was published
     * @param context immutable context exposed by the publisher
     * @param diagnostics stable diagnostic snapshot without ERROR entries
     */
    public PublishedCompilationResult(
            String sessionId,
            CompiledModelSet compiledModelSet,
            EngineContext context,
            List<Diagnostic> diagnostics) {
        super(sessionId, ApiContracts.publishedDiagnostics(diagnostics));
        this.compiledModelSet = Objects.requireNonNull(
                compiledModelSet,
                "compiledModelSet");
        this.context = Objects.requireNonNull(context, "context");
        if (!compiledModelSet.equals(context.compiledModelSet())) {
            throw new IllegalArgumentException(
                    "context must be backed by the published compiledModelSet");
        }
    }

    /**
     * Returns the complete immutable model set produced by the compiler.
     */
    public CompiledModelSet compiledModelSet() {
        return compiledModelSet;
    }

    /**
     * Returns the immutable context that was exposed by the publisher.
     */
    public EngineContext context() {
        return context;
    }

    /**
     * Returns both deterministic source and semantic digests.
     */
    public DigestPair digests() {
        return compiledModelSet.digestPair();
    }

    /**
     * Returns the compiler version included in the published semantic identity.
     */
    public String compilerVersion() {
        return compiledModelSet.compilerVersion();
    }

    /**
     * Returns the schema version used by the completed compilation.
     */
    public String schemaVersion() {
        return compiledModelSet.schemaVersion();
    }

    /**
     * Returns the normalized option-set version used by the compilation.
     */
    public String optionsVersion() {
        return compiledModelSet.optionsVersion();
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
        return sessionId().equals(that.sessionId())
                && compiledModelSet.equals(that.compiledModelSet)
                && context.equals(that.context)
                && diagnostics().equals(that.diagnostics());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId(), compiledModelSet, context, diagnostics());
    }

    @Override
    public String toString() {
        return "PublishedCompilationResult{"
                + "sessionId='" + sessionId() + '\''
                + ", semanticDigest='" + digests().semanticDigest() + '\''
                + ", diagnostics=" + diagnostics().size()
                + '}';
    }
}
