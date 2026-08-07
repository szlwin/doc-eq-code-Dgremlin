package dec.core.compiler.pass;

import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationResult;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.FailedCompilationResult;
import dec.core.compiler.api.ModelCompiler;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.PublicationStatus;
import dec.core.compiler.api.PublishedCompilationResult;
import dec.core.compiler.compiled.SemanticDigestInput;
import dec.core.compiler.source.SourcePolicy;
import dec.core.context.EngineContext;
import dec.core.context.model.CompiledModelSet;
import java.util.Objects;
import java.util.Optional;

/**
 * 使用冻结十阶段生产 Pipeline 的无状态 ModelCompiler 实现。
 */
final class StandardModelCompiler implements ModelCompiler {
    private final CompilerPipeline pipeline;

    /** 根据显式 SourcePolicy 构造固定生产 Pipeline。 */
    StandardModelCompiler(SourcePolicy sourcePolicy, String compilerVersion) {
        this.pipeline = StandardCompilerPasses.pipeline(
                Objects.requireNonNull(sourcePolicy, "sourcePolicy"),
                compilerVersion);
    }

    /**
     * 运行完整 Pipeline，并把发布成功时的同一个 candidate 包装为正式结果。
     */
    @Override
    public CompilationResult compileAndPublish(
            CompilationRequest request,
            PublicationRequest publicationRequest) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(publicationRequest, "publicationRequest");
        PublishedContextCapture capture = new PublishedContextCapture(
                publicationRequest.publisher());
        PipelineExecutionResult execution = pipeline.execute(
                request,
                new PublicationRequest(
                        publicationRequest.expectedCurrent(),
                        capture));
        if (execution.state() != CompilationSessionState.PUBLISHED) {
            return FailedCompilationResult.failed(execution.diagnostics());
        }

        EngineContext published = capture.published().orElseThrow(
                () -> new IllegalStateException(
                        "published pipeline result has no captured context"));
        CompiledModelSet modelSet = published.compiledModelSet();
        return PublishedCompilationResult.published(
                execution.diagnostics(),
                modelSet,
                published,
                modelSet.digestPair(),
                modelSet.compilerVersion(),
                modelSet.schemaVersion(),
                modelSet.optionsVersion(),
                SemanticDigestInput.DIGEST_ALGORITHM_VERSION);
    }

    /**
     * 记录外部 Publisher 确认提交的精确 candidate，并稳定缓存其状态结果。
     */
    private static final class PublishedContextCapture
            implements ContextPublisher {
        private final ContextPublisher delegate;
        private EngineContext published;

        private PublishedContextCapture(ContextPublisher delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
        }

        /** 委托同一个 CAS 请求；只有 PUBLISHED 才记录 candidate identity。 */
        @Override
        public PublicationResult publish(
                Optional<EngineContext> expectedCurrent,
                EngineContext candidate) {
            PublicationResult result = Objects.requireNonNull(
                    delegate.publish(expectedCurrent, candidate),
                    "publication result");
            PublicationStatus status = Objects.requireNonNull(
                    result.status(),
                    "publication status");
            if (status == PublicationStatus.PUBLISHED) {
                published = Objects.requireNonNull(candidate, "candidate");
            }
            return stable(status);
        }

        /** 返回外部 Publisher 已确认的精确 Context。 */
        private Optional<EngineContext> published() {
            return Optional.ofNullable(published);
        }

        /** 缓存一次读取到的状态，防止不稳定 PublicationResult 分裂判断。 */
        private static PublicationResult stable(final PublicationStatus status) {
            return new PublicationResult() {
                @Override
                public PublicationStatus status() {
                    return status;
                }
            };
        }
    }
}
