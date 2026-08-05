package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.PublicationResult;
import dec.core.context.EngineContext;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.PublishedSourceManifest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T14：冻结模型输入并由最终 Pass 准备完整 candidate Context。
 */
class CandidateContextT14Test {

    /** Builder 必须按四阶段构造包含完整版本与摘要事实的 Context。 */
    @Test
    void builderCreatesCompleteCandidateContext() {
        DigestPair digestPair = new DigestPair("source-t14", "semantic-t14");

        EngineContext candidate = input(digestPair).candidate(
                Collections.emptyList());

        assertEquals(
                PublishedSourceManifest.empty(),
                candidate.compiledModelSet().sourceManifest());
        assertEquals(0, candidate.compiledModelSet().definitions().size());
        assertEquals(0, candidate.compiledModelSet().deferred().size());
        assertEquals(digestPair, candidate.compiledModelSet().digestPair());
        assertEquals("compiler-t14", candidate.compiledModelSet().compilerVersion());
        assertEquals("schema-t14", candidate.compiledModelSet().schemaVersion());
        assertEquals("options-t14", candidate.compiledModelSet().optionsVersion());
    }

    /** 越序、缺失和 freeze 后复用必须稳定拒绝。 */
    @Test
    void builderEnforcesFixedOneShotOrder() {
        CompiledModelSetBuilder builder = builder();
        assertThrows(
                IllegalStateException.class,
                () -> builder.definitions(emptyDefinitions()));
        assertThrows(IllegalStateException.class, builder::freeze);

        CompiledModelSetBuilder completed = builder()
                .sourceManifest(PublishedSourceManifest.empty())
                .definitions(emptyDefinitions())
                .deferred(emptyDeferred())
                .digestPair(new DigestPair("source-t14", "semantic-t14"));
        completed.freeze();

        assertThrows(IllegalStateException.class, completed::freeze);
        assertThrows(
                IllegalStateException.class,
                () -> completed.digestPair(
                        new DigestPair("source-other", "semantic-other")));
    }

    /** 最终 Pass 必须把精确 candidate 交给唯一 Publisher。 */
    @Test
    void finalPassPublishesPreparedCandidateExactlyOnce() {
        AtomicInteger publisherCalls = new AtomicInteger();
        AtomicReference<EngineContext> published =
                new AtomicReference<EngineContext>();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(8, inputProducingDigestPass());
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

        PipelineExecutionResult result = new CompilerPipeline(passes).execute(
                PipelineTestSupport.request(
                        new PipelineTestSupport.MutableClock(),
                        () -> false,
                        Optional.empty(),
                        new PipelineTestSupport.RecordingObserver()),
                PipelineTestSupport.publicationRequest(publisher));

        assertEquals(CompilationSessionState.PUBLISHED, result.state());
        assertEquals(1, publisherCalls.get());
        assertTrue(published.get() != null);
        assertEquals(
                new DigestPair("source-t14", "semantic-t14"),
                published.get().compiledModelSet().digestPair());
        assertFalse(result.artifacts().isEmpty());
        assertTrue(result.artifacts().get(
                CandidateContextPublicationPass.INPUT_ARTIFACT)
                instanceof CompiledModelSetBuilder.FrozenInput);
    }

    /** 缺少冻结输入时必须 FAILED，且不能调用 Publisher。 */
    @Test
    void missingInputFailsBeforePublisher() {
        AtomicInteger publisherCalls = new AtomicInteger();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, new CandidateContextPublicationPass());
        ContextPublisher publisher = new ContextPublisher() {
            @Override
            public PublicationResult publish(
                    Optional<EngineContext> expectedCurrent,
                    EngineContext candidate) {
                publisherCalls.incrementAndGet();
                return PipelineTestSupport.publishedResult();
            }
        };

        PipelineExecutionResult result = new CompilerPipeline(passes).execute(
                PipelineTestSupport.request(
                        new PipelineTestSupport.MutableClock(),
                        () -> false,
                        Optional.empty(),
                        new PipelineTestSupport.RecordingObserver()),
                PipelineTestSupport.publicationRequest(publisher));

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisherCalls.get());
        assertTrue(result.artifacts().isEmpty());
    }

    /** 创建在 Digest Pass 写入 frozen input 的测试 Pass。 */
    private static CompilerPass inputProducingDigestPass() {
        return new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.DIGEST_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                context.putArtifact(
                        CandidateContextPublicationPass.INPUT_ARTIFACT,
                        input(new DigestPair(
                                "source-t14",
                                "semantic-t14")));
                return PassResult.passed();
            }
        };
    }

    /** 创建完成四阶段冻结的输入闭包。 */
    private static CompiledModelSetBuilder.FrozenInput input(
            DigestPair digestPair) {
        return builder()
                .sourceManifest(PublishedSourceManifest.empty())
                .definitions(emptyDefinitions())
                .deferred(emptyDeferred())
                .digestPair(digestPair)
                .freeze();
    }

    /** 创建使用稳定版本域的 Builder。 */
    private static CompiledModelSetBuilder builder() {
        return new CompiledModelSetBuilder(
                "compiler-t14",
                "schema-t14",
                "options-t14");
    }

    /** 创建空的不可变 Definition Registry。 */
    private static ImmutableRegistry<DefinitionKey, CompiledDefinition>
            emptyDefinitions() {
        return new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                Collections.<DefinitionKey, CompiledDefinition>emptyMap());
    }

    /** 创建空的不可变 Deferred Registry。 */
    private static ImmutableDeferredRegistry emptyDeferred() {
        return new ImmutableDeferredRegistry(
                Collections.<DeferredKey, DeferredDefinition>emptyMap());
    }
}
