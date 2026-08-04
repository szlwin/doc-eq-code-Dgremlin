package dec.core.compiler.pass;

import dec.core.compiler.api.CancellationToken;
import dec.core.compiler.api.CompilationObserver;
import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.Deadline;
import dec.core.compiler.api.MonotonicClock;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.PublicationStatus;
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
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * TASK-P1-T12 测试共用的确定性请求、时钟、取消和 Pass 夹具。
 */
final class PipelineTestSupport {
    private PipelineTestSupport() {
    }

    /** 创建不触发真实 Source 或 Frontend 的完整请求。 */
    static CompilationRequest request(
            MutableClock clock,
            MutableCancellation cancellation,
            Optional<Deadline> deadline,
            RecordingObserver observer) {
        DocumentSourceProvider provider = new DocumentSourceProvider() {
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
        return new CompilationRequest(
                new SourceReference("classpath:mix/orm-config.xml"),
                provider,
                frontends,
                new CompilationOptions("schema-v1", "options-v1"),
                deadline,
                cancellation,
                clock,
                observer);
    }

    /** 创建返回 PUBLISHED 的无外部存储测试发布边界。 */
    static PublicationRequest publicationRequest() {
        return new PublicationRequest(
                Optional.<EngineContext>empty(),
                new ContextPublisher() {
                    @Override
                    public PublicationResult publish(
                            Optional<EngineContext> expectedCurrent,
                            EngineContext candidate) {
                        return publishedResult();
                    }
                });
    }

    /** 创建九个普通成功 Pass 和一个最终发布 Pass。 */
    static List<CompilerPass> successfulPasses(List<String> executions) {
        List<CompilerPass> result = new ArrayList<CompilerPass>();
        for (int index = 0; index < 9; index++) {
            result.add(new RecordingPass(
                    CompilerPipeline.fixedPassOrder().get(index),
                    executions));
        }
        result.add(new RecordingPublicationPass(executions, null, false));
        return result;
    }

    /** 创建指定位置返回 ERROR 的十 Pass 列表。 */
    static List<CompilerPass> failingPasses(
            List<String> executions,
            int failureIndex) {
        List<CompilerPass> result = successfulPasses(executions);
        Diagnostic diagnostic = error("test.pass.error", failureIndex);
        if (failureIndex == 9) {
            result.set(9, new RecordingPublicationPass(
                    executions,
                    diagnostic,
                    false));
        } else {
            result.set(failureIndex, new RecordingPass(
                    CompilerPipeline.fixedPassOrder().get(failureIndex),
                    executions,
                    diagnostic,
                    false));
        }
        return result;
    }

    /** 创建指定位置抛 RuntimeException 的十 Pass 列表。 */
    static List<CompilerPass> throwingPasses(
            List<String> executions,
            int failureIndex) {
        List<CompilerPass> result = successfulPasses(executions);
        if (failureIndex == 9) {
            result.set(9, new RecordingPublicationPass(
                    executions,
                    null,
                    true));
        } else {
            result.set(failureIndex, new RecordingPass(
                    CompilerPipeline.fixedPassOrder().get(failureIndex),
                    executions,
                    null,
                    true));
        }
        return result;
    }

    /** 构造稳定的测试 ERROR Diagnostic。 */
    static Diagnostic error(String messageKey, int ordinal) {
        return new Diagnostic(
                DiagnosticCode.MIX_PUBLICATION_BLOCKED,
                DiagnosticSeverity.ERROR,
                messageKey,
                null,
                new SourceRef(
                        "pipeline-test", ordinal + 1, 1,
                        "/pipeline/pass[" + ordinal + "]"),
                Collections.<SourceRef>emptyList(),
                null,
                "PipelineTestPass");
    }

    /** 创建最小、不可变的发布候选 Context。 */
    static EngineContext candidate() {
        CompiledModelSet modelSet = new CompiledModelSet(
                PublishedSourceManifest.empty(),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                Collections.<Diagnostic>emptyList(),
                new DigestPair("source-t12", "semantic-t12"),
                "compiler-t12",
                "schema-t12",
                "options-t12");
        return new EngineContext(modelSet);
    }

    /** 创建稳定的发布成功结果。 */
    static PublicationResult publishedResult() {
        return new PublicationResult() {
            @Override
            public PublicationStatus status() {
                return PublicationStatus.PUBLISHED;
            }
        };
    }

    /** 可由测试精确推进的单调时钟。 */
    static final class MutableClock implements MonotonicClock {
        private long now;
        private long step = 1L;

        /** 设置每次读取后的固定前进量。 */
        void setStep(long value) {
            this.step = value;
        }

        /** 直接设置当前同域纳秒值。 */
        void setNow(long value) {
            this.now = value;
        }

        @Override
        public long nanoTime() {
            long value = now;
            now += step;
            return value;
        }
    }

    /** 可由测试在指定时点请求取消的令牌。 */
    static final class MutableCancellation implements CancellationToken {
        private boolean cancelled;

        /** 更新取消状态。 */
        void cancel() {
            cancelled = true;
        }

        @Override
        public boolean isCancellationRequested() {
            return cancelled;
        }
    }

    /** 记录 timing 和状态转换，不改变任何编译事实。 */
    static final class RecordingObserver implements CompilationObserver {
        private final List<CompilationTiming> timings =
                new ArrayList<CompilationTiming>();
        private final List<SessionStateTransition> transitions =
                new ArrayList<SessionStateTransition>();

        @Override
        public void onTiming(CompilationTiming timing) {
            timings.add(timing);
        }

        @Override
        public void onStateTransition(SessionStateTransition transition) {
            transitions.add(transition);
        }

        /** 返回观察到的 timing 快照。 */
        List<CompilationTiming> timings() {
            return new ArrayList<CompilationTiming>(timings);
        }

        /** 返回观察到的状态转换快照。 */
        List<SessionStateTransition> transitions() {
            return new ArrayList<SessionStateTransition>(transitions);
        }
    }

    /** 可配置成功、ERROR 或 RuntimeException 的普通 Pass。 */
    static class RecordingPass implements CompilerPass {
        private final String name;
        private final List<String> executions;
        private final Diagnostic diagnostic;
        private final boolean throwsFailure;

        RecordingPass(String name, List<String> executions) {
            this(name, executions, null, false);
        }

        RecordingPass(
                String name,
                List<String> executions,
                Diagnostic diagnostic,
                boolean throwsFailure) {
            this.name = name;
            this.executions = executions;
            this.diagnostic = diagnostic;
            this.throwsFailure = throwsFailure;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public PassResult execute(PassContext context) {
            executions.add(name);
            context.putArtifact("last-pass", name);
            if (throwsFailure) {
                throw new IllegalStateException("controlled-pass-failure");
            }
            if (diagnostic != null) {
                return PassResult.of(Collections.singletonList(diagnostic));
            }
            return PassResult.passed();
        }
    }

    /** 可配置发布前 ERROR 或异常的最终 Publication Pass。 */
    private static final class RecordingPublicationPass
            implements PublicationCompilerPass {
        private final List<String> executions;
        private final Diagnostic diagnostic;
        private final boolean throwsFailure;

        private RecordingPublicationPass(
                List<String> executions,
                Diagnostic diagnostic,
                boolean throwsFailure) {
            this.executions = executions;
            this.diagnostic = diagnostic;
            this.throwsFailure = throwsFailure;
        }

        @Override
        public String name() {
            return CompilerPipeline.PUBLICATION_PASS;
        }

        @Override
        public PassResult execute(PublicationPassContext context) {
            executions.add(name());
            if (throwsFailure) {
                throw new IllegalStateException("controlled-publication-failure");
            }
            if (diagnostic != null) {
                return PassResult.of(Collections.singletonList(diagnostic));
            }
            context.publish(candidate());
            return PassResult.passed();
        }
    }
}
