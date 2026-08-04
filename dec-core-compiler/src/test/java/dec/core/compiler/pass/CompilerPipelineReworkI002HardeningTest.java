package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CancellationToken;
import dec.core.compiler.api.CompilationObserver;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.Deadline;
import dec.core.compiler.api.MonotonicClock;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.PublicationStatus;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.context.EngineContext;
import dec.core.context.model.DiagnosticCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 / I002：封闭发布前 TOCTOU、状态读取和循环 artifact 边界。
 */
class CompilerPipelineReworkI002HardeningTest {

    /** PublicationPass 内在 publish 前请求取消时，外部 publisher 必须保持 0 次调用。 */
    @Test
    void cancellationImmediatelyBeforePublishBlocksCommit() {
        MutableCancellation cancellation = new MutableCancellation();
        CountingPublisher publisher = new CountingPublisher();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> {
            cancellation.cancel();
            context.publish(PipelineTestSupport.candidate());
            return PassResult.passed();
        }));

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                new MutableClock(),
                cancellation,
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_COMPILATION_CANCELLED));
    }

    /** PublicationPass 内在 publish 前达到 Deadline 时，外部 publisher 必须保持 0 次调用。 */
    @Test
    void deadlineImmediatelyBeforePublishBlocksCommit() {
        MutableClock clock = new MutableClock();
        CountingPublisher publisher = new CountingPublisher();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> {
            clock.setNow(100L);
            context.publish(PipelineTestSupport.candidate());
            return PassResult.passed();
        }));

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                clock,
                new MutableCancellation(),
                Optional.of(new Deadline(100L)));

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_COMPILATION_TIMED_OUT));
    }

    /** PublicationStatus 必须只读取一次，避免不稳定结果产生分裂终态。 */
    @Test
    void publicationStatusIsSnapshottedOnce() {
        UnstableStatusPublisher publisher = new UnstableStatusPublisher();

        PipelineExecutionResult result = execute(
                PipelineTestSupport.successfulPasses(new ArrayList<String>()),
                publisher,
                new MutableClock(),
                new MutableCancellation(),
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        assertEquals(1, publisher.calls());
        assertEquals(1, publisher.statusCalls());
    }

    /** 循环 artifact 图必须稳定失败，不能以 StackOverflowError 越过 Pipeline 边界。 */
    @Test
    void cyclicArtifactGraphFailsClosed() {
        CountingPublisher publisher = new CountingPublisher();
        List<Object> cyclic = new ArrayList<Object>();
        cyclic.add(cyclic);
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(0, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                context.putArtifact("cyclic", cyclic);
                return PassResult.passed();
            }
        });

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                new MutableClock(),
                new MutableCancellation(),
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "pipeline.pass.failure".equals(diagnostic.messageKey())));
    }

    /** 创建最终 Publication Pass。 */
    private static CompilerPass publicationPass(PublicationBehavior behavior) {
        return new PublicationCompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.PUBLICATION_PASS;
            }

            @Override
            public PassResult execute(PublicationPassContext context) {
                return behavior.execute(context);
            }
        };
    }

    /** 执行指定基础设施组合。 */
    private static PipelineExecutionResult execute(
            List<CompilerPass> passes,
            ContextPublisher publisher,
            MonotonicClock clock,
            CancellationToken cancellation,
            Optional<Deadline> deadline) {
        return new CompilerPipeline(passes).execute(
                PipelineTestSupport.request(
                        clock,
                        cancellation,
                        deadline,
                        new StableObserver()),
                PipelineTestSupport.publicationRequest(publisher));
    }

    /** Publication Pass 测试行为。 */
    private interface PublicationBehavior {
        PassResult execute(PublicationPassContext context);
    }

    /** 可变单调时钟；默认不自动前进。 */
    private static final class MutableClock implements MonotonicClock {
        private long now;

        /** 设置当前同域纳秒值。 */
        private void setNow(long value) {
            now = value;
        }

        @Override
        public long nanoTime() {
            return now;
        }
    }

    /** 可变取消令牌。 */
    private static final class MutableCancellation implements CancellationToken {
        private boolean cancelled;

        /** 请求取消。 */
        private void cancel() {
            cancelled = true;
        }

        @Override
        public boolean isCancellationRequested() {
            return cancelled;
        }
    }

    /** 记录 publisher 调用次数并返回 PUBLISHED。 */
    private static class CountingPublisher implements ContextPublisher {
        private int calls;

        @Override
        public PublicationResult publish(
                Optional<EngineContext> expectedCurrent,
                EngineContext candidate) {
            calls++;
            return PipelineTestSupport.publishedResult();
        }

        /** 返回真实外部调用次数。 */
        private int calls() {
            return calls;
        }
    }

    /** 返回第一次 PUBLISHED、后续 CONFLICT 的不稳定状态实现。 */
    private static final class UnstableStatusPublisher extends CountingPublisher {
        private int statusCalls;

        @Override
        public PublicationResult publish(
                Optional<EngineContext> expectedCurrent,
                EngineContext candidate) {
            super.publish(expectedCurrent, candidate);
            return new PublicationResult() {
                @Override
                public PublicationStatus status() {
                    statusCalls++;
                    return statusCalls == 1
                            ? PublicationStatus.PUBLISHED
                            : PublicationStatus.CONFLICT;
                }
            };
        }

        /** 返回 status() 的真实读取次数。 */
        private int statusCalls() {
            return statusCalls;
        }
    }

    /** 不改变任何编译事实的 Observer。 */
    private static final class StableObserver implements CompilationObserver {
        @Override
        public void onTiming(CompilationTiming timing) {
        }

        @Override
        public void onStateTransition(SessionStateTransition transition) {
        }
    }
}
