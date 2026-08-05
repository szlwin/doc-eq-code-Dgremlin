package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.Deadline;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.api.PublicationResult;
import dec.core.context.EngineContext;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 / I003 独立 Review：验证 prepare/commit 顺序和 Context 封闭边界。
 */
class CompilerPipelineReworkI003IndependentReviewTest {

    /** Publication Context 不得保存或公开外部 publisher capability。 */
    @Test
    void publicationContextHasNoExternalPublicationCapability() {
        for (Field field : PublicationPassContext.class.getDeclaredFields()) {
            assertFalse(PublicationRequest.class.isAssignableFrom(field.getType()),
                    field.toString());
            assertFalse(ContextPublisher.class.isAssignableFrom(field.getType()),
                    field.toString());
        }
        for (Method method : PublicationPassContext.class.getMethods()) {
            assertFalse(PublicationRequest.class.isAssignableFrom(
                    method.getReturnType()), method.toString());
            assertFalse(ContextPublisher.class.isAssignableFrom(
                    method.getReturnType()), method.toString());
            assertFalse("publish".equals(method.getName()), method.toString());
        }
    }

    /** retained Publication Context 关闭后，公开和包内读取均必须拒绝。 */
    @Test
    void retainedPublicationContextRejectsAllReadsAfterClose() {
        CountingPublisher publisher = new CountingPublisher();
        List<PublicationPassContext> retained =
                new ArrayList<PublicationPassContext>();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> {
            retained.add(context);
            context.prepare(PipelineTestSupport.candidate());
            return PassResult.passed();
        }));

        PipelineExecutionResult result = execute(passes, publisher);

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        PublicationPassContext old = retained.get(0);
        assertThrows(IllegalStateException.class, old::request);
        assertThrows(IllegalStateException.class, old::state);
        assertThrows(IllegalStateException.class,
                () -> old.artifact("last-pass", String.class));
        assertThrows(IllegalStateException.class,
                () -> old.addDiagnostic(PipelineTestSupport.error("late", 1)));
        assertThrows(IllegalStateException.class,
                () -> old.prepare(PipelineTestSupport.candidate()));
        assertThrows(IllegalStateException.class, old::candidatePrepared);
        assertThrows(IllegalStateException.class, old::preparedCandidate);
    }

    /** Publisher 只能在 final Pass 已完整返回后被调用。 */
    @Test
    void publisherRunsOnlyAfterFinalPassReturns() {
        AtomicBoolean passActive = new AtomicBoolean(false);
        OrderingPublisher publisher = new OrderingPublisher(passActive);
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> {
            passActive.set(true);
            context.prepare(PipelineTestSupport.candidate());
            passActive.set(false);
            return PassResult.passed();
        }));

        PipelineExecutionResult result = execute(passes, publisher);

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        assertEquals(1, publisher.calls());
        assertTrue(publisher.observedReturnedPass());
    }

    /** Context 中的 ERROR 即使在 candidate 准备后登记，也必须阻断 commit。 */
    @Test
    void contextErrorAfterPreparationBlocksCommit() {
        CountingPublisher publisher = new CountingPublisher();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> {
            context.prepare(PipelineTestSupport.candidate());
            context.addDiagnostic(PipelineTestSupport.error(
                    "i003.context.error", 9));
            return PassResult.passed();
        }));

        PipelineExecutionResult result = execute(passes, publisher);

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "i003.context.error".equals(diagnostic.messageKey())));
    }

    /** 同一 final Pass 重复准备 candidate 必须本地拒绝且不重复提交。 */
    @Test
    void duplicatePreparationIsRejectedLocally() {
        CountingPublisher publisher = new CountingPublisher();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> {
            context.prepare(PipelineTestSupport.candidate());
            assertThrows(IllegalStateException.class,
                    () -> context.prepare(PipelineTestSupport.candidate()));
            return PassResult.passed();
        }));

        PipelineExecutionResult result = execute(passes, publisher);

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        assertEquals(1, publisher.calls());
    }

    /** Context 和 PassResult 返回的非 ERROR Diagnostic 均必须保留。 */
    @Test
    void preservesContextAndResultWarningsAfterCommit() {
        CountingPublisher publisher = new CountingPublisher();
        Diagnostic contextWarning = warning("i003.context.warning", 1);
        Diagnostic resultWarning = warning("i003.result.warning", 2);
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> {
            context.prepare(PipelineTestSupport.candidate());
            context.addDiagnostic(contextWarning);
            return PassResult.of(Collections.singletonList(resultWarning));
        }));

        PipelineExecutionResult result = execute(passes, publisher);

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        assertEquals(1, publisher.calls());
        assertTrue(result.diagnostics().contains(contextWarning));
        assertTrue(result.diagnostics().contains(resultWarning));
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

    /** 执行指定 Pipeline。 */
    private static PipelineExecutionResult execute(
            List<CompilerPass> passes,
            ContextPublisher publisher) {
        return new CompilerPipeline(passes).execute(
                PipelineTestSupport.request(
                        new PipelineTestSupport.MutableClock(),
                        new PipelineTestSupport.MutableCancellation(),
                        Optional.<Deadline>empty(),
                        new PipelineTestSupport.RecordingObserver()),
                PipelineTestSupport.publicationRequest(publisher));
    }

    /** 创建稳定 WARNING Diagnostic。 */
    private static Diagnostic warning(String messageKey, int line) {
        return new Diagnostic(
                DiagnosticCode.MIX_PUBLICATION_BLOCKED,
                DiagnosticSeverity.WARNING,
                messageKey,
                null,
                new SourceRef("pipeline-i003-review", line, 1, "/publication"),
                Collections.<SourceRef>emptyList(),
                null,
                CompilerPipeline.PUBLICATION_PASS);
    }

    /** Publication Pass 测试行为。 */
    private interface PublicationBehavior {
        PassResult execute(PublicationPassContext context);
    }

    /** 返回 PUBLISHED 并记录外部调用次数。 */
    private static class CountingPublisher implements ContextPublisher {
        private int calls;

        @Override
        public PublicationResult publish(
                Optional<EngineContext> expectedCurrent,
                EngineContext candidate) {
            calls++;
            return PipelineTestSupport.publishedResult();
        }

        /** 返回真实调用次数。 */
        int calls() {
            return calls;
        }
    }

    /** 验证 publisher 调用时 final Pass 已经退出。 */
    private static final class OrderingPublisher extends CountingPublisher {
        private final AtomicBoolean passActive;
        private boolean observedReturnedPass;

        private OrderingPublisher(AtomicBoolean passActive) {
            this.passActive = passActive;
        }

        @Override
        public PublicationResult publish(
                Optional<EngineContext> expectedCurrent,
                EngineContext candidate) {
            observedReturnedPass = !passActive.get();
            return super.publish(expectedCurrent, candidate);
        }

        /** 返回是否观察到 final Pass 已退出。 */
        private boolean observedReturnedPass() {
            return observedReturnedPass;
        }
    }
}
