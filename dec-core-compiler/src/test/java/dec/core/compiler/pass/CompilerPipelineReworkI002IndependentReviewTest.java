package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CancellationToken;
import dec.core.compiler.api.CompilationObserver;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.Deadline;
import dec.core.compiler.api.MonotonicClock;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.PublicationStatus;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.context.EngineContext;
import dec.core.context.model.DiagnosticCode;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 / I002 独立 Review：补强 Publication 原子提交和事实冻结边界。
 */
class CompilerPipelineReworkI002IndependentReviewTest {

    /** 只有第十个位置可以持有 PublicationCompilerPass capability。 */
    @Test
    void enforcesPublicationCapabilitySlot() {
        List<CompilerPass> earlyPublication = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        earlyPublication.set(0, publicationPass(context -> PassResult.passed(),
                CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS));
        assertThrows(IllegalArgumentException.class,
                () -> new CompilerPipeline(earlyPublication));

        List<CompilerPass> ordinaryFinal = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        ordinaryFinal.set(9, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.PUBLICATION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                return PassResult.passed();
            }
        });
        assertThrows(IllegalArgumentException.class,
                () -> new CompilerPipeline(ordinaryFinal));
    }

    /** 普通 Context 和 Session 都不得保存 PublicationRequest 或 Publisher。 */
    @Test
    void ordinaryObjectsDoNotRetainPublicationCapability() {
        for (Class<?> type : Arrays.<Class<?>>asList(
                PassContext.class,
                CompilationSession.class)) {
            for (Field field : type.getDeclaredFields()) {
                assertFalse(PublicationRequest.class.isAssignableFrom(field.getType()),
                        type.getName() + "#" + field.getName());
                assertFalse(ContextPublisher.class.isAssignableFrom(field.getType()),
                        type.getName() + "#" + field.getName());
            }
        }
    }

    /** compare-and-set 冲突必须失败且 publisher 只调用一次。 */
    @Test
    void publicationConflictFailsWithOnePublisherCall() {
        CountingPublisher publisher = new CountingPublisher(Mode.CONFLICT);

        PipelineExecutionResult result = execute(
                PipelineTestSupport.successfulPasses(new ArrayList<String>()),
                publisher,
                new StableClock(),
                new StableCancellation(),
                Optional.<Deadline>empty(),
                new StableObserver());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(1, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_PUBLICATION_CONFLICT));
    }

    /** null PublicationResult 必须收敛为发布失败。 */
    @Test
    void nullPublicationResultFailsClosed() {
        assertInvalidPublicationResult(Mode.NULL_RESULT);
    }

    /** null PublicationStatus 必须收敛为发布失败。 */
    @Test
    void nullPublicationStatusFailsClosed() {
        assertInvalidPublicationResult(Mode.NULL_STATUS);
    }

    /** Publisher 抛异常必须使用发布失败 Diagnostic。 */
    @Test
    void publisherExceptionFailsClosed() {
        assertInvalidPublicationResult(Mode.THROW);
    }

    /** PublicationPass 未调用 publish 时不得进入 PUBLISHED。 */
    @Test
    void publicationPassMustCommitExactlyOnce() {
        CountingPublisher publisher = new CountingPublisher(Mode.PUBLISHED);
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> PassResult.passed(),
                CompilerPipeline.PUBLICATION_PASS));

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                new StableClock(),
                new StableCancellation(),
                Optional.<Deadline>empty(),
                new StableObserver());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "pipeline.publication.blocked".equals(diagnostic.messageKey())));
    }

    /** 第二次 publish 必须在本地拒绝，且不能重复外部提交或否定第一次成功。 */
    @Test
    void secondPublishIsRejectedWithoutDowngradingCommit() {
        CountingPublisher publisher = new CountingPublisher(Mode.PUBLISHED);
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> {
            context.publish(PipelineTestSupport.candidate());
            assertThrows(IllegalStateException.class,
                    () -> context.publish(PipelineTestSupport.candidate()));
            return PassResult.passed();
        }, CompilerPipeline.PUBLICATION_PASS));

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                new StableClock(),
                new StableCancellation(),
                Optional.<Deadline>empty(),
                new StableObserver());

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        assertEquals(1, publisher.calls());
    }

    /** 首 Pass 前取消必须保持 publisher 调用数为 0。 */
    @Test
    void cancellationBeforePipelineBlocksPublisher() {
        CountingPublisher publisher = new CountingPublisher(Mode.PUBLISHED);
        StableCancellation cancellation = new StableCancellation();
        cancellation.cancel();

        PipelineExecutionResult result = execute(
                PipelineTestSupport.successfulPasses(new ArrayList<String>()),
                publisher,
                new StableClock(),
                cancellation,
                Optional.<Deadline>empty(),
                new StableObserver());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
    }

    /** 首 Pass 前超时必须保持 publisher 调用数为 0。 */
    @Test
    void timeoutBeforePipelineBlocksPublisher() {
        CountingPublisher publisher = new CountingPublisher(Mode.PUBLISHED);

        PipelineExecutionResult result = execute(
                PipelineTestSupport.successfulPasses(new ArrayList<String>()),
                publisher,
                new StableClock(),
                new StableCancellation(),
                Optional.of(new Deadline(0L)),
                new StableObserver());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
    }

    /** CancellationToken 基础设施异常必须准确诊断且不触达 publisher。 */
    @Test
    void cancellationTokenFailureBlocksPublisher() {
        CountingPublisher publisher = new CountingPublisher(Mode.PUBLISHED);
        CancellationToken throwingToken = new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                throw new IllegalStateException("controlled-token-failure");
            }
        };

        PipelineExecutionResult result = execute(
                PipelineTestSupport.successfulPasses(new ArrayList<String>()),
                publisher,
                new StableClock(),
                throwingToken,
                Optional.<Deadline>empty(),
                new StableObserver());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_OBSERVER_FAILURE
                        && "pipeline.cancellation-token.failure".equals(
                                diagnostic.messageKey())));
    }

    /** Deadline Clock 基础设施异常必须准确诊断且不触达 publisher。 */
    @Test
    void deadlineClockFailureBlocksPublisher() {
        CountingPublisher publisher = new CountingPublisher(Mode.PUBLISHED);
        MonotonicClock throwingClock = new MonotonicClock() {
            @Override
            public long nanoTime() {
                throw new IllegalStateException("controlled-clock-failure");
            }
        };

        PipelineExecutionResult result = execute(
                PipelineTestSupport.successfulPasses(new ArrayList<String>()),
                publisher,
                throwingClock,
                new StableCancellation(),
                Optional.of(new Deadline(100L)),
                new StableObserver());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_OBSERVER_FAILURE
                        && "pipeline.clock.failure".equals(diagnostic.messageKey())));
    }

    /** 未知可变 artifact 必须在当前 Pass 内 fail-closed。 */
    @Test
    void rejectsUnknownMutableArtifact() {
        CountingPublisher publisher = new CountingPublisher(Mode.PUBLISHED);
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(0, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                context.putArtifact("mutable-builder", new StringBuilder("unsafe"));
                return PassResult.passed();
            }
        });

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                new StableClock(),
                new StableCancellation(),
                Optional.<Deadline>empty(),
                new StableObserver());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "pipeline.pass.failure".equals(diagnostic.messageKey())));
    }

    /** 嵌套 List/Map artifact 必须递归复制并冻结。 */
    @Test
    void recursivelySnapshotsNestedArtifactContainers() {
        CountingPublisher publisher = new CountingPublisher(Mode.PUBLISHED);
        List<String> nestedList = new ArrayList<String>();
        nestedList.add("before");
        Map<String, Object> mutable = new LinkedHashMap<String, Object>();
        mutable.put("items", nestedList);
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(0, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                context.putArtifact("nested", mutable);
                return PassResult.passed();
            }
        });

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                new StableClock(),
                new StableCancellation(),
                Optional.<Deadline>empty(),
                new StableObserver());
        nestedList.add("after");
        mutable.put("late", "mutation");

        Map<?, ?> frozen = (Map<?, ?>) result.artifacts().get("nested");
        assertEquals(Collections.singletonList("before"), frozen.get("items"));
        assertFalse(frozen.containsKey("late"));
        assertThrows(UnsupportedOperationException.class,
                () -> ((Map<Object, Object>) frozen).put("external", "mutation"));
    }

    /** retained 普通 Context 的读写方法在 Pass 返回后全部拒绝。 */
    @Test
    void retainedOrdinaryContextRejectsReadsAndWrites() {
        CountingPublisher publisher = new CountingPublisher(Mode.PUBLISHED);
        List<PassContext> retained = new ArrayList<PassContext>();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(0, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                retained.add(context);
                context.putArtifact("current", "value");
                return PassResult.passed();
            }
        });

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                new StableClock(),
                new StableCancellation(),
                Optional.<Deadline>empty(),
                new StableObserver());

        assertEquals(CompilationSessionState.PUBLISHED, result.state());
        PassContext old = retained.get(0);
        assertThrows(IllegalStateException.class, old::request);
        assertThrows(IllegalStateException.class, old::state);
        assertThrows(IllegalStateException.class,
                () -> old.artifact("current", String.class));
        assertThrows(IllegalStateException.class,
                () -> old.putArtifact("late", "mutation"));
        assertThrows(IllegalStateException.class,
                () -> old.addDiagnostic(PipelineTestSupport.error("late", 1)));
    }

    /** retained Publication Context 在成功提交后全部访问均拒绝。 */
    @Test
    void retainedPublicationContextClosesAfterCommit() {
        CountingPublisher publisher = new CountingPublisher(Mode.PUBLISHED);
        List<PublicationPassContext> retained = new ArrayList<PublicationPassContext>();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> {
            retained.add(context);
            context.publish(PipelineTestSupport.candidate());
            return PassResult.passed();
        }, CompilerPipeline.PUBLICATION_PASS));

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                new StableClock(),
                new StableCancellation(),
                Optional.<Deadline>empty(),
                new StableObserver());

        assertEquals(CompilationSessionState.PUBLISHED, result.state());
        assertPublicationContextClosed(retained.get(0));
    }

    /** retained Publication Context 在发布前失败后也必须关闭。 */
    @Test
    void retainedPublicationContextClosesAfterFailure() {
        CountingPublisher publisher = new CountingPublisher(Mode.PUBLISHED);
        List<PublicationPassContext> retained = new ArrayList<PublicationPassContext>();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, publicationPass(context -> {
            retained.add(context);
            return PassResult.of(Collections.singletonList(
                    PipelineTestSupport.error("publication.precommit", 9)));
        }, CompilerPipeline.PUBLICATION_PASS));

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                new StableClock(),
                new StableCancellation(),
                Optional.<Deadline>empty(),
                new StableObserver());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertPublicationContextClosed(retained.get(0));
    }

    /** Observer timing/state 回调失败不得改变已验证或已提交结果。 */
    @Test
    void observerFailuresDoNotChangePublishedResult() {
        CountingPublisher publisher = new CountingPublisher(Mode.PUBLISHED);
        CompilationObserver throwingObserver = new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                throw new IllegalStateException("controlled-timing-observer-failure");
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                throw new IllegalStateException("controlled-state-observer-failure");
            }
        };

        PipelineExecutionResult result = execute(
                PipelineTestSupport.successfulPasses(new ArrayList<String>()),
                publisher,
                new StableClock(),
                new StableCancellation(),
                Optional.<Deadline>empty(),
                throwingObserver);

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        assertEquals(1, publisher.calls());
    }

    /** 验证非法 PublicationResult 的统一失败后置条件。 */
    private static void assertInvalidPublicationResult(Mode mode) {
        CountingPublisher publisher = new CountingPublisher(mode);

        PipelineExecutionResult result = execute(
                PipelineTestSupport.successfulPasses(new ArrayList<String>()),
                publisher,
                new StableClock(),
                new StableCancellation(),
                Optional.<Deadline>empty(),
                new StableObserver());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(1, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_PUBLICATION_FAILURE));
    }

    /** 验证 Publication Context 已关闭。 */
    private static void assertPublicationContextClosed(PublicationPassContext context) {
        assertThrows(IllegalStateException.class, context::request);
        assertThrows(IllegalStateException.class, context::state);
        assertThrows(IllegalStateException.class,
                () -> context.artifact("last-pass", String.class));
        assertThrows(IllegalStateException.class,
                () -> context.addDiagnostic(PipelineTestSupport.error("late", 1)));
        assertThrows(IllegalStateException.class,
                () -> context.publish(PipelineTestSupport.candidate()));
    }

    /** 创建指定名称和行为的 PublicationCompilerPass。 */
    private static CompilerPass publicationPass(
            PublicationBehavior behavior,
            String name) {
        return new PublicationCompilerPass() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public PassResult execute(PublicationPassContext context) {
                return behavior.execute(context);
            }
        };
    }

    /** 执行指定 Pipeline 和基础设施组合。 */
    private static PipelineExecutionResult execute(
            List<CompilerPass> passes,
            ContextPublisher publisher,
            MonotonicClock clock,
            CancellationToken cancellation,
            Optional<Deadline> deadline,
            CompilationObserver observer) {
        return new CompilerPipeline(passes).execute(
                PipelineTestSupport.request(
                        clock,
                        cancellation,
                        deadline,
                        observer),
                PipelineTestSupport.publicationRequest(publisher));
    }

    /** Publication Pass 测试行为。 */
    private interface PublicationBehavior {
        PassResult execute(PublicationPassContext context);
    }

    /** Publisher 返回模式。 */
    private enum Mode {
        PUBLISHED,
        CONFLICT,
        NULL_RESULT,
        NULL_STATUS,
        THROW
    }

    /** 可配置结果并记录外部调用次数的 Publisher。 */
    private static final class CountingPublisher implements ContextPublisher {
        private final Mode mode;
        private int calls;

        private CountingPublisher(Mode mode) {
            this.mode = mode;
        }

        @Override
        public PublicationResult publish(
                Optional<EngineContext> expectedCurrent,
                EngineContext candidate) {
            calls++;
            if (mode == Mode.THROW) {
                throw new IllegalStateException("controlled-publisher-failure");
            }
            if (mode == Mode.NULL_RESULT) {
                return null;
            }
            return new PublicationResult() {
                @Override
                public PublicationStatus status() {
                    if (mode == Mode.NULL_STATUS) {
                        return null;
                    }
                    return mode == Mode.CONFLICT
                            ? PublicationStatus.CONFLICT
                            : PublicationStatus.PUBLISHED;
                }
            };
        }

        /** 返回真实外部调用次数。 */
        private int calls() {
            return calls;
        }
    }

    /** 稳定递增的单调时钟。 */
    private static final class StableClock implements MonotonicClock {
        private long value;

        @Override
        public long nanoTime() {
            return value++;
        }
    }

    /** 可变但不抛异常的取消令牌。 */
    private static final class StableCancellation implements CancellationToken {
        private boolean cancelled;

        /** 请求取消。 */
        private void cancel() {
            cancelled = true;
        }

        @Override
        public boolean isCancellationRequested() {
            return cancelled;
        }
    }

    /** 不改变任何事实的 Observer。 */
    private static final class StableObserver implements CompilationObserver {
        @Override
        public void onTiming(CompilationTiming timing) {
        }

        @Override
        public void onStateTransition(SessionStateTransition transition) {
        }
    }
}
