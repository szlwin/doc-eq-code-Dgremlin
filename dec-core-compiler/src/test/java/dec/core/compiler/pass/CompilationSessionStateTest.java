package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.Deadline;
import dec.core.context.model.DiagnosticCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12：验证 ERROR、异常、取消、超时和终态门禁。
 */
class CompilationSessionStateTest {

    /** 中间 Pass 返回 ERROR 后必须进入 FAILED，后续 Pass 和发布均不执行。 */
    @Test
    void stopsAfterPassErrorAndSkipsPublication() {
        List<String> executions = new ArrayList<String>();
        CompilerPipeline pipeline = new CompilerPipeline(
                PipelineTestSupport.failingPasses(executions, 4));

        PipelineExecutionResult result = execute(
                pipeline,
                new PipelineTestSupport.MutableClock(),
                new PipelineTestSupport.MutableCancellation(),
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(5, executions.size());
        assertFalse(executions.contains(CompilerPipeline.PUBLICATION_PASS));
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_PUBLICATION_BLOCKED));
    }

    /** Pass 抛 RuntimeException 必须收敛为 FAILED，不允许异常越过结果边界。 */
    @Test
    void convertsPassRuntimeExceptionToStableFailure() {
        List<String> executions = new ArrayList<String>();
        CompilerPipeline pipeline = new CompilerPipeline(
                PipelineTestSupport.throwingPasses(executions, 3));

        PipelineExecutionResult result = execute(
                pipeline,
                new PipelineTestSupport.MutableClock(),
                new PipelineTestSupport.MutableCancellation(),
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(4, executions.size());
        assertFalse(executions.contains(CompilerPipeline.PUBLICATION_PASS));
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "pipeline.pass.failure".equals(diagnostic.messageKey())));
    }

    /** 首个 Pass 前已取消时不得执行任何 Pass。 */
    @Test
    void cancellationBeforeFirstPassFailsWithoutExecution() {
        PipelineTestSupport.MutableCancellation cancellation =
                new PipelineTestSupport.MutableCancellation();
        cancellation.cancel();
        List<String> executions = new ArrayList<String>();

        PipelineExecutionResult result = execute(
                new CompilerPipeline(
                        PipelineTestSupport.successfulPasses(executions)),
                new PipelineTestSupport.MutableClock(),
                cancellation,
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertTrue(executions.isEmpty());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_COMPILATION_CANCELLED));
    }

    /** 首个 Pass 前达到 Deadline 时不得执行任何 Pass。 */
    @Test
    void timeoutBeforeFirstPassFailsWithoutExecution() {
        PipelineTestSupport.MutableClock clock =
                new PipelineTestSupport.MutableClock();
        clock.setNow(10L);
        List<String> executions = new ArrayList<String>();

        PipelineExecutionResult result = execute(
                new CompilerPipeline(
                        PipelineTestSupport.successfulPasses(executions)),
                clock,
                new PipelineTestSupport.MutableCancellation(),
                Optional.of(new Deadline(10L)));

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertTrue(executions.isEmpty());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_COMPILATION_TIMED_OUT));
    }

    /** PUBLISHED 和 FAILED 都是终态，所有语义写入必须拒绝。 */
    @Test
    void terminalStatesRejectFurtherMutation() {
        CompilationSession published = newSession();
        transitionSuccessPath(published);
        assertEquals(CompilationSessionState.PUBLISHED, published.state());
        assertThrows(IllegalStateException.class,
                () -> published.transitionTo(CompilationSessionState.FAILED));
        assertThrows(IllegalStateException.class,
                () -> published.putArtifact("late", "mutation"));
        assertThrows(IllegalStateException.class,
                () -> published.addDiagnostics(java.util.Collections.singletonList(
                        PipelineTestSupport.error("late.error", 1))));

        CompilationSession failed = newSession();
        failed.transitionTo(CompilationSessionState.FAILED);
        assertThrows(IllegalStateException.class,
                () -> failed.transitionTo(CompilationSessionState.PUBLISHED));
        assertThrows(IllegalStateException.class,
                () -> failed.putArtifact("late", "mutation"));
    }

    /** 创建测试专用非终态 Session。 */
    private static CompilationSession newSession() {
        return new CompilationSession(PipelineTestSupport.request(
                new PipelineTestSupport.MutableClock(),
                new PipelineTestSupport.MutableCancellation(),
                Optional.<Deadline>empty(),
                new PipelineTestSupport.RecordingObserver()));
    }

    /** 沿唯一成功状态路径推进到 PUBLISHED。 */
    private static void transitionSuccessPath(CompilationSession session) {
        session.transitionTo(CompilationSessionState.SOURCES_DISCOVERED);
        session.transitionTo(CompilationSessionState.PARSED);
        session.transitionTo(CompilationSessionState.RAW_BUILT);
        session.transitionTo(CompilationSessionState.STRUCTURALLY_VALIDATED);
        session.transitionTo(CompilationSessionState.SYMBOLS_REGISTERED);
        session.transitionTo(CompilationSessionState.REFERENCES_RESOLVED);
        session.transitionTo(CompilationSessionState.GRAPH_PREPARED);
        session.transitionTo(CompilationSessionState.SEMANTICALLY_VALIDATED);
        session.transitionTo(CompilationSessionState.PUBLISHED);
    }

    /** 执行指定 Pipeline。 */
    private static PipelineExecutionResult execute(
            CompilerPipeline pipeline,
            PipelineTestSupport.MutableClock clock,
            PipelineTestSupport.MutableCancellation cancellation,
            Optional<Deadline> deadline) {
        return pipeline.execute(
                PipelineTestSupport.request(
                        clock,
                        cancellation,
                        deadline,
                        new PipelineTestSupport.RecordingObserver()),
                PipelineTestSupport.publicationRequest());
    }
}
