package dec.core.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.api.CompilationResult;
import dec.core.compiler.api.CompilationStatus;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.PublicationStatus;
import dec.core.compiler.api.PublishedCompilationResult;
import dec.core.compiler.source.SourceReference;
import dec.core.context.EngineContext;
import dec.core.context.projection.CoreConfigProjection;
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
        CompilerBootstrap bootstrap = CompilerBootstrap.builder()
                .classLoader(getClass().getClassLoader())
                .allowedRoot("classpath:stage-mix/")
                .publisher(publisher)
                .build();

        CompilationResult result = bootstrap.compileAndPublish(
                new SourceReference("classpath:stage-mix/orm-config.xml"),
                new CompilationOptions("1.0", "stage-closure-options"),
                Optional.<EngineContext>empty());

        assertEquals(CompilationStatus.PUBLISHED, result.status());
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

        CoreConfigProjection projection = bootstrap.projection(published);
        assertSame(published.engineContext().projection(), projection);
    }

    /**
     * 第二次编译失败时不得调用 Publisher，也不得覆盖此前成功 Context。
     */
    @Test
    void failedSecondCompilationPreservesPublishedContext() {
        RecordingPublisher publisher = new RecordingPublisher();
        CompilerBootstrap bootstrap = CompilerBootstrap.builder()
                .classLoader(getClass().getClassLoader())
                .allowedRoot("classpath:stage-mix/")
                .publisher(publisher)
                .build();

        CompilationResult first = bootstrap.compileAndPublish(
                new SourceReference("classpath:stage-mix/orm-config.xml"),
                new CompilationOptions("1.0", "stage-closure-options"),
                Optional.<EngineContext>empty());
        assertEquals(CompilationStatus.PUBLISHED, first.status());
        EngineContext published = publisher.current().get();
        int publicationCount = publisher.publicationCount();

        CompilationResult failed = bootstrap.compileAndPublish(
                new SourceReference("classpath:stage-mix/invalid-root.xml"),
                new CompilationOptions("1.0", "stage-closure-options"),
                Optional.of(published));

        assertEquals(CompilationStatus.FAILED, failed.status());
        assertSame(published, publisher.current().get());
        assertEquals(publicationCount, publisher.publicationCount());
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
