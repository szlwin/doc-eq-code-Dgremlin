package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 验证完整 T02 API 的行为不变量，而不仅是反射签名。
 */
class CompilerFullApiBehaviorR03Test {
    @Test
    void publishedResultExposesOneCompletePublishedFact() {
        Diagnostic warning = warningDiagnostic();
        CompiledModelSet modelSet = modelSet(
                "semantic-1",
                Collections.singletonList(warning));
        EngineContext engineContext = new EngineContext(modelSet);

        PublishedCompilationResult result = published(modelSet, engineContext);

        assertEquals(CompilationStatus.PUBLISHED, result.status());
        assertSame(modelSet, result.modelSet());
        assertSame(engineContext, result.engineContext());
        assertSame(modelSet.diagnostics(), result.diagnostics());
        assertEquals(modelSet.digestPair(), result.digests());
        assertEquals(modelSet.compilerVersion(), result.compilerVersion());
        assertEquals(modelSet.schemaVersion(), result.schemaVersion());
        assertEquals(modelSet.optionsVersion(), result.optionsDigest());
        assertEquals("sha-256-v1", result.digestAlgorithmVersion());
    }

    @Test
    void publishedResultRejectsEveryDivergentPublishedFact() {
        CompiledModelSet modelSet = modelSet(
                "semantic-1",
                Collections.<Diagnostic>emptyList());
        CompiledModelSet equalButDistinct = modelSet(
                "semantic-1",
                Collections.<Diagnostic>emptyList());
        EngineContext engineContext = new EngineContext(modelSet);

        assertEquals(modelSet, equalButDistinct);
        assertFalse(modelSet == equalButDistinct);
        assertThrows(
                IllegalArgumentException.class,
                () -> published(modelSet, new EngineContext(equalButDistinct)));
        assertThrows(
                IllegalArgumentException.class,
                () -> PublishedCompilationResult.published(
                        Collections.singletonList(warningDiagnostic()),
                        modelSet,
                        engineContext,
                        modelSet.digestPair(),
                        modelSet.compilerVersion(),
                        modelSet.schemaVersion(),
                        modelSet.optionsVersion(),
                        "sha-256-v1"));
        assertThrows(
                IllegalArgumentException.class,
                () -> PublishedCompilationResult.published(
                        modelSet.diagnostics(),
                        modelSet,
                        engineContext,
                        new DigestPair("other-source", "other-semantic"),
                        modelSet.compilerVersion(),
                        modelSet.schemaVersion(),
                        modelSet.optionsVersion(),
                        "sha-256-v1"));
        assertThrows(
                IllegalArgumentException.class,
                () -> PublishedCompilationResult.published(
                        modelSet.diagnostics(),
                        modelSet,
                        engineContext,
                        modelSet.digestPair(),
                        "other-compiler",
                        modelSet.schemaVersion(),
                        modelSet.optionsVersion(),
                        "sha-256-v1"));
        assertThrows(
                IllegalArgumentException.class,
                () -> PublishedCompilationResult.published(
                        modelSet.diagnostics(),
                        modelSet,
                        engineContext,
                        modelSet.digestPair(),
                        modelSet.compilerVersion(),
                        "other-schema",
                        modelSet.optionsVersion(),
                        "sha-256-v1"));
        assertThrows(
                IllegalArgumentException.class,
                () -> PublishedCompilationResult.published(
                        modelSet.diagnostics(),
                        modelSet,
                        engineContext,
                        modelSet.digestPair(),
                        modelSet.compilerVersion(),
                        modelSet.schemaVersion(),
                        "other-options",
                        "sha-256-v1"));
        assertThrows(
                IllegalArgumentException.class,
                () -> PublishedCompilationResult.published(
                        modelSet.diagnostics(),
                        modelSet,
                        engineContext,
                        modelSet.digestPair(),
                        modelSet.compilerVersion(),
                        modelSet.schemaVersion(),
                        modelSet.optionsVersion(),
                        " "));
    }

