package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.compiled.CompilerDigestService;
import dec.core.compiler.compiled.DigestBoundCompiledInput;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.DocumentSource;
import dec.core.compiler.source.SourceManifest;
import dec.core.context.EngineContext;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.DataKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DeferredKind;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.NormalizedBody;
import dec.core.context.model.PublishedSourceDescriptor;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.RequiredStage;
import dec.core.context.model.SourceRef;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T14 / I002：原子 provenance 输入与最终 candidate 发布合同。
 */
class CandidateContextT14Test {

    /** bind 必须生成真实摘要并保留非空模型的全部发布事实。 */
    @Test
    void atomicBindCreatesCompleteNonEmptyCandidate() {
        Fixture fixture = fixture("schema-v1", "options-v1");
        Diagnostic warning = PipelineDiagnostics.observerTransitionFailure(
                "SEMANTICALLY_VALIDATED->PUBLISHED");

        EngineContext candidate = fixture.input.candidate(
                Collections.singletonList(warning));

        assertEquals(fixture.publishedManifest,
                candidate.compiledModelSet().sourceManifest());
        assertEquals(fixture.definition,
                candidate.compiledModelSet().definitions().require(
                        fixture.definition.key()));
        assertEquals(fixture.deferredDefinition,
                candidate.compiledModelSet().deferred().find(
                        fixture.deferredDefinition.key()).get());
        assertEquals(Collections.singletonList(warning),
                candidate.compiledModelSet().diagnostics());
        assertEquals("compiler-t14",
                candidate.compiledModelSet().compilerVersion());
        assertEquals("schema-v1",
                candidate.compiledModelSet().schemaVersion());
        assertEquals("options-v1",
                candidate.compiledModelSet().optionsVersion());
        assertTrue(candidate.compiledModelSet().digestPair()
                .sourceDigest().matches("[0-9a-f]{64}"));
        assertTrue(candidate.compiledModelSet().digestPair()
                .semanticDigest().matches("[0-9a-f]{64}"));
    }

    /** Builder 只接受 atomic input，并在首次 freeze 后永久封闭。 */
    @Test
    void builderIsOneShotAndDoesNotExposeSplitInputs() {
        CompiledModelSetBuilder builder = new CompiledModelSetBuilder(
                fixture("schema-v1", "options-v1").boundInput);

        builder.freeze();

        assertThrows(IllegalStateException.class, builder::freeze);
        for (java.lang.reflect.Method method
                : CompiledModelSetBuilder.class.getMethods()) {
            assertFalse("digestPair".equals(method.getName()));
            assertFalse("definitions".equals(method.getName()));
            assertFalse("deferred".equals(method.getName()));
            assertFalse("sourceManifest".equals(method.getName()));
        }
    }

    /** 最终 Pass 必须把完整 candidate 精确交给唯一 Publisher 一次。 */
    @Test
    void finalPassPublishesCompleteCandidateExactlyOnce() {
        Fixture fixture = fixture("schema-v1", "options-v1");
        Diagnostic warning = PipelineDiagnostics.observerTransitionFailure(
                "SEMANTICALLY_VALIDATED->PUBLISHED");
        AtomicInteger publisherCalls = new AtomicInteger();
        AtomicReference<EngineContext> published =
                new AtomicReference<EngineContext>();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(8, inputProducingDigestPass(fixture.input, warning));
        passes.set(9, new CandidateContextPublicationPass());
        ContextPublisher publisher = new ContextPublisher() {
            @Override
            public PublicationResult publish(
                    Optional<EngineContext> expectedCurrent,
                    EngineContext candidate) {
                publisherCalls.incrementAndGet();
                published.set(candidate);
                return PipelineTestSupport.publishedResult();
            }
        };

        PipelineExecutionResult result = execute(passes, publisher);

        assertEquals(CompilationSessionState.PUBLISHED, result.state());
        assertEquals(1, publisherCalls.get());
        EngineContext actual = published.get();
        assertEquals(fixture.publishedManifest,
                actual.compiledModelSet().sourceManifest());
        assertEquals(fixture.definition,
                actual.compiledModelSet().definitions().require(
                        fixture.definition.key()));
        assertEquals(fixture.deferredDefinition,
                actual.compiledModelSet().deferred().find(
                        fixture.deferredDefinition.key()).get());
        assertEquals(Collections.singletonList(warning),
                actual.compiledModelSet().diagnostics());
        assertEquals(fixture.boundInput.digestPair(),
                actual.compiledModelSet().digestPair());
        assertEquals("compiler-t14", actual.compiledModelSet().compilerVersion());
        assertEquals("schema-v1", actual.compiledModelSet().schemaVersion());
        assertEquals("options-v1", actual.compiledModelSet().optionsVersion());
        assertTrue(result.artifacts().get(
                CandidateContextPublicationPass.INPUT_ARTIFACT)
                instanceof CompiledModelSetBuilder.FrozenInput);
    }

    /** 缺少 frozen input 时必须返回精确 blocked Diagnostic。 */
    @Test
    void missingInputFailsWithExactDiagnostic() {
        AtomicInteger publisherCalls = new AtomicInteger();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, new CandidateContextPublicationPass());

