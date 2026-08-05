package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.PublicationRequest;
import dec.core.context.EngineContext;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DigestPair;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.Registry;
import dec.core.context.model.RequiredStage;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T14 独立 Review：复核输入快照、Diagnostic 和发布能力边界。
 */
class CandidateContextT14IndependentReviewTest {

    /** Registry 与 Deferred 必须只在各自阶段读取，freeze 后不得重新访问。 */
    @Test
    void mutableRegistriesAreReadOnlyDuringTheirSnapshotStage() {
        CountingRegistry definitions = new CountingRegistry();
        CountingDeferredRegistry deferred = new CountingDeferredRegistry();

        CompiledModelSetBuilder.FrozenInput input = builder()
                .sourceManifest(PublishedSourceManifest.empty())
                .definitions(definitions)
                .deferred(deferred)
                .digestPair(new DigestPair("source-review", "semantic-review"))
                .freeze();

        definitions.rejectFurtherReads = true;
        deferred.rejectFurtherReads = true;
        EngineContext candidate = input.candidate(Collections.emptyList());

        assertEquals(1, definitions.keysReads.get());
        assertEquals(1, deferred.keysReads.get());
        assertEquals(0, candidate.compiledModelSet().definitions().size());
        assertEquals(0, candidate.compiledModelSet().deferred().size());
    }

    /** ERROR Diagnostic 必须在 candidate 构造边界 fail-closed。 */
    @Test
    void errorDiagnosticRejectsCandidateBeforePublication() {
        Diagnostic error = PipelineTestSupport.error("t14.error", 8);

        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> input().candidate(Collections.singletonList(error)));

        assertTrue(failure.getMessage().contains("ERROR"));
    }

    /** 非阻断 Warning 必须完整进入 CompiledModelSet，不能被 Builder 丢弃。 */
    @Test
    void warningDiagnosticIsPreservedInCandidate() {
        Diagnostic warning = PipelineDiagnostics.observerTransitionFailure(
                "SEMANTICALLY_VALIDATED->PUBLISHED");

        EngineContext candidate = input().candidate(
                Collections.singletonList(warning));

        assertEquals(
                Collections.singletonList(warning),
                candidate.compiledModelSet().diagnostics());
    }

    /** 版本域和四个阶段的 null/blank 输入必须稳定拒绝。 */
    @Test
    void invalidVersionsAndNullStagesFailClosed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CompiledModelSetBuilder(" ", "schema", "options"));
        assertThrows(
                NullPointerException.class,
                () -> builder().sourceManifest(null));
        assertThrows(
                NullPointerException.class,
                () -> builder()
                        .sourceManifest(PublishedSourceManifest.empty())
                        .definitions(null));
    }

    /** FrozenInput 可重复生成等价候选，但不能暴露或重新读取 Builder 状态。 */
    @Test
    void frozenInputProducesStableEquivalentCandidates() {
        CompiledModelSetBuilder.FrozenInput input = input();

        EngineContext first = input.candidate(Collections.emptyList());
        EngineContext second = input.candidate(Collections.emptyList());

        assertFalse(first == second);
        assertEquals(
                first.compiledModelSet().sourceManifest(),
                second.compiledModelSet().sourceManifest());
        assertEquals(
                first.compiledModelSet().digestPair(),
                second.compiledModelSet().digestPair());
        assertEquals(
                first.compiledModelSet().compilerVersion(),
                second.compiledModelSet().compilerVersion());
    }

    /** 最终 Pass 自身不得持有 Publisher 或 PublicationRequest capability。 */
    @Test
    void finalPassDoesNotHoldExternalPublicationCapability() {
        for (Field field : CandidateContextPublicationPass.class
                .getDeclaredFields()) {
            assertFalse(ContextPublisher.class.isAssignableFrom(field.getType()));
            assertFalse(PublicationRequest.class.isAssignableFrom(field.getType()));
        }
    }

    /** Publication Context 关闭后，Diagnostic 快照入口也必须拒绝访问。 */
    @Test
    void diagnosticSnapshotRejectsUseAfterContextClose() {
        CompilationSession session = new CompilationSession(
                PipelineTestSupport.request(
                        new PipelineTestSupport.MutableClock(),
                        () -> false,
                        Optional.empty(),
                        new PipelineTestSupport.RecordingObserver()));
        PublicationPassContext context = new PublicationPassContext(session);

        context.close();

        assertThrows(IllegalStateException.class, context::diagnostics);
    }

    /** 创建完整、空 Registry 的冻结输入。 */
    private static CompiledModelSetBuilder.FrozenInput input() {
        return builder()
                .sourceManifest(PublishedSourceManifest.empty())
                .definitions(new CountingRegistry())
                .deferred(new CountingDeferredRegistry())
                .digestPair(new DigestPair("source-review", "semantic-review"))
                .freeze();
    }

    /** 创建稳定版本域 Builder。 */
    private static CompiledModelSetBuilder builder() {
        return new CompiledModelSetBuilder(
                "compiler-review",
                "schema-review",
                "options-review");
    }

    /** 统计并可拒绝阶段结束后读取的空 Definition Registry。 */
    private static final class CountingRegistry
            implements Registry<DefinitionKey, CompiledDefinition> {
        private final AtomicInteger keysReads = new AtomicInteger();
        private boolean rejectFurtherReads;

        @Override
        public Optional<CompiledDefinition> find(DefinitionKey key) {
            rejectIfClosed();
            return Optional.empty();
        }

        @Override
        public CompiledDefinition require(DefinitionKey key) {
            rejectIfClosed();
            throw new IllegalArgumentException("missing definition");
        }

        @Override
        public List<DefinitionKey> keys() {
            rejectIfClosed();
            keysReads.incrementAndGet();
            return Collections.emptyList();
        }

        @Override
        public int size() {
            rejectIfClosed();
            return 0;
        }

        /** 模拟调用方在阶段完成后封闭原 Registry。 */
        private void rejectIfClosed() {
            if (rejectFurtherReads) {
                throw new AssertionError("definitions reread after snapshot");
            }
        }
    }

    /** 统计并可拒绝阶段结束后读取的空 Deferred Registry。 */
    private static final class CountingDeferredRegistry
            implements DeferredRegistry {
        private final AtomicInteger keysReads = new AtomicInteger();
        private boolean rejectFurtherReads;

        @Override
        public List<DeferredDefinition> requiredBy(RequiredStage stage) {
            rejectIfClosed();
            return Collections.emptyList();
        }

        @Override
        public List<DeferredDefinition> ownedBy(DefinitionKey key) {
            rejectIfClosed();
            return Collections.emptyList();
        }

        @Override
        public Optional<DeferredDefinition> find(DeferredKey key) {
            rejectIfClosed();
            return Optional.empty();
        }

        @Override
        public List<DeferredKey> keys() {
            rejectIfClosed();
            keysReads.incrementAndGet();
            return Collections.emptyList();
        }

        @Override
        public int size() {
            rejectIfClosed();
            return 0;
        }

        /** 模拟调用方在阶段完成后封闭原 Deferred Registry。 */
        private void rejectIfClosed() {
            if (rejectFurtherReads) {
                throw new AssertionError("deferred reread after snapshot");
            }
        }
    }
}
