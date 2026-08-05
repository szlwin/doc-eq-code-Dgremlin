package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationObserver;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.compiler.api.TimingPhase;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T13 / I001：Observer Warning 与稳定 Timing phase 的有效 RED Oracle。
 */
class CompilationObserverTest {

    /** 完整成功路径必须同时暴露 discovery/parse/pass/digest 计时。 */
    @Test
    void successfulPipelineReportsAllStableTimingPhases() {
        PipelineTestSupport.RecordingObserver observer =
                new PipelineTestSupport.RecordingObserver();

        PipelineExecutionResult result = execute(observer);

        Map<TimingPhase, Integer> counts = phaseCounts(result.timings());
        assertEquals(CompilationSessionState.PUBLISHED, result.state());
        assertEquals(Integer.valueOf(1), counts.get(TimingPhase.DISCOVERY));
        assertEquals(Integer.valueOf(1), counts.get(TimingPhase.PARSE));
        assertEquals(Integer.valueOf(10), counts.get(TimingPhase.PASS));
        assertEquals(Integer.valueOf(1), counts.get(TimingPhase.DIGEST));
        assertEquals(13, result.timings().size());
        assertEquals(result.timings(), observer.timings());
    }

    /** Timing Observer 失败必须形成非阻断 Warning，不能静默吞掉。 */
    @Test
    void timingObserverFailureAddsWarningWithoutChangingPublication() {
        CompilationObserver observer = new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                throw new IllegalStateException("controlled-timing-failure");
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                // 状态回调保持正常，用于隔离 timing failure。
            }
        };

        PipelineExecutionResult result = execute(observer);

        assertEquals(CompilationSessionState.PUBLISHED, result.state());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_OBSERVER_FAILURE
                        && diagnostic.severity() == DiagnosticSeverity.WARNING
                        && "pipeline.observer.timing.failure".equals(
                                diagnostic.messageKey())));
        assertFalse(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.severity() == DiagnosticSeverity.ERROR));
    }

    /** 终态 Transition Observer 失败仍必须保留 PUBLISHED，并登记 Warning。 */
    @Test
    void transitionObserverFailureAfterCommitCannotRollbackPublishedState() {
        CompilationObserver observer = new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                // Timing 回调保持正常，用于隔离 transition failure。
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                throw new IllegalStateException("controlled-transition-failure");
            }
        };

        PipelineExecutionResult result = execute(observer);

        assertEquals(CompilationSessionState.PUBLISHED, result.state());
        assertTrue(result.candidate().isPresent());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_OBSERVER_FAILURE
                        && diagnostic.severity() == DiagnosticSeverity.WARNING
                        && "pipeline.observer.transition.failure".equals(
                                diagnostic.messageKey())));
        assertFalse(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.severity() == DiagnosticSeverity.ERROR));
    }

    /** 两类 Observer 均失败时不得减少 Timing 或状态事实。 */
    @Test
    void repeatedObserverFailuresPreserveAllCompilationFacts() {
        CompilationObserver observer = new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                throw new IllegalStateException("timing");
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                throw new IllegalStateException("transition");
            }
        };

        PipelineExecutionResult result = execute(observer);

        assertEquals(CompilationSessionState.PUBLISHED, result.state());
        assertEquals(10, result.executedPasses().size());
        assertEquals(13, result.timings().size());
        assertFalse(result.transitions().isEmpty());
        assertTrue(result.diagnostics().stream().filter(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_OBSERVER_FAILURE
                        && diagnostic.severity() == DiagnosticSeverity.WARNING)
                .count() >= 2L);
    }

    /** 执行完整成功 Pipeline。 */
    private static PipelineExecutionResult execute(CompilationObserver observer) {
        return new CompilerPipeline(PipelineTestSupport.successfulPasses(
                new ArrayList<String>())).execute(
                PipelineTestSupport.request(
                        new PipelineTestSupport.MutableClock(),
                        new PipelineTestSupport.MutableCancellation(),
                        Optional.empty(),
                        observer),
                PipelineTestSupport.publicationRequest());
    }

    /** 按 phase 统计计时条目。 */
    private static Map<TimingPhase, Integer> phaseCounts(
            List<CompilationTiming> timings) {
        Map<TimingPhase, Integer> result =
                new EnumMap<TimingPhase, Integer>(TimingPhase.class);
        for (TimingPhase phase : TimingPhase.values()) {
            result.put(phase, Integer.valueOf(0));
        }
        for (CompilationTiming timing : timings) {
            result.put(
                    timing.phase(),
                    Integer.valueOf(result.get(timing.phase()).intValue() + 1));
        }
        return result;
    }
}
