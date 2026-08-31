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
 * 独立验证 Starter 只做实例级委托，并复用发布结果中的同源只读投影。
 */
class CompilerStarterBehaviorT15Test {

    /** 编译入口必须只委托一次，并保持输入和输出实例身份不变。 */
    @Test
    void compileAndPublishDelegatesExactlyOnceWithoutRepackingFacts() {
        CompilationRequest request = request();
        PublicationRequest publicationRequest = publicationRequest();
        PublishedCompilationResult expected = publishedResult();
        RecordingCompiler compiler = new RecordingCompiler(expected);

        CompilationResult actual = new CompilerStarter(compiler)
                .compileAndPublish(request, publicationRequest);

        assertEquals(1, compiler.invocationCount);
        assertSame(request, compiler.request);
        assertSame(publicationRequest, compiler.publicationRequest);
        assertSame(expected, actual);
    }

    /** Projection 必须来自发布结果持有的同一个 EngineContext，禁止复制或重建。 */
    @Test
    void projectionReusesPublishedEngineContextProjectionIdentity() {
        PublishedCompilationResult published = publishedResult();
        CompilerStarter starter = new CompilerStarter(new RecordingCompiler(published));

        assertSame(
                published.engineContext().projection(),
                starter.projection(published));
    }

    /** 非发布结果不得暴露候选投影，并返回稳定失败原因。 */
    @Test
    void projectionRejectsNonPublishedResultWithStableReason() {
        CompilerStarter starter = new CompilerStarter(
                new RecordingCompiler(nonPublishedResult()));

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> starter.projection(nonPublishedResult()));

        assertEquals(
                "projection requires a published compilation result",
                failure.getMessage());
    }

    /** 创建仅用于 identity 验证且不会真正解析 Source 的最小请求。 */
    private static CompilationRequest request() {
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
                // identity 测试不会执行编译，Observer 仅满足显式依赖合同。
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                // identity 测试不会执行编译，Observer 仅满足显式依赖合同。
            }
        };
        return new CompilationRequest(
                new SourceReference("classpath:test/root.xml"),
                sourceProvider,
                frontends,
                new CompilationOptions("schema-1", "options-1"),
                Optional.empty(),
                cancellationToken,
                clock,
                observer);
    }

    /** 创建不会被调用的最小条件发布边界。 */
    private static PublicationRequest publicationRequest() {
        ContextPublisher publisher = new ContextPublisher() {
            @Override
            public PublicationResult publish(
                    Optional<EngineContext> expectedCurrent,
                    EngineContext candidate) {
                return null;
            }
        };
        return new PublicationRequest(Optional.empty(), publisher);
    }

    /** 创建真实发布模型，确保 Projection identity 断言覆盖正式构造合同。 */
    private static PublishedCompilationResult publishedResult() {
        CompiledModelSet modelSet = new CompiledModelSet(
                PublishedSourceManifest.empty(),
                dec.core.context.model.CompiledViewMaterializationIndex.empty(),
                dec.core.context.model.ModelAccessPolicyIndex.empty(),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                Collections.<Diagnostic>emptyList(),
                new DigestPair("source", "semantic"),
                "compiler-1",
                "schema-1",
                "options-1");
        EngineContext context = new EngineContext(modelSet);
        return PublishedCompilationResult.published(
                modelSet.diagnostics(),
                modelSet,
                context,
                modelSet.digestPair(),
                modelSet.compilerVersion(),
                modelSet.schemaVersion(),
                modelSet.optionsVersion(),
                "sha-256-v1");
    }

    /** 创建不携带候选 Context 的稳定失败结果替身。 */
    private static CompilationResult nonPublishedResult() {
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

    /** 记录 Starter 对公共 Compiler 边界的真实委托事实。 */
    private static final class RecordingCompiler implements ModelCompiler {
        private final CompilationResult result;
        private int invocationCount;
        private CompilationRequest request;
        private PublicationRequest publicationRequest;

        private RecordingCompiler(CompilationResult result) {
            this.result = result;
        }

        @Override
        public CompilationResult compileAndPublish(
                CompilationRequest request,
                PublicationRequest publicationRequest) {
            invocationCount++;
            this.request = request;
            this.publicationRequest = publicationRequest;
            return result;
        }
    }
}
