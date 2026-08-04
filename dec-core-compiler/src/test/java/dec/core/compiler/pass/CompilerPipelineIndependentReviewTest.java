package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.Deadline;
import dec.core.compiler.api.PublicationRequest;
import dec.core.context.model.DiagnosticCode;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 独立 Review：补强精确名称、运行中停止和结果封装边界。
 */
class CompilerPipelineIndependentReviewTest {

    /** Pass 名称必须逐字符精确匹配，不能通过 trim 接受 padded 名称。 */
    @Test
    void rejectsPaddedPassName() {
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(0, new CompilerPass() {
            @Override
            public String name() {
                return " SourceGraphValidationPass";
            }

            @Override
            public PassResult execute(PassContext context) {
                return PassResult.passed();
            }
        });

        assertThrows(IllegalArgumentException.class,
                () -> new CompilerPipeline(passes));
    }

    /** Pass 执行期间请求取消时，当前 Pass 有 timing，但后续 Pass 不执行。 */
    @Test
    void cancellationDuringPassStopsBeforeStateAdvance() {
        List<String> executions = new ArrayList<String>();
        PipelineTestSupport.MutableCancellation cancellation =
                new PipelineTestSupport.MutableCancellation();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(executions);
        passes.set(2, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.SYMBOL_REGISTRATION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                executions.add(name());
                cancellation.cancel();
                return PassResult.passed();
            }
        });

        PipelineExecutionResult result = execute(
                new CompilerPipeline(passes),
                new PipelineTestSupport.MutableClock(),
                cancellation,
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(3, executions.size());
        assertEquals(3, result.timings().size());
        assertFalse(executions.contains(CompilerPipeline.PUBLICATION_PASS));
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_COMPILATION_CANCELLED));
    }

    /** Pass 执行期间达到 Deadline 时，后续状态和发布必须停止。 */
    @Test
    void timeoutDuringPassStopsBeforeStateAdvance() {
        List<String> executions = new ArrayList<String>();
        PipelineTestSupport.MutableClock clock =
                new PipelineTestSupport.MutableClock();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(executions);
        passes.set(0, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                executions.add(name());
                clock.setNow(100L);
                return PassResult.passed();
            }
        });

        PipelineExecutionResult result = execute(
                new CompilerPipeline(passes),
                clock,
                new PipelineTestSupport.MutableCancellation(),
                Optional.of(new Deadline(50L)));

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(Collections.singletonList(
                CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS), executions);
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_COMPILATION_TIMED_OUT));
    }

    /** PassContext 直接登记 ERROR 时，即使 PassResult passed 也必须阻断。 */
    @Test
    void contextDiagnosticErrorBlocksPipeline() {
        List<String> executions = new ArrayList<String>();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(executions);
        passes.set(1, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.STRUCTURAL_VALIDATION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                executions.add(name());
                context.addDiagnostic(PipelineTestSupport.error(
                        "test.context.error", 1));
                return PassResult.passed();
            }
        });

        PipelineExecutionResult result = execute(
                new CompilerPipeline(passes),
                new PipelineTestSupport.MutableClock(),
                new PipelineTestSupport.MutableCancellation(),
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(2, executions.size());
        assertFalse(executions.contains(CompilerPipeline.PUBLICATION_PASS));
    }

    /** null PassResult 必须转换为稳定失败，不允许空指针越过结果边界。 */
    @Test
    void nullPassResultFailsClosed() {
        List<String> executions = new ArrayList<String>();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(executions);
        passes.set(3, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.REFERENCE_RESOLUTION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                executions.add(name());
                return null;
            }
        });

        PipelineExecutionResult result = execute(
                new CompilerPipeline(passes),
                new PipelineTestSupport.MutableClock(),
                new PipelineTestSupport.MutableCancellation(),
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "pipeline.pass.failure".equals(diagnostic.messageKey())));
    }

    /** PublicationPass 异常必须使用独立发布失败 Diagnostic。 */
    @Test
    void publicationExceptionUsesPublicationFailureCode() {
        PipelineExecutionResult result = execute(
                new CompilerPipeline(PipelineTestSupport.throwingPasses(
                        new ArrayList<String>(), 9)),
                new PipelineTestSupport.MutableClock(),
                new PipelineTestSupport.MutableCancellation(),
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_PUBLICATION_FAILURE));
        assertTrue(result.artifacts().isEmpty());
    }

    /** compile-only execute 和内部 Session 均不得成为公共 API。 */
    @Test
    void keepsCompileOnlyAndMutableSessionOutOfPublicApi() throws Exception {
        Method execute = CompilerPipeline.class.getDeclaredMethod(
                "execute",
                CompilationRequest.class,
                PublicationRequest.class);
        assertFalse(Modifier.isPublic(execute.getModifiers()));
        assertThrows(NoSuchMethodException.class,
                () -> PipelineExecutionResult.class.getMethod("session"));
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
