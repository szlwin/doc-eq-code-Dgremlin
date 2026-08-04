package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.Deadline;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12：验证 Session-local 数据隔离、不可变快照和无全局状态。
 */
class SessionIsolationTest {

    /** 连续执行必须创建不同 Session，且各自保留独立 artifact、timing 和转换。 */
    @Test
    void createsIsolatedSessionForEveryExecution() {
        CompilerPipeline pipeline = new CompilerPipeline(
                PipelineTestSupport.successfulPasses(new ArrayList<String>()));

        PipelineExecutionResult first = execute(pipeline);
        PipelineExecutionResult second = execute(pipeline);

        assertEquals(CompilationSessionState.PUBLISHED, first.state(),
                first.diagnostics().toString());
        assertEquals(CompilationSessionState.PUBLISHED, second.state(),
                second.diagnostics().toString());
        assertNotSame(first.session(), second.session());
        assertNotSame(first.artifacts(), second.artifacts());
        assertEquals(first.executedPasses(), second.executedPasses());
        assertEquals(first.transitions(), second.transitions());
        assertEquals(first.timings().size(), second.timings().size());
    }

    /** 一个失败 Session 的 Diagnostic 不得污染下一次成功 Session。 */
    @Test
    void failureDiagnosticsDoNotLeakIntoNextSession() {
        CompilerPipeline failing = new CompilerPipeline(
                PipelineTestSupport.failingPasses(
                        new ArrayList<String>(), 2));
        PipelineExecutionResult failed = execute(failing);
        PipelineExecutionResult passed = execute(new CompilerPipeline(
                PipelineTestSupport.successfulPasses(
                        new ArrayList<String>())));

        assertEquals(CompilationSessionState.FAILED, failed.state());
        assertFalse(failed.diagnostics().isEmpty());
        assertEquals(CompilationSessionState.PUBLISHED, passed.state(),
                passed.diagnostics().toString());
        assertTrue(passed.diagnostics().isEmpty());
    }

    /** 对外暴露的 Pass、Diagnostic、状态、Timing 和 artifact 集合必须不可修改。 */
    @Test
    void exposesOnlyImmutableCollectionSnapshots() {
        CompilerPipeline pipeline = new CompilerPipeline(
                PipelineTestSupport.successfulPasses(new ArrayList<String>()));
        PipelineExecutionResult result = execute(pipeline);

        assertThrows(UnsupportedOperationException.class,
                () -> pipeline.passes().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> result.executedPasses().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> result.transitions().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> result.timings().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> result.diagnostics().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> result.artifacts().put("external", "mutation"));
    }

    /** T12 新增生产类型不得含 static/thread-local 可变状态。 */
    @Test
    void hasNoStaticMutableOrThreadLocalState() {
        List<Class<?>> types = Arrays.<Class<?>>asList(
                CompilerPipeline.class,
                CompilationSession.class,
                PassContext.class,
                PassResult.class,
                PipelineExecutionResult.class,
                PipelineDiagnostics.class);

        for (Class<?> type : types) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isSynthetic()) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers())) {
                    assertTrue(Modifier.isFinal(field.getModifiers()),
                            type.getName() + "#" + field.getName());
                }
                assertFalse(ThreadLocal.class.isAssignableFrom(field.getType()),
                        type.getName() + "#" + field.getName());
            }
        }
    }

    /** 执行一个无取消、无 Deadline 的完整 Pipeline。 */
    private static PipelineExecutionResult execute(CompilerPipeline pipeline) {
        return pipeline.execute(
                PipelineTestSupport.request(
                        new PipelineTestSupport.MutableClock(),
                        new PipelineTestSupport.MutableCancellation(),
                        Optional.<Deadline>empty(),
                        new PipelineTestSupport.RecordingObserver()),
                PipelineTestSupport.publicationRequest());
    }
}