    @Test
    void requestRejectsMissingInjectedSessionDependencies() {
        SourceReference root = new SourceReference("classpath:mix/root.xml");
        DocumentSourceProvider provider = provider();
        FrontendRegistry frontends = format -> null;
        CompilationOptions options = new CompilationOptions("schema-1", "options-1");
        Optional<Deadline> deadline = Optional.of(new Deadline(100L));
        CancellationToken token = () -> false;
        MonotonicClock clock = () -> 90L;
        CompilationObserver observer = observer();

        assertThrows(
                NullPointerException.class,
                () -> new CompilationRequest(
                        null, provider, frontends, options, deadline, token, clock, observer));
        assertThrows(
                NullPointerException.class,
                () -> new CompilationRequest(
                        root, null, frontends, options, deadline, token, clock, observer));
        assertThrows(
                NullPointerException.class,
                () -> new CompilationRequest(
                        root, provider, null, options, deadline, token, clock, observer));
        assertThrows(
                NullPointerException.class,
                () -> new CompilationRequest(
                        root, provider, frontends, null, deadline, token, clock, observer));
        assertThrows(
                NullPointerException.class,
                () -> new CompilationRequest(
                        root, provider, frontends, options, null, token, clock, observer));
        assertThrows(
                NullPointerException.class,
                () -> new CompilationRequest(
                        root, provider, frontends, options, deadline, null, clock, observer));
        assertThrows(
                NullPointerException.class,
                () -> new CompilationRequest(
                        root, provider, frontends, options, deadline, token, null, observer));
        assertThrows(
                NullPointerException.class,
                () -> new CompilationRequest(
                        root, provider, frontends, options, deadline, token, clock, null));
    }

    @Test
    void timingDeadlineAndTransitionsUseStableValidatedSemantics() {
        MonotonicClock clock = () -> 100L;
        Deadline deadline = new Deadline(100L);
        assertTrue(deadline.isExpired(clock.nanoTime()));

        CompilationTiming passTiming = new CompilationTiming(
                TimingPhase.PASS,
                Optional.of("ReferenceResolutionPass"),
                12L);
        assertEquals(TimingPhase.PASS, passTiming.phase());
        assertEquals("ReferenceResolutionPass", passTiming.pass().get());
        assertEquals(12L, passTiming.elapsedNanos());

        assertThrows(
                IllegalArgumentException.class,
                () -> new CompilationTiming(
                        TimingPhase.PASS,
                        Optional.<String>empty(),
                        1L));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CompilationTiming(
                        TimingPhase.DIGEST,
                        Optional.of("DigestPass"),
                        1L));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CompilationTiming(
                        TimingPhase.PARSE,
                        Optional.<String>empty(),
                        -1L));

        SessionStateTransition transition = new SessionStateTransition(
                CompilationSessionState.CREATED,
                CompilationSessionState.SOURCES_DISCOVERED);
        assertEquals(CompilationSessionState.CREATED, transition.from());
        assertEquals(CompilationSessionState.SOURCES_DISCOVERED, transition.to());
        assertThrows(
                IllegalArgumentException.class,
                () -> new SessionStateTransition(
                        CompilationSessionState.CREATED,
                        CompilationSessionState.CREATED));
    }

    /**
     * 使用模型中的全部事实创建成功结果。
     */
    private static PublishedCompilationResult published(
            CompiledModelSet modelSet,
            EngineContext engineContext) {
        return PublishedCompilationResult.published(
                modelSet.diagnostics(),
                modelSet,
                engineContext,
                modelSet.digestPair(),
                modelSet.compilerVersion(),
                modelSet.schemaVersion(),
                modelSet.optionsVersion(),
                "sha-256-v1");
    }

    /**
     * 创建只用于验证显式注入边界的 Source Provider。
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
     * 创建无外部状态的 Observer 测试替身。
     */
    private static CompilationObserver observer() {
        return new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                // 测试替身不持有全局状态。
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                // 测试替身不持有全局状态。
            }
        };
    }

    /**
     * 使用最终 T01 构造合同创建最小模型。
     */
    private static CompiledModelSet modelSet(
            String semanticDigest,
            List<Diagnostic> diagnostics) {
        return new CompiledModelSet(
                PublishedSourceManifest.empty(),
                dec.core.context.model.CompiledViewMaterializationIndex.empty(),
                dec.core.context.model.ModelAccessPolicyIndex.empty(),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                diagnostics,
                new DigestPair("source-1", semanticDigest),
                "compiler-1",
                "schema-1",
                "options-1");
    }

    /**
     * 创建合法的 WARNING Diagnostic。
     */
    private static Diagnostic warningDiagnostic() {
        return new Diagnostic(
                DiagnosticCode.MIX_PUBLICATION_BLOCKED,
                DiagnosticSeverity.WARNING,
                "publication.warning",
                null,
                new SourceRef("test:root", 1, 1, "/root"),
                Collections.<SourceRef>emptyList(),
                "Review the warning before publication",
                "PublicationPass");
    }
}
