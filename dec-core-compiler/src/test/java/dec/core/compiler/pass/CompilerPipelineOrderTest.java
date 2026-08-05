package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.Deadline;
import dec.core.compiler.api.SessionStateTransition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12：验证十个 Pass 的固定顺序和唯一成功状态路径。
 */
class CompilerPipelineOrderTest {

    /** 合法输入必须精确执行十个 Pass，并沿唯一状态路径进入 PUBLISHED。 */
    @Test
    void executesFixedPassOrderAndUniquePublishedStatePath() {
        List<String> executions = new ArrayList<String>();
        CompilerPipeline pipeline = new CompilerPipeline(
                PipelineTestSupport.successfulPasses(executions));
        PipelineTestSupport.MutableClock clock =
                new PipelineTestSupport.MutableClock();
        PipelineTestSupport.RecordingObserver observer =
                new PipelineTestSupport.RecordingObserver();

        PipelineExecutionResult result = pipeline.execute(
                PipelineTestSupport.request(
                        clock,
                        new PipelineTestSupport.MutableCancellation(),
                        Optional.<Deadline>empty(),
                        observer),
                PipelineTestSupport.publicationRequest());

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        assertEquals(CompilerPipeline.fixedPassOrder(), executions);
        assertEquals(CompilerPipeline.fixedPassOrder(), result.executedPasses());
        assertEquals(expectedTransitions(), result.transitions());
        assertEquals(13, result.timings().size());
        assertEquals(result.timings(), observer.timings());
        assertEquals(result.transitions(), observer.transitions());
        assertTrue(result.diagnostics().isEmpty());
    }

    /** 调换任意两个 Pass 必须在执行前拒绝。 */
    @Test
    void rejectsReorderedPassesBeforeExecution() {
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        Collections.swap(passes, 0, 1);

        assertThrows(IllegalArgumentException.class,
                () -> new CompilerPipeline(passes));
    }

    /** 缺失、重复或 null Pass 都必须在执行前拒绝。 */
    @Test
    void rejectsIncompleteDuplicateAndNullPassSets() {
        List<CompilerPass> missing = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        missing.remove(9);
        assertThrows(IllegalArgumentException.class,
                () -> new CompilerPipeline(missing));

        List<CompilerPass> duplicate = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        duplicate.set(1, duplicate.get(0));
        assertThrows(IllegalArgumentException.class,
                () -> new CompilerPipeline(duplicate));

        List<CompilerPass> containingNull = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        containingNull.set(4, null);
        assertThrows(NullPointerException.class,
                () -> new CompilerPipeline(containingNull));
    }

    /** Pipeline 必须防御性复制调用方 Pass List。 */
    @Test
    void defensivelyCopiesCallerPassList() {
        List<String> executions = new ArrayList<String>();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(executions);
        CompilerPipeline pipeline = new CompilerPipeline(passes);
        passes.clear();

        PipelineExecutionResult result = pipeline.execute(
                PipelineTestSupport.request(
                        new PipelineTestSupport.MutableClock(),
                        new PipelineTestSupport.MutableCancellation(),
                        Optional.<Deadline>empty(),
                        new PipelineTestSupport.RecordingObserver()),
                PipelineTestSupport.publicationRequest());

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        assertEquals(10, executions.size());
    }

    /** 构造 DESIGN-R38 冻结的九次成功状态转换。 */
    private static List<SessionStateTransition> expectedTransitions() {
        return Arrays.asList(
                new SessionStateTransition(
                        CompilationSessionState.CREATED,
                        CompilationSessionState.SOURCES_DISCOVERED),
                new SessionStateTransition(
                        CompilationSessionState.SOURCES_DISCOVERED,
                        CompilationSessionState.PARSED),
                new SessionStateTransition(
                        CompilationSessionState.PARSED,
                        CompilationSessionState.RAW_BUILT),
                new SessionStateTransition(
                        CompilationSessionState.RAW_BUILT,
                        CompilationSessionState.STRUCTURALLY_VALIDATED),
                new SessionStateTransition(
                        CompilationSessionState.STRUCTURALLY_VALIDATED,
                        CompilationSessionState.SYMBOLS_REGISTERED),
                new SessionStateTransition(
                        CompilationSessionState.SYMBOLS_REGISTERED,
                        CompilationSessionState.REFERENCES_RESOLVED),
                new SessionStateTransition(
                        CompilationSessionState.REFERENCES_RESOLVED,
                        CompilationSessionState.GRAPH_PREPARED),
                new SessionStateTransition(
                        CompilationSessionState.GRAPH_PREPARED,
                        CompilationSessionState.SEMANTICALLY_VALIDATED),
                new SessionStateTransition(
                        CompilationSessionState.SEMANTICALLY_VALIDATED,
                        CompilationSessionState.PUBLISHED));
    }
}
