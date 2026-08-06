package dec.core.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationObserver;
import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.api.CompilationResult;
import dec.core.compiler.api.CompilationStatus;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.PublicationStatus;
import dec.core.compiler.api.PublishedCompilationResult;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.compiler.api.TimingPhase;
import dec.core.compiler.pass.CompilerPipeline;
import dec.core.compiler.source.SourceReference;
import dec.core.context.CoreConfigProjection;
import dec.core.context.EngineContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/**
 * P1 阶段收口：验证生产 Bootstrap 使用真实 mix 完成一键编译与原子发布。
 */
class CompilerBootstrapStageClosureTest {

    /**
     * 生产入口必须组装 Provider、XML/YAML Frontend、十阶段 Pipeline 和 Publisher。
     */
    @Test
    void compilesAndPublishesRealMixedFrontendFixture() {
        RecordingPublisher publisher = new RecordingPublisher();
        RecordingObserver observer = new RecordingObserver();
        CompilerBootstrap bootstrap = bootstrap(publisher, observer);

        CompilationResult result = compile(
                bootstrap,
                "classpath:stage-mix/orm-config.xml",
                Optional.<EngineContext>empty());

        assertEquals(
                CompilationStatus.PUBLISHED,
                result.status(),
                result.diagnostics().toString());
        PublishedCompilationResult published =
                (PublishedCompilationResult) result;
        assertSame(publisher.current().get(), published.engineContext());
        assertSame(
                published.modelSet(),
                published.engineContext().compiledModelSet());
        assertEquals(10, published.modelSet().sourceManifest().sources().size());
        assertTrue(published.modelSet().sourceManifest().sources().stream()
                .anyMatch(source -> "XML".equals(source.format())));
        assertTrue(published.modelSet().sourceManifest().sources().stream()
                .anyMatch(source -> "YAML".equals(source.format())));
        assertTrue(published.modelSet().definitions().size() > 0);
        assertTrue(published.modelSet().deferred().size() > 0);
        assertEquals(CompilerPipeline.fixedPassOrder(), observer.passOrder());

        CoreConfigProjection projection = bootstrap.projection(published);
        assertSame(published.engineContext().projection(), projection);
    }

    /**
     * 相同真实输入在独立 Bootstrap 中必须产生相同 Source 与 Semantic Digest。
     */
    @Test
    void producesDeterministicDigestsForIdenticalMixedInput() {
        PublishedCompilationResult first = published(bootstrap(
                new RecordingPublisher(),
                new RecordingObserver()));
        PublishedCompilationResult second = published(bootstrap(
                new RecordingPublisher(),
                new RecordingObserver()));

        assertEquals(first.digests(), second.digests());
        assertEquals(
                first.modelSet().sourceManifest(),
                second.modelSet().sourceManifest());
        assertEquals(
                first.modelSet().definitions(),
                second.modelSet().definitions());
        assertEquals(
                first.modelSet().deferred(),
                second.modelSet().deferred());
    }

    /**
     * 第二次编译失败时不得调用 Publisher，也不得覆盖此前成功 Context。
     */
    @Test
    void failedSecondCompilationPreservesPublishedContext() {
        RecordingPublisher publisher = new RecordingPublisher();
        CompilerBootstrap bootstrap = bootstrap(
                publisher,
                new RecordingObserver());

        CompilationResult first = compile(
                bootstrap,
                "classpath:stage-mix/orm-config.xml",
                Optional.<EngineContext>empty());
        assertEquals(
                CompilationStatus.PUBLISHED,
                first.status(),
                first.diagnostics().toString());
        EngineContext published = publisher.current().get();
        int publicationCount = publisher.publicationCount();

        CompilationResult failed = compile(
                bootstrap,
                "classpath:stage-mix/invalid-root.xml",
                Optional.of(published));

        assertEquals(
                CompilationStatus.FAILED,
                failed.status(),
                failed.diagnostics().toString());
        assertSame(published, publisher.current().get());
        assertEquals(publicationCount, publisher.publicationCount());
    }

    /** 创建绑定真实 fixture、安全根和显式基础设施的生产 Bootstrap。 */
    private CompilerBootstrap bootstrap(
            RecordingPublisher publisher,
            CompilationObserver observer) {
        return CompilerBootstrap.builder()
                .classLoader(getClass().getClassLoader())
                .allowedRoot("classpath:stage-mix/")
                .publisher(publisher)
                .observer(observer)
                .build();
    }

    /** 使用固定真实根和编译选项执行一次生产入口。 */
    private static CompilationResult compile(
            CompilerBootstrap bootstrap,
            String root,
            Optional<EngineContext> expectedCurrent) {
        return bootstrap.compileAndPublish(
                new SourceReference(root),
                new CompilationOptions("1.0", "stage-closure-options"),
                expectedCurrent);
    }

    /** 执行一次成功编译并收窄为 Published 结果。 */
    private static PublishedCompilationResult published(
            CompilerBootstrap bootstrap) {
        CompilationResult result = compile(
                bootstrap,
                "classpath:stage-mix/orm-config.xml",
                Optional.<EngineContext>empty());
        assertEquals(
                CompilationStatus.PUBLISHED,
                result.status(),
                result.diagnostics().toString());
        return (PublishedCompilationResult) result;
    }

    /** 记录真实十阶段 Timing，不改变编译语义事实。 */
    private static final class RecordingObserver implements CompilationObserver {
        private final List<String> passOrder = new ArrayList<String>();

        /** 只记录 PASS 计时中的稳定阶段名。 */
        @Override
        public void onTiming(CompilationTiming timing) {
            if (timing.phase() == TimingPhase.PASS) {
                passOrder.add(timing.pass().get());
            }
        }

        /** 状态转换由既有 Pipeline 测试覆盖，此处不保存额外状态。 */
        @Override
        public void onStateTransition(SessionStateTransition transition) {
            // 本 Oracle 只需证明真实固定十阶段均被执行。
        }

        /** 返回实际执行 Pass 的防御性快照。 */
        private List<String> passOrder() {
            return new ArrayList<String>(passOrder);
        }
    }

    /** 记录实际 CAS 提交事实，供成功和失败不污染断言共用。 */
    private static final class RecordingPublisher implements ContextPublisher {
        private final AtomicReference<EngineContext> current =
                new AtomicReference<EngineContext>();
        private int publicationCount;

        /** 仅当 expectedCurrent 与当前实例完全一致时提交候选 Context。 */
        @Override
        public synchronized PublicationResult publish(
                Optional<EngineContext> expectedCurrent,
                EngineContext candidate) {
            EngineContext expected = expectedCurrent.orElse(null);
            if (current.get() != expected) {
                return result(PublicationStatus.CONFLICT);
            }
            current.set(candidate);
            publicationCount++;
            return result(PublicationStatus.PUBLISHED);
        }

        /** 返回当前已发布 Context 的只读快照。 */
        private Optional<EngineContext> current() {
            return Optional.ofNullable(current.get());
        }

        /** 返回实际成功提交次数。 */
        private int publicationCount() {
            return publicationCount;
        }

        /** 创建稳定的发布结果值。 */
        private static PublicationResult result(final PublicationStatus status) {
            return new PublicationResult() {
                @Override
                public PublicationStatus status() {
                    return status;
                }
            };
        }
    }
}