        PipelineExecutionResult result = execute(
                passes,
                countingPublisher(publisherCalls));

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisherCalls.get());
        assertTrue(result.artifacts().isEmpty());
        assertEquals(Collections.singletonList(
                PipelineDiagnostics.publicationBlocked()),
                result.diagnostics());
    }

    /** request schema/options mismatch 必须在 Publisher 前精确失败。 */
    @Test
    void requestMismatchFailsWithExactDiagnostic() {
        Fixture fixture = fixture("schema-other", "options-other");
        AtomicInteger publisherCalls = new AtomicInteger();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(8, inputProducingDigestPass(
                fixture.input,
                null));
        passes.set(9, new CandidateContextPublicationPass());

        PipelineExecutionResult result = execute(
                passes,
                countingPublisher(publisherCalls));

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisherCalls.get());
        assertTrue(result.artifacts().isEmpty());
        assertEquals(Collections.singletonList(
                PipelineDiagnostics.publicationProvenanceMismatch()),
                result.diagnostics());
        Diagnostic diagnostic = result.diagnostics().get(0);
        assertEquals(DiagnosticCode.MIX_PUBLICATION_PROVENANCE_MISMATCH,
                diagnostic.code());
        assertEquals(DiagnosticSeverity.ERROR, diagnostic.severity());
        assertEquals("pipeline.publication.provenance-mismatch",
                diagnostic.messageKey());
    }

    /** 执行使用固定 schema-v1/options-v1 的测试 Pipeline。 */
    private static PipelineExecutionResult execute(
            List<CompilerPass> passes,
            ContextPublisher publisher) {
        return new CompilerPipeline(passes).execute(
                PipelineTestSupport.request(
                        new PipelineTestSupport.MutableClock(),
                        () -> false,
                        Optional.empty(),
                        new PipelineTestSupport.RecordingObserver()),
                PipelineTestSupport.publicationRequest(publisher));
    }

    /** 创建只统计调用次数的 Publisher。 */
    private static ContextPublisher countingPublisher(AtomicInteger calls) {
        return new ContextPublisher() {
            @Override
            public PublicationResult publish(
                    Optional<EngineContext> expectedCurrent,
                    EngineContext candidate) {
                calls.incrementAndGet();
                return PipelineTestSupport.publishedResult();
            }
        };
    }

    /** 在 Digest Pass 写入 frozen input，并可同时产生非阻断 Warning。 */
    private static CompilerPass inputProducingDigestPass(
            CompiledModelSetBuilder.FrozenInput input,
            Diagnostic warning) {
        return new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.DIGEST_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                context.putArtifact(
                        CandidateContextPublicationPass.INPUT_ARTIFACT,
                        input);
                return warning == null
                        ? PassResult.passed()
                        : PassResult.of(Collections.singletonList(warning));
            }
        };
    }

    /** 创建真实非空模型、请求版本和 T13 摘要绑定夹具。 */
    private static Fixture fixture(String schemaVersion, String optionsDigest) {
        DefinitionKey key = new DataKey("alpha");
        CompiledDefinition definition = new CompiledDefinition(
                key,
                new SourceRef("source:root", 1, 1, "/data/alpha"),
                new NormalizedBody("json", "{\"name\":\"alpha\"}"));
        Map<DefinitionKey, CompiledDefinition> definitions =
                new LinkedHashMap<DefinitionKey, CompiledDefinition>();
        definitions.put(key, definition);
        ImmutableRegistry<DefinitionKey, CompiledDefinition> definitionRegistry =
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(definitions);

        DeferredDefinition deferredDefinition = new DeferredDefinition(
                new DeferredKey(key, DeferredKind.INFORMATION, 0),
                RequiredStage.P3,
                "P3_INFORMATION",
                new SourceRef("source:root", 2, 1, "/information/alpha"),
                new NormalizedBody("expression", "alpha"),
                Collections.singletonList(key));
        Map<DeferredKey, DeferredDefinition> deferred =
                new LinkedHashMap<DeferredKey, DeferredDefinition>();
        deferred.put(deferredDefinition.key(), deferredDefinition);
        ImmutableDeferredRegistry deferredRegistry =
                new ImmutableDeferredRegistry(deferred);

        PublishedSourceManifest publishedManifest = new PublishedSourceManifest(
                "source:root",
                Collections.singletonList(new PublishedSourceDescriptor(
                        "source:root", "XML", "content-digest")),
                Collections.emptyList());
        DocumentSource rawSource = new DocumentSource(
                "source:root",
                URI.create("memory:/root"),
                DocumentFormat.XML,
                new AllowedRoot(URI.create("memory:/")),
                "<root/>".getBytes(StandardCharsets.UTF_8),
                "fixture-t14");
        SourceManifest sources = new SourceManifest(
                Collections.singletonList(rawSource));
        DigestBoundCompiledInput boundInput = new CompilerDigestService().bind(
                sources,
                publishedManifest,
                definitionRegistry,
                deferredRegistry,
                "compiler-t14",
                new CompilationOptions(schemaVersion, optionsDigest));
        return new Fixture(
                publishedManifest,
                definition,
                deferredDefinition,
                boundInput,
                new CompiledModelSetBuilder(boundInput).freeze());
    }

    /** 聚合完整模型和 frozen input，避免测试之间共享可变状态。 */
    private static final class Fixture {
        private final PublishedSourceManifest publishedManifest;
        private final CompiledDefinition definition;
        private final DeferredDefinition deferredDefinition;
        private final DigestBoundCompiledInput boundInput;
        private final CompiledModelSetBuilder.FrozenInput input;

        private Fixture(
                PublishedSourceManifest publishedManifest,
                CompiledDefinition definition,
                DeferredDefinition deferredDefinition,
                DigestBoundCompiledInput boundInput,
                CompiledModelSetBuilder.FrozenInput input) {
            this.publishedManifest = publishedManifest;
            this.definition = definition;
            this.deferredDefinition = deferredDefinition;
            this.boundInput = boundInput;
            this.input = input;
        }
    }
}
