package dec.core.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dec.core.compiler.api.CancellationToken;
import dec.core.compiler.api.CompilationObserver;
import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationResult;
import dec.core.compiler.api.CompilationStatus;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.ModelCompiler;
import dec.core.compiler.api.MonotonicClock;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.PublishedCompilationResult;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendRegistry;
import dec.core.compiler.source.DocumentSourceProvider;
import dec.core.compiler.source.SourceReference;
import dec.core.compiler.source.SourceResolutionContext;
import dec.core.compiler.source.SourceResolutionResult;
import dec.core.context.CoreConfigProjection;
import dec.core.context.EngineContext;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.PublishedSourceManifest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T15 独立 Review：验证 Starter 的实例委托与同源 Projection 行为。
 */
class CompilerStarterBehaviorT15IndependentReviewTest {

    /**
     * Starter 必须只调用注入的 Compiler 一次，并保持输入与返回结果的对象同一性。
     */
    @Test
    void compileAndPublishDelegatesExactlyOnceWithSameInstances() {
        CompilationResult expectedResult = terminalResult();
        RecordingCompiler compiler = new RecordingCompiler(expectedResult);
        CompilerStarter starter = new CompilerStarter(compiler);
        CompilationRequest request = compilationRequest();
        PublicationRequest publicationRequest = publicationRequest();

        CompilationResult actualResult = starter.compileAndPublish(
                request,
                publicationRequest);

        assertSame(expectedResult, actualResult);
        assertEquals(1, compiler.callCount);
        assertSame(request, compiler.actualRequest);
        assertSame(publicationRequest, compiler.actualPublicationRequest);
    }

    /**
     * 参数校验必须在委托前完成，避免无效输入触发 Compiler 副作用。
     */
    @Test
    void rejectsNullInputsBeforeInvokingCompiler() {
        RecordingCompiler compiler = new RecordingCompiler(terminalResult());
        CompilerStarter starter = new CompilerStarter(compiler);
        CompilationRequest request = compilationRequest();
        PublicationRequest publicationRequest = publicationRequest();

        NullPointerException compilerError = assertThrows(
                NullPointerException.class,
                () -> new CompilerStarter(null));
        NullPointerException requestError = assertThrows(
                NullPointerException.class,
                () -> starter.compileAndPublish(null, publicationRequest));
        NullPointerException publicationError = assertThrows(
                NullPointerException.class,
                () -> starter.compileAndPublish(request, null));

        assertEquals("compiler", compilerError.getMessage());
        assertEquals("request", requestError.getMessage());
        assertEquals("publicationRequest", publicationError.getMessage());
        assertEquals(0, compiler.callCount);
    }

    /**
     * Projection 必须直接来自已发布结果持有的同一个 EngineContext。
     */
    @Test
    void projectionReturnsTheExactPublishedContextProjection() {
        PublishedCompilationResult result = publishedResult();
        CompilerStarter starter = new CompilerStarter(
                new RecordingCompiler(result));

        CoreConfigProjection projection = starter.projection(result);

        assertSame(result.engineContext().projection(), projection);
        assertSame(projection, starter.projection(result));
    }

    /**
     * 非发布结果和空结果必须稳定拒绝，不能伪造可读取 Projection。
     */
    @Test
    void projectionRejectsNullAndNonPublishedResults() {
        CompilerStarter starter = new CompilerStarter(
                new RecordingCompiler(terminalResult()));

        NullPointerException nullError = assertThrows(
                NullPointerException.class,
                () -> starter.projection(null));
        IllegalStateException stateError = assertThrows(
                IllegalStateException.class,
                () -> starter.projection(terminalResult()));

        assertEquals("result", nullError.getMessage());
        assertEquals(
                "projection requires a published compilation result",
                stateError.getMessage());
    }

    /**
     * 创建只用于验证委托对象同一性的完整 CompilationRequest。
     */
    private static CompilationRequest compilationRequest() {
        DocumentSourceProvider sourceProvider = new DocumentSourceProvider() {
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
        FrontendRegistry frontends = new FrontendRegistry() {
            @Override
            public DocumentFrontend require(DocumentFormat format) {
                return null;
            }
        };
        CancellationToken cancellationToken = new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        };
        MonotonicClock clock = new MonotonicClock() {
            @Override
            public long nanoTime() {
                return 0L;
            }
        };
        CompilationObserver observer = new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                // 测试替身不产生外部状态。
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                // 测试替身不产生外部状态。
            }
        };
        return new CompilationRequest(
                new SourceReference("memory:/t15/root.xml"),
                sourceProvider,
                frontends,
                new CompilationOptions("schema-1", "options-1"),
                Optional.empty(),
                cancellationToken,
                clock,
                observer);
    }

    /**
     * 创建不会被 Starter 主动调用的条件发布边界测试替身。
     */
    private static PublicationRequest publicationRequest() {
        ContextPublisher publisher = new ContextPublisher() {
            @Override
            public PublicationResult publish(
                    Optional<EngineContext> expectedCurrent,
                    EngineContext candidate) {
                return null;
            }
        };
        return new PublicationRequest(Optional.<EngineContext>empty(), publisher);
    }

    /**
     * 创建最小合法发布事实，验证 Projection 与 EngineContext 的同源同一性。
     */
    private static PublishedCompilationResult publishedResult() {
        DigestPair digests = new DigestPair("source-digest", "semantic-digest");
        CompiledModelSet modelSet = new CompiledModelSet(
                PublishedSourceManifest.empty(),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                Collections.<Diagnostic>emptyList(),
                digests,
                "compiler-1",
                "schema-1",
                "options-1");
        EngineContext engineContext = new EngineContext(modelSet);
        return PublishedCompilationResult.published(
                modelSet.diagnostics(),
                modelSet,
                engineContext,
                digests,
                "compiler-1",
                "schema-1",
                "options-1",
                "sha-256-v1");
    }

    /**
     * 创建不带候选 Context 的普通终态结果，用于委托和拒绝路径验证。
     */
    private static CompilationResult terminalResult() {
        return new CompilationResult() {
            @Override
            public CompilationStatus status() {
                return CompilationStatus.FAILED;
            }

            @Override
            public List<Diagnostic> diagnostics() {
                return Collections.emptyList();
            }
        };
    }

    /**
     * 记录 Starter 委托事实，不执行真实编译或发布副作用。
     */
    private static final class RecordingCompiler implements ModelCompiler {
        private final CompilationResult result;
        private int callCount;
        private CompilationRequest actualRequest;
        private PublicationRequest actualPublicationRequest;

        private RecordingCompiler(CompilationResult result) {
            this.result = result;
        }

        @Override
        public CompilationResult compileAndPublish(
                CompilationRequest request,
                PublicationRequest publicationRequest) {
            callCount++;
            actualRequest = request;
            actualPublicationRequest = publicationRequest;
            return result;
        }
    }
}
