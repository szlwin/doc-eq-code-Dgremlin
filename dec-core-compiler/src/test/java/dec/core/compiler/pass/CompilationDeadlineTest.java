package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CancellationToken;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.Deadline;
import dec.core.compiler.api.MonotonicClock;
import dec.core.context.model.DiagnosticCode;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T13 / I001：Deadline、Cancel 与 supplemental timing 的回归 Oracle。
 */
class CompilationDeadlineTest {

    /** start timestamp 精确等于 Deadline 时不得执行首个 Pass。 */
    @Test
    void exactStartDeadlineStopsBeforeFirstPass() {
        CountingClock clock = new CountingClock(10L, 0L);
        ArrayList<String> executions = new ArrayList<String>();

        PipelineExecutionResult result = execute(
                clock,
                () -> false,
                Optional.of(new Deadline(10L)),
                executions);

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertTrue(executions.isEmpty());
        assertTrue(result.timings().isEmpty());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_COMPILATION_TIMED_OUT));
    }

    /** 首个 Pass 前取消必须保持 publisher 与所有 Pass 不可达。 */
    @Test
    void cancellationStopsBeforeFirstPass() {
        ArrayList<String> executions = new ArrayList<String>();

        PipelineExecutionResult result = execute(
                new CountingClock(0L, 1L),
                () -> true,
                Optional.empty(),
                executions);

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertTrue(executions.isEmpty());
        assertTrue(result.timings().isEmpty());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_COMPILATION_CANCELLED));
    }

    /** DISCOVERY/PARSE/DIGEST supplemental timing 必须复用 Pass elapsed，不得新增 Clock 读取。 */
    @Test
    void supplementalTimingDoesNotReadClockAgain() {
        CountingClock clock = new CountingClock(0L, 1L);
        ArrayList<String> executions = new ArrayList<String>();

        PipelineExecutionResult result = execute(
                clock,
                () -> false,
                Optional.empty(),
                executions);

        assertEquals(CompilationSessionState.PUBLISHED, result.state());
        assertEquals(10, executions.size());
        assertEquals(20, clock.reads());
        assertEquals(13, result.timings().size());
        assertFalse(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_COMPILATION_TIMED_OUT));
    }

    /** 执行指定 Clock/Cancel/Deadline 的 Pipeline。 */
    private static PipelineExecutionResult execute(
            MonotonicClock clock,
            CancellationToken cancellation,
            Optional<Deadline> deadline,
            ArrayList<String> executions) {
        return new CompilerPipeline(PipelineTestSupport.successfulPasses(executions))
                .execute(
                        PipelineTestSupport.request(
                                clock,
                                cancellation,
                                deadline,
                                new PipelineTestSupport.RecordingObserver()),
                        PipelineTestSupport.publicationRequest());
    }

    /** 统计读取次数的确定性单调时钟。 */
    private static final class CountingClock implements MonotonicClock {
        private long current;
        private final long step;
        private final AtomicInteger reads = new AtomicInteger();

        private CountingClock(long current, long step) {
            this.current = current;
            this.step = step;
        }

        @Override
        public long nanoTime() {
            reads.incrementAndGet();
            long value = current;
            current += step;
            return value;
        }

        /** 返回真实 Clock 读取次数。 */
        private int reads() {
            return reads.get();
        }
    }
}
