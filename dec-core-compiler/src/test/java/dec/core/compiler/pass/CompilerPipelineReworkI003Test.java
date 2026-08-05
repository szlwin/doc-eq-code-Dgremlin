package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import dec.core.compiler.api.SessionStateTransition;
import dec.core.context.EngineContext;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 / I003：最终 Diagnostic、Clock、Deadline 与 artifact 保真阻断测试。
 */
class CompilerPipelineReworkI003Test {

    /** final Pass 已准备 candidate 但返回 ERROR 时，外部 publisher 必须保持 0 次调用。 */
    @Test
    void finalPassErrorIsVisibleBeforeCommit() {
        CountingPublisher publisher = new CountingPublisher();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> {
            stageCandidate(context);
            return PassResult.of(Collections.singletonList(
                    PipelineTestSupport.error("i003.final.error", 9)));
        }));

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                new SequenceClock(0L),
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "i003.final.error".equals(diagnostic.messageKey())));
    }

    /** final Pass 返回 WARNING 时应成功发布，且 WARNING 不得静默丢失。 */
    @Test
    void finalPassWarningIsPreservedAfterCommit() {
        CountingPublisher publisher = new CountingPublisher();
        Diagnostic warning = warning("i003.final.warning");
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> {
            stageCandidate(context);
            return PassResult.of(Collections.singletonList(warning));
        }));

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                new SequenceClock(0L),
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        assertEquals(1, publisher.calls());
        assertTrue(result.diagnostics().contains(warning));
    }

    /** long 极值差值溢出必须收敛为 FAILED，不能让异常越过 Pipeline。 */
    @Test
    void timingOverflowFailsClosed() {
        CountingPublisher publisher = new CountingPublisher();
        SequenceClock clock = new SequenceClock(Long.MIN_VALUE, Long.MAX_VALUE);

        PipelineExecutionResult result = assertDoesNotThrow(() -> execute(
                PipelineTestSupport.successfulPasses(new ArrayList<String>()),
                publisher,
                clock,
                Optional.<Deadline>empty()));

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_OBSERVER_FAILURE
                        && "pipeline.clock.failure".equals(
                                diagnostic.messageKey())));
    }

    /** start timestamp 已到 Deadline 时不得记录或调用普通 Pass。 */
    @Test
    void startTimestampDeadlineBlocksPassExecution() {
        CountingPublisher publisher = new CountingPublisher();
        List<String> executions = new ArrayList<String>();
        SequenceClock clock = new SequenceClock(9L, 10L, 11L);

        PipelineExecutionResult result = execute(
                PipelineTestSupport.successfulPasses(executions),
                publisher,
                clock,
                Optional.of(new Deadline(10L)));

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertTrue(executions.isEmpty());
        assertTrue(result.executedPasses().isEmpty());
        assertTrue(result.timings().isEmpty());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_COMPILATION_TIMED_OUT));
    }

    /** IdentityHashMap 的两个冻结后相等 key 必须 fail-closed，不能覆盖事实。 */
    @Test
    void identityMapCollisionFailsClosed() {
        CountingPublisher publisher = new CountingPublisher();
        List<String> firstKey = new ArrayList<String>();
        firstKey.add("same");
        List<String> secondKey = new ArrayList<String>();
        secondKey.add("same");
        Map<List<String>, String> identityMap =
                new IdentityHashMap<List<String>, String>();
        identityMap.put(firstKey, "first");
        identityMap.put(secondKey, "second");

        PipelineExecutionResult result = execute(
                passesWithArtifact("identity-map", identityMap),
                publisher,
                new SequenceClock(0L),
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "pipeline.pass.failure".equals(diagnostic.messageKey())));
    }

    /** identity-backed Set 的冻结后相等元素必须 fail-closed，不能静默去重。 */
    @Test
    void identitySetCollisionFailsClosed() {
        CountingPublisher publisher = new CountingPublisher();
        Set<List<String>> identitySet = Collections.newSetFromMap(
                new IdentityHashMap<List<String>, Boolean>());
        identitySet.add(new ArrayList<String>(Collections.singletonList("same")));
        identitySet.add(new ArrayList<String>(Collections.singletonList("same")));

        PipelineExecutionResult result = execute(
                passesWithArtifact("identity-set", identitySet),
                publisher,
                new SequenceClock(0L),
                Optional.<Deadline>empty());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "pipeline.pass.failure".equals(diagnostic.messageKey())));
    }

    /** 创建写入指定 artifact 的十阶段 Pipeline。 */
    private static List<CompilerPass> passesWithArtifact(
            String key,
            Object artifact) {
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(0, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                context.putArtifact(key, artifact);
                return PassResult.passed();
            }
        });
        return passes;
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

    /**
     * 兼容 I002 的 publish API 与 I003 的 prepare API，以保持同一个 RED Oracle。
     */
    private static void stageCandidate(PublicationPassContext context) {
        Method method;
        try {
            try {
                method = PublicationPassContext.class.getMethod(
                        "prepare",
                        EngineContext.class);
            } catch (NoSuchMethodException beforeI003) {
                method = PublicationPassContext.class.getMethod(
                        "publish",
                        EngineContext.class);
            }
            method.invoke(context, PipelineTestSupport.candidate());
        } catch (InvocationTargetException failure) {
            Throwable cause = failure.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException("candidate preparation failed", cause);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("candidate preparation API missing", failure);
        }
    }

    /** 执行指定 Pipeline。 */
    private static PipelineExecutionResult execute(
            List<CompilerPass> passes,
            CountingPublisher publisher,
            MonotonicClock clock,
            Optional<Deadline> deadline) {
        return new CompilerPipeline(passes).execute(
                PipelineTestSupport.request(
                        clock,
                        new NeverCancelled(),
                        deadline,
                        new StableObserver()),
                PipelineTestSupport.publicationRequest(publisher));
    }

    /** 创建稳定 WARNING Diagnostic。 */
    private static Diagnostic warning(String messageKey) {
        return new Diagnostic(
                DiagnosticCode.MIX_PUBLICATION_BLOCKED,
                DiagnosticSeverity.WARNING,
                messageKey,
                null,
                new SourceRef("pipeline-i003", 1, 1, "/publication"),
                Collections.<SourceRef>emptyList(),
                null,
                CompilerPipeline.PUBLICATION_PASS);
    }

    /** Publication Pass 测试行为。 */
    private interface PublicationBehavior {
        PassResult execute(PublicationPassContext context);
    }

    /** 记录真实外部发布调用次数。 */
    private static final class CountingPublisher implements ContextPublisher {
        private int calls;

        @Override
        public PublicationResult publish(
                Optional<EngineContext> expectedCurrent,
                EngineContext candidate) {
            calls++;
            return PipelineTestSupport.publishedResult();
        }

        /** 返回真实调用次数。 */
        private int calls() {
            return calls;
        }
    }

    /** 按给定序列返回 Clock 值，序列耗尽后重复最后一个值。 */
    private static final class SequenceClock implements MonotonicClock {
        private final long[] values;
        private int index;

        private SequenceClock(long... values) {
            this.values = values.length == 0 ? new long[]{0L} : values.clone();
        }

        @Override
        public long nanoTime() {
            int selected = Math.min(index, values.length - 1);
            index++;
            return values[selected];
        }
    }

    /** 永不取消的稳定 Token。 */
    private static final class NeverCancelled implements CancellationToken {
        @Override
        public boolean isCancellationRequested() {
            return false;
        }
    }

    /** 不改变语义事实的 Observer。 */
    private static final class StableObserver implements CompilationObserver {
        @Override
        public void onTiming(CompilationTiming timing) {
        }

        @Override
        public void onStateTransition(SessionStateTransition transition) {
        }
    }
}
