package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.FrontendRegistry;
import dec.core.compiler.source.DocumentSourceProvider;
import dec.core.compiler.source.SourceReference;
import dec.core.compiler.source.SourceResolutionContext;
import dec.core.compiler.source.SourceResolutionResult;
import dec.core.context.EngineContext;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.SourceRef;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 验证值对象的字符串表示包含全部语义字段，便于稳定诊断和审查。
 */
class CompilerValueSemanticsR03Test {
    @Test
    void requestAndPublicationRequestExposeEverySemanticField() {
        SourceReference root = new SourceReference("classpath:mix/root.xml");
        DocumentSourceProvider provider = provider();
        FrontendRegistry frontends = format -> null;
        CompilationOptions options = new CompilationOptions("schema-1", "options-1");
        Deadline deadline = new Deadline(100L);
        CancellationToken token = () -> false;
        MonotonicClock clock = () -> 90L;
        CompilationObserver observer = observer();

        CompilationRequest request = new CompilationRequest(
                root,
                provider,
                frontends,
                options,
                Optional.of(deadline),
                token,
                clock,
                observer);
        String requestText = request.toString();
        assertTrue(requestText.contains(root.toString()));
        assertTrue(requestText.contains(provider.toString()));
        assertTrue(requestText.contains(frontends.toString()));
        assertTrue(requestText.contains(options.toString()));
        assertTrue(requestText.contains(deadline.toString()));
        assertTrue(requestText.contains(token.toString()));
        assertTrue(requestText.contains(clock.toString()));
        assertTrue(requestText.contains(observer.toString()));

        ContextPublisher publisher = (expected, candidate) ->
                () -> PublicationStatus.PUBLISHED;
        PublicationRequest publicationRequest = new PublicationRequest(
                Optional.<EngineContext>empty(),
                publisher);
        assertTrue(publicationRequest.toString().contains(publisher.toString()));
        assertTrue(publicationRequest.toString().contains(Optional.empty().toString()));
    }

    @Test
    void terminalResultsExposeEverySemanticField() {
        Diagnostic warning = warningDiagnostic();
        CompiledModelSet modelSet = modelSet(warning);
        EngineContext engineContext = new EngineContext(modelSet);
        PublishedCompilationResult published = PublishedCompilationResult.published(
                modelSet.diagnostics(),
                modelSet,
                engineContext,
                modelSet.digestPair(),
                modelSet.compilerVersion(),
                modelSet.schemaVersion(),
                modelSet.optionsVersion(),
                "sha-256-v1");

        String publishedText = published.toString();
        assertTrue(publishedText.contains(modelSet.diagnostics().toString()));
        assertTrue(publishedText.contains(modelSet.toString()));
        assertTrue(publishedText.contains(engineContext.toString()));
        assertTrue(publishedText.contains(modelSet.digestPair().toString()));
        assertTrue(publishedText.contains(modelSet.compilerVersion()));
        assertTrue(publishedText.contains(modelSet.schemaVersion()));
        assertTrue(publishedText.contains(modelSet.optionsVersion()));
        assertTrue(publishedText.contains("sha-256-v1"));

        FailedCompilationResult failed = FailedCompilationResult.failed(
                Collections.singletonList(errorDiagnostic()));
        assertTrue(failed.toString().contains(failed.diagnostics().toString()));
    }

    /**
     * 创建无全局状态的 Source Provider 测试替身。
     */
    private static DocumentSourceProvider provider() {
        return new DocumentSourceProvider() {
            @Override
            public SourceResolutionResult resolve(
                    SourceReference reference,
                    SourceResolutionContext context) {
                return null;
            }

            @Override
            public SourceResolutionResult resolveFileSet(
                    SourceReference reference,
                    SourceResolutionContext context) {
                return null;
            }
        };
    }

    /**
     * 创建无全局状态的 Observer 测试替身。
     */
    private static CompilationObserver observer() {
        return new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                // 测试替身不记录外部状态。
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                // 测试替身不记录外部状态。
            }
        };
    }

    /**
     * 创建包含 WARNING 的最终 T01 模型。
     */
    private static CompiledModelSet modelSet(Diagnostic warning) {
        return new CompiledModelSet(
                PublishedSourceManifest.empty(),
                dec.core.context.model.CompiledViewMaterializationIndex.empty(),
                dec.core.context.model.ModelAccessPolicyIndex.empty(),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                Collections.singletonList(warning),
                new DigestPair("source-1", "semantic-1"),
                "compiler-1",
                "schema-1",
                "options-1");
    }

    /**
     * 创建成功结果可携带的 WARNING Diagnostic。
     */
    private static Diagnostic warningDiagnostic() {
        return diagnostic(DiagnosticSeverity.WARNING, "publication.warning");
    }

    /**
     * 创建失败结果必须携带的 ERROR Diagnostic。
     */
    private static Diagnostic errorDiagnostic() {
        return diagnostic(DiagnosticSeverity.ERROR, "publication.error");
    }

    /**
     * 创建稳定 SourceRef 的测试 Diagnostic。
     */
    private static Diagnostic diagnostic(
            DiagnosticSeverity severity,
            String messageKey) {
        return new Diagnostic(
                DiagnosticCode.MIX_PUBLICATION_BLOCKED,
                severity,
                messageKey,
                null,
                new SourceRef("test:root", 1, 1, "/root"),
                Collections.<SourceRef>emptyList(),
                "Review publication state",
                "PublicationPass");
    }
}
