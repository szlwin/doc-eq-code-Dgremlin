package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationObserver;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.compiler.api.TimingPhase;
import dec.core.context.EngineContext;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T13 独立 Review：复核 Observer fail-open 与 Clock 边界。
 */
class CompilationObserverIndependentReviewTest {

    /** 每个 Timing 回调失败都必须可审计，但不能阻断发布。 */
    @Test
    void everyTimingObserverFailureBecomesWarning() {
        PipelineExecutionResult result = execute(new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                throw new IllegalStateException("timing");
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                // 仅隔离 Timing 回调。
            }
        }, new CountingClock());

        assertEquals(CompilationSessionState.PUBLISHED, result.state());
        assertEquals(13L, warningCount(
                result,
                "pipeline.observer.timing.failure"));
        assertFalse(result.artifacts().isEmpty());
    }

    /** 每个状态回调失败都必须登记 Warning，终态不能回滚。 */
    @Test
    void everyTransitionObserverFailureBecomesWarning() {
        PipelineExecutionResult result = execute(new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                // 仅隔离 Transition 回调。
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                throw new IllegalStateException("transition");
            }
        }, new CountingClock());

        assertEquals(CompilationSessionState.PUBLISHED, result.state());
        assertEquals(
                result.transitions().size(),
                warningCount(result, "pipeline.observer.transition.failure"));
        assertFalse(result.artifacts().isEmpty());
    }

    /** FAILED 转换的 Observer 异常只能追加 Warning，不能覆盖原 ERROR 或触发发布。 */
    @Test
    void failedTransitionObserverFailurePreservesOriginalFailure() {
        AtomicInteger publisherCalls = new AtomicInteger();
        CompilationObserver observer = new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                // 本用例只让 FAILED 状态转换回调失败。
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                if (transition.to() == CompilationSessionState.FAILED) {
                    throw new IllegalStateException("observer-failure");
                }
            }
        };
        ContextPublisher publisher = new ContextPublisher() {
            @Override
            public PublicationResult publish(
                    Optional<EngineContext> expectedCurrent,
                    EngineContext candidate) {
                publisherCalls.incrementAndGet();
                return PipelineTestSupport.publishedResult();
            }
        };

        PipelineExecutionResult result = new CompilerPipeline(
                PipelineTestSupport.failingPasses(
                        new ArrayList<String>(),
                        2)).execute(
                PipelineTestSupport.request(
                        new CountingClock(),
                        () -> false,
                        Optional.empty(),
                        observer),
                PipelineTestSupport.publicationRequest(publisher));

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(1L, result.diagnostics().stream().filter(diagnostic ->
                diagnostic.severity() == DiagnosticSeverity.ERROR
                        && "test.pass.error".equals(diagnostic.messageKey()))
                .count());
        assertEquals(
                1L,
                warningCount(result, "pipeline.observer.transition.failure"));
        assertEquals(0, publisherCalls.get());
        assertTrue(result.artifacts().isEmpty());
    }

    /** supplemental timing 必须复用同一 elapsed，且总 Clock 读取保持 20。 */
    @Test
    void supplementalTimingReusesPassClockReadsAndElapsed() {
        CountingClock clock = new CountingClock();
        PipelineExecutionResult result = execute(
                new PipelineTestSupport.RecordingObserver(),
                clock);

        assertEquals(20, clock.reads.get());
        assertEquals(13, result.timings().size());
        assertSupplementalMatchesPass(
                result.timings(),
                CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS,
                TimingPhase.DISCOVERY);
        assertSupplementalMatchesPass(
                result.timings(),
                CompilerPipeline.STRUCTURAL_VALIDATION_PASS,
                TimingPhase.PARSE);
        assertSupplementalMatchesPass(
                result.timings(),
                CompilerPipeline.DIGEST_PASS,
                TimingPhase.DIGEST);
    }

    /** observation 专用入口必须拒绝其他 code、ERROR 和 seal 后写入。 */
    @Test
    void observationDiagnosticBoundaryIsStrict() {
        CompilationSession session = new CompilationSession(
                PipelineTestSupport.request(
                        new CountingClock(),
                        () -> false,
                        Optional.empty(),
                        new PipelineTestSupport.RecordingObserver()));

        assertThrows(
                IllegalArgumentException.class,
                () -> session.addObservationDiagnostic(
                        PipelineDiagnostics.publicationBlocked()));
        assertThrows(
                IllegalArgumentException.class,
                () -> session.addObservationDiagnostic(
                        PipelineDiagnostics.clockFailure("pass")));

        session.transitionTo(CompilationSessionState.FAILED);
        session.seal();
        assertThrows(
                IllegalStateException.class,
                () -> session.addObservationDiagnostic(
                        PipelineDiagnostics.observerTimingFailure("PASS:test")));
    }

    /** 运行完整成功 Pipeline。 */
    private static PipelineExecutionResult execute(
            CompilationObserver observer,
            CountingClock clock) {
        return new CompilerPipeline(PipelineTestSupport.successfulPasses(
                new ArrayList<String>())).execute(
                PipelineTestSupport.request(
                        clock,
                        () -> false,
                        Optional.empty(),
                        observer),
                PipelineTestSupport.publicationRequest());
    }

    /** 统计指定 messageKey 的 Observer Warning。 */
    private static long warningCount(
            PipelineExecutionResult result,
            String messageKey) {
        return result.diagnostics().stream().filter(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_OBSERVER_FAILURE
                        && diagnostic.severity() == DiagnosticSeverity.WARNING
                        && messageKey.equals(diagnostic.messageKey()))
                .count();
    }

    /** 验证补充阶段与对应 PASS 使用相同 elapsed。 */
    private static void assertSupplementalMatchesPass(
            List<CompilationTiming> timings,
            String passName,
            TimingPhase phase) {
        long passElapsed = timings.stream().filter(timing ->
                timing.phase() == TimingPhase.PASS
                        && timing.pass().isPresent()
                        && passName.equals(timing.pass().get()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing pass timing"))
                .elapsedNanos();
        CompilationTiming supplemental = timings.stream().filter(timing ->
                timing.phase() == phase)
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing supplemental timing"));

        assertFalse(supplemental.pass().isPresent());
        assertEquals(passElapsed, supplemental.elapsedNanos());
    }

    /** 每次读取递增 1ns，并记录真实调用次数。 */
    private static final class CountingClock
            implements dec.core.compiler.api.MonotonicClock {
        private final AtomicInteger reads = new AtomicInteger();

        @Override
        public long nanoTime() {
            return reads.getAndIncrement();
        }
    }
}
