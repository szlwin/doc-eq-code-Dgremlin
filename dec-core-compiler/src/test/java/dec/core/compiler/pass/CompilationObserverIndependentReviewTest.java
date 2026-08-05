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
import dec.core.context.model.Diagnostic;
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

    /**
     * FAILED 转换的 Observer 异常只能追加精确 Warning，不能改变原错误身份、
     * Pass 执行、状态转换、Timing、Publisher 或 Artifact。
     */
    @Test
    void failedTransitionObserverFailurePreservesOriginalFailure() {
        AtomicInteger controlPublisherCalls = new AtomicInteger();
        AtomicInteger observedPublisherCalls = new AtomicInteger();
        List<String> controlExecutions = new ArrayList<String>();
        List<String> observedExecutions = new ArrayList<String>();

        PipelineExecutionResult control = executeFailed(
                new PipelineTestSupport.RecordingObserver(),
                controlPublisherCalls,
                controlExecutions);
        PipelineExecutionResult observed = executeFailed(
                failedTransitionThrowingObserver(),
                observedPublisherCalls,
                observedExecutions);

        // Observer 失败不得改变 Pipeline 的任何执行事实或终态顺序。
        assertEquals(control.state(), observed.state());
        assertEquals(CompilationSessionState.FAILED, observed.state());
        assertEquals(control.executedPasses(), observed.executedPasses());
        assertEquals(controlExecutions, observedExecutions);
        assertEquals(control.transitions(), observed.transitions());
        assertEquals(control.timings(), observed.timings());

        // 原始 Pass ERROR 的完整身份必须在两组结果中保持一致。
        Diagnostic controlOriginal = requireDiagnostic(
                control,
                DiagnosticCode.MIX_PUBLICATION_BLOCKED,
                DiagnosticSeverity.ERROR,
                "test.pass.error");
        Diagnostic observedOriginal = requireDiagnostic(
                observed,
                DiagnosticCode.MIX_PUBLICATION_BLOCKED,
                DiagnosticSeverity.ERROR,
                "test.pass.error");
        assertEquals("PipelineTestPass", controlOriginal.pass());
        assertEquals(controlOriginal, observedOriginal);

        // 观察组唯一允许新增一个与真实 FAILED 转换一致的 Warning。
        Diagnostic warning = requireDiagnostic(
                observed,
                DiagnosticCode.MIX_OBSERVER_FAILURE,
                DiagnosticSeverity.WARNING,
                "pipeline.observer.transition.failure");
        assertEquals("STRUCTURALLY_VALIDATED->FAILED", warning.pass());
        assertEquals(control.diagnostics().size() + 1, observed.diagnostics().size());
        assertEquals(0L, warningCount(
                control,
                "pipeline.observer.transition.failure"));
        assertEquals(1L, warningCount(
                observed,
                "pipeline.observer.transition.failure"));

        assertEquals(0, controlPublisherCalls.get());
        assertEquals(0, observedPublisherCalls.get());
        assertTrue(control.artifacts().isEmpty());
        assertTrue(observed.artifacts().isEmpty());
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

    /** 构造只在真实 FAILED 状态转换时抛异常的 Observer。 */
    private static CompilationObserver failedTransitionThrowingObserver() {
        return new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                // 本用例不干扰 Timing 回调。
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                if (transition.to() == CompilationSessionState.FAILED) {
                    throw new IllegalStateException("observer-failure");
                }
            }
        };
    }

    /**
     * 使用固定失败位置执行 Pipeline，确保控制组和观察组只有 Observer 行为不同。
     */
    private static PipelineExecutionResult executeFailed(
            CompilationObserver observer,
            AtomicInteger publisherCalls,
            List<String> executions) {
        ContextPublisher publisher = new ContextPublisher() {
            @Override
            public PublicationResult publish(
                    Optional<EngineContext> expectedCurrent,
                    EngineContext candidate) {
                publisherCalls.incrementAndGet();
                return PipelineTestSupport.publishedResult();
            }
        };
        return new CompilerPipeline(
                PipelineTestSupport.failingPasses(executions, 2)).execute(
                PipelineTestSupport.request(
                        new CountingClock(),
                        () -> false,
                        Optional.empty(),
                        observer),
                PipelineTestSupport.publicationRequest(publisher));
    }

    /** 按完整身份查找唯一 Diagnostic，避免只按 severity 或 messageKey 误命中。 */
    private static Diagnostic requireDiagnostic(
            PipelineExecutionResult result,
            DiagnosticCode code,
            DiagnosticSeverity severity,
            String messageKey) {
        List<Diagnostic> matches = new ArrayList<Diagnostic>();
        for (Diagnostic diagnostic : result.diagnostics()) {
            if (diagnostic.code() == code
                    && diagnostic.severity() == severity
                    && messageKey.equals(diagnostic.messageKey())) {
                matches.add(diagnostic);
            }
        }
        assertEquals(1, matches.size());
        return matches.get(0);
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
