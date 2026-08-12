package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CancellationToken;
import dec.core.compiler.api.CompilationObserver;
import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.Deadline;
import dec.core.compiler.api.MonotonicClock;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.PublicationStatus;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendRegistry;
import dec.core.compiler.source.DocumentSourceProvider;
import dec.core.compiler.source.SourceReference;
import dec.core.compiler.source.SourceResolutionContext;
import dec.core.compiler.source.SourceResolutionResult;
import dec.core.context.EngineContext;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.PublishedSourceManifest;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 / I002：Publication capability、Context 生命周期和原子终态阻断测试。
 */
class CompilerPipelineReworkI002Test {

    /** 普通 PassContext 不得公开 PublicationRequest 或 ContextPublisher。 */
    @Test
    void ordinaryPassContextHasNoPublicationCapability() {
        for (Method method : PassContext.class.getMethods()) {
            assertFalse(method.getName().equals("publicationRequest"), method.toString());
            assertFalse(PublicationRequest.class.isAssignableFrom(method.getReturnType()),
                    method.toString());
            assertFalse(ContextPublisher.class.isAssignableFrom(method.getReturnType()),
                    method.toString());
        }
        for (Field field : PassContext.class.getDeclaredFields()) {
            assertFalse(PublicationRequest.class.isAssignableFrom(field.getType()),
                    field.toString());
            assertFalse(ContextPublisher.class.isAssignableFrom(field.getType()),
                    field.toString());
        }
    }

    /** 早期 Pass 即使尝试发布并随后返回 ERROR，publisher 调用数也必须为 0。 */
    @Test
    void earlyErrorCannotReachPublisher() {
        CountingPublisher publisher = new CountingPublisher(PublicationStatus.PUBLISHED);
        List<String> executions = new ArrayList<String>();
        List<CompilerPass> passes = compatiblePasses(
                executions,
                publisher,
                false,
                null,
                false);
        passes.set(0, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                executions.add(name());
                invokeLegacyPublisherIfPresent(context, publisher);
                return PassResult.of(Collections.singletonList(
                        PipelineTestSupport.error("i002.early.error", 0)));
            }
        });

        PipelineExecutionResult result = execute(
                passes,
                publisher,
                new StableClock(),
                new MutableCancellation());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertEquals(Collections.singletonList(
                CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS), executions);
    }

    /** 完整成功路径必须只通过最终 PublicationPass 发布一次。 */
    @Test
    void successfulPathPublishesExactlyOnce() {
        CountingPublisher publisher = new CountingPublisher(PublicationStatus.PUBLISHED);
        PipelineExecutionResult result = execute(
                compatiblePasses(
                        new ArrayList<String>(),
                        publisher,
                        false,
                        null,
                        false),
                publisher,
                new StableClock(),
                new MutableCancellation());

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        assertEquals(1, publisher.calls());
    }

    /** retained Context 在 PUBLISHED 后必须关闭，且已返回结果保持稳定。 */
    @Test
    void retainedContextCannotMutatePublishedResult() {
        CountingPublisher publisher = new CountingPublisher(PublicationStatus.PUBLISHED);
        List<PassContext> retained = new ArrayList<PassContext>();
        List<CompilerPass> passes = compatiblePasses(
                new ArrayList<String>(), publisher, false, null, false);
        passes.set(0, retainingPass(
                CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS,
                retained,
                null));

        PipelineExecutionResult result = execute(
                passes, publisher, new StableClock(), new MutableCancellation());
        List<Diagnostic> beforeDiagnostics = result.diagnostics();
        Map<String, Object> beforeArtifacts = result.artifacts();

        assertEquals(CompilationSessionState.PUBLISHED, result.state());
        assertThrows(IllegalStateException.class, () -> retained.get(0).addDiagnostic(
                PipelineTestSupport.error("i002.late.error", 1)));
        assertThrows(IllegalStateException.class,
                () -> retained.get(0).putArtifact("late", "mutation"));
        assertEquals(beforeDiagnostics, result.diagnostics());
        assertEquals(beforeArtifacts, result.artifacts());
    }

    /** retained Context 在 FAILED 后同样必须关闭。 */
    @Test
    void retainedContextCannotMutateFailedResult() {
        CountingPublisher publisher = new CountingPublisher(PublicationStatus.PUBLISHED);
        List<PassContext> retained = new ArrayList<PassContext>();
        List<CompilerPass> passes = compatiblePasses(
                new ArrayList<String>(), publisher, false, null, false);
        passes.set(0, retainingPass(
                CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS,
                retained,
                PipelineTestSupport.error("i002.failed", 0)));

        PipelineExecutionResult result = execute(
                passes, publisher, new StableClock(), new MutableCancellation());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertThrows(IllegalStateException.class,
                () -> retained.get(0).putArtifact("late", "mutation"));
        assertThrows(IllegalStateException.class, () -> retained.get(0).addDiagnostic(
                PipelineTestSupport.error("i002.failed.late", 1)));
    }

    /** 第二次 execute 不能通过旧 Context 改变第一次结果。 */
    @Test
    void laterExecutionCannotMutateEarlierResult() {
        CountingPublisher publisher = new CountingPublisher(PublicationStatus.PUBLISHED);
        List<PassContext> retained = new ArrayList<PassContext>();
        List<CompilerPass> passes = compatiblePasses(
                new ArrayList<String>(), publisher, false, null, false);
        passes.set(0, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                for (PassContext previous : retained) {
                    try {
                        previous.addDiagnostic(PipelineTestSupport.error(
                                "i002.cross-session", 2));
                    } catch (IllegalStateException expectedClosedContext) {
                        // I002 正确实现必须拒绝旧 Context；当前 Context 继续执行。
                    }
                }
                retained.add(context);
                context.putArtifact("current", "session-" + retained.size());
                return PassResult.passed();
            }
        });

        CompilerPipeline pipeline = new CompilerPipeline(passes);
        PipelineExecutionResult first = execute(
                pipeline, publisher, new StableClock(), new MutableCancellation());
        List<Diagnostic> firstDiagnostics = first.diagnostics();
        Map<String, Object> firstArtifacts = first.artifacts();

        PipelineExecutionResult second = execute(
                pipeline, publisher, new StableClock(), new MutableCancellation());

        assertEquals(CompilationSessionState.PUBLISHED, second.state());
        assertEquals(firstDiagnostics, first.diagnostics());
        assertEquals(firstArtifacts, first.artifacts());
    }

    /** Result 必须保存值快照，不能持有 CompilationSession 事实源。 */
    @Test
    void resultDoesNotRetainCompilationSession() {
        for (Field field : PipelineExecutionResult.class.getDeclaredFields()) {
            assertFalse(CompilationSession.class.isAssignableFrom(field.getType()),
                    field.toString());
        }
        assertThrows(NoSuchMethodException.class,
                () -> PipelineExecutionResult.class.getDeclaredMethod("session"));
    }

    /** mutable artifact 容器必须在写入时递归快照。 */
    @Test
    void mutableArtifactContainerIsSnapshotted() {
        CountingPublisher publisher = new CountingPublisher(PublicationStatus.PUBLISHED);
        List<String> mutable = new ArrayList<String>();
        mutable.add("before");
        List<CompilerPass> passes = compatiblePasses(
                new ArrayList<String>(), publisher, false, null, false);
        passes.set(0, new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.SOURCE_GRAPH_VALIDATION_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                context.putArtifact("mutable", mutable);
                return PassResult.passed();
            }
        });

        PipelineExecutionResult result = execute(
                passes, publisher, new StableClock(), new MutableCancellation());
        mutable.add("after");

        assertEquals(Collections.singletonList("before"),
                result.artifacts().get("mutable"));
    }

    /** candidate 准备后的 end-clock 失败必须阻断外部提交。 */
    @Test
    void publicationPreparationClockFailureBlocksCommit() {
        CountingPublisher publisher = new CountingPublisher(PublicationStatus.PUBLISHED);
        ThrowingClock clock = new ThrowingClock(20);

        PipelineExecutionResult result = execute(
                compatiblePasses(
                        new ArrayList<String>(), publisher, true, null, false),
                publisher,
                clock,
                new MutableCancellation());

        assertEquals(0, publisher.calls());
        assertEquals(CompilationSessionState.FAILED, result.state());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_OBSERVER_FAILURE
                        && "pipeline.clock.failure".equals(
                                diagnostic.messageKey())));
    }

    /** candidate 准备后的取消必须在 commit 前生效。 */
    @Test
    void cancellationAfterPreparationBlocksCommit() {
        CountingPublisher publisher = new CountingPublisher(PublicationStatus.PUBLISHED);
        MutableCancellation cancellation = new MutableCancellation();

        PipelineExecutionResult result = execute(
                compatiblePasses(
                        new ArrayList<String>(),
                        publisher,
                        true,
                        cancellation::cancel,
                        false),
                publisher,
                new StableClock(),
                cancellation);

        assertEquals(0, publisher.calls());
        assertEquals(CompilationSessionState.FAILED, result.state());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_COMPILATION_CANCELLED));
    }

    /** candidate 准备后的 Pass 异常必须阻断外部提交。 */
    @Test
    void publicationPassFailureAfterPreparationBlocksCommit() {
        CountingPublisher publisher = new CountingPublisher(PublicationStatus.PUBLISHED);

        PipelineExecutionResult result = execute(
                compatiblePasses(
                        new ArrayList<String>(), publisher, true, null, true),
                publisher,
                new StableClock(),
                new MutableCancellation());

        assertEquals(0, publisher.calls());
        assertEquals(CompilationSessionState.FAILED, result.state());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_PUBLICATION_FAILURE));
    }

    /** start-clock 失败时不得声称 Pass 已执行。 */
    @Test
    void startClockFailureDoesNotRecordUnexecutedPass() {
        CountingPublisher publisher = new CountingPublisher(PublicationStatus.PUBLISHED);
        List<String> executions = new ArrayList<String>();

        PipelineExecutionResult result = execute(
                compatiblePasses(executions, publisher, false, null, false),
                publisher,
                new ThrowingClock(1),
                new MutableCancellation());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertTrue(executions.isEmpty());
        assertTrue(result.executedPasses().isEmpty());
        assertTrue(result.timings().isEmpty());
        assertEquals(0, publisher.calls());
    }

    /** 创建可在 I001、I002 与 I003 Pipeline 上运行的十阶段 Pass 列表。 */
    private static List<CompilerPass> compatiblePasses(
            List<String> executions,
            CountingPublisher publisher,
            boolean legacyPublish,
            Runnable afterPublish,
            boolean throwAfterPublish) {
        List<CompilerPass> passes = new ArrayList<CompilerPass>();
        for (int index = 0; index < 9; index++) {
            passes.add(new SimplePass(
                    CompilerPipeline.fixedPassOrder().get(index), executions));
        }
        passes.add(new CompatiblePublicationPass(
                executions,
                publisher,
                legacyPublish,
                afterPublish,
                throwAfterPublish));
        return passes;
    }

    /** 创建可保留 Context 并按需返回 ERROR 的普通 Pass。 */
    private static CompilerPass retainingPass(
            String name,
            List<PassContext> retained,
            Diagnostic diagnostic) {
        return new CompilerPass() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public PassResult execute(PassContext context) {
                retained.add(context);
                context.putArtifact("current", name);
                if (diagnostic == null) {
                    return PassResult.passed();
                }
                return PassResult.of(Collections.singletonList(diagnostic));
            }
        };
    }

    /** 执行 Pipeline。 */
    private static PipelineExecutionResult execute(
            List<CompilerPass> passes,
            CountingPublisher publisher,
            MonotonicClock clock,
            MutableCancellation cancellation) {
        return execute(new CompilerPipeline(passes), publisher, clock, cancellation);
    }

    /** 执行已构造的 Pipeline。 */
    private static PipelineExecutionResult execute(
            CompilerPipeline pipeline,
            CountingPublisher publisher,
            MonotonicClock clock,
            MutableCancellation cancellation) {
        return pipeline.execute(
                request(clock, cancellation),
                new PublicationRequest(
                        Optional.<EngineContext>empty(), publisher));
    }

    /** 创建完整但不执行真实 Source/Frontend 的请求。 */
    private static CompilationRequest request(
            MonotonicClock clock,
            MutableCancellation cancellation) {
        DocumentSourceProvider provider = new DocumentSourceProvider() {
            @Override
            public SourceResolutionResult resolve(
                    SourceReference reference,
                    SourceResolutionContext context) {
                return null;
            }

            @Override
            public SourceResolutionResult resolveFileSet(
                    SourceReference reference,
                    SourceResolutionContext context) {
                return null;
            }
        };
        FrontendRegistry frontends = new FrontendRegistry() {
            @Override
            public DocumentFrontend require(DocumentFormat format) {
                return null;
            }
        };
        CompilationObserver observer = new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
            }
        };
        return new CompilationRequest(
                new SourceReference("classpath:mix/i002.xml"),
                provider,
                frontends,
                new CompilationOptions("schema-v1", "options-v1"),
                Optional.<Deadline>empty(),
                cancellation,
                clock,
                observer);
    }

    /** 反射调用 I001 普通 Context 暴露的 publisher；I002 后方法不存在。 */
    private static void invokeLegacyPublisherIfPresent(
            PassContext context,
            CountingPublisher publisher) {
        try {
            Method method = PassContext.class.getMethod("publicationRequest");
            PublicationRequest request = (PublicationRequest) method.invoke(context);
            request.publisher().publish(request.expectedCurrent(), candidate());
        } catch (NoSuchMethodException expectedAfterI002) {
            // I002 普通 Context 已移除发布能力。
        } catch (IllegalAccessException failure) {
            throw new AssertionError(failure);
        } catch (InvocationTargetException failure) {
            throw new AssertionError(failure.getCause());
        }
    }

    /** 创建最小不可变候选 EngineContext。 */
    private static EngineContext candidate() {
        CompiledModelSet modelSet = new CompiledModelSet(
                PublishedSourceManifest.empty(),
                dec.core.context.model.CompiledViewMaterializationIndex.empty(),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                Collections.<Diagnostic>emptyList(),
                new DigestPair("source-i002", "semantic-i002"),
                "compiler-i002",
                "schema-i002",
                "options-i002");
        return new EngineContext(modelSet);
    }

    /** 普通成功 Pass。 */
    private static final class SimplePass implements CompilerPass {
        private final String name;
        private final List<String> executions;

        private SimplePass(String name, List<String> executions) {
            this.name = name;
            this.executions = executions;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public PassResult execute(PassContext context) {
            executions.add(name);
            context.putArtifact("last-pass", name);
            return PassResult.passed();
        }
    }

    /** 同时兼容旧普通调用和 Publication 专用调用的测试 Pass。 */
    private static final class CompatiblePublicationPass
            implements PublicationCompilerPass {
        private final List<String> executions;
        private final CountingPublisher publisher;
        private final boolean legacyPublish;
        private final Runnable afterPublish;
        private final boolean throwAfterPublish;

        private CompatiblePublicationPass(
                List<String> executions,
                CountingPublisher publisher,
                boolean legacyPublish,
                Runnable afterPublish,
                boolean throwAfterPublish) {
            this.executions = executions;
            this.publisher = publisher;
            this.legacyPublish = legacyPublish;
            this.afterPublish = afterPublish;
            this.throwAfterPublish = throwAfterPublish;
        }

        @Override
        public String name() {
            return CompilerPipeline.PUBLICATION_PASS;
        }

        @Override
        public PassResult execute(PassContext context) {
            executions.add(name());
            if (legacyPublish) {
                invokeLegacyPublisherIfPresent(context, publisher);
            }
            afterPublicationAction();
            return PassResult.passed();
        }

        @Override
        public PassResult execute(PublicationPassContext context) {
            executions.add(name());
            context.publish(candidate());
            afterPublicationAction();
            return PassResult.passed();
        }

        /** 在候选准备后模拟 token 变化或 Pass 自身异常。 */
        private void afterPublicationAction() {
            if (afterPublish != null) {
                afterPublish.run();
            }
            if (throwAfterPublish) {
                throw new IllegalStateException("failure-after-publication-preparation");
            }
        }
    }

    /** 可配置返回状态并记录调用次数的 Publisher。 */
    private static final class CountingPublisher implements ContextPublisher {
        private final PublicationStatus status;
        private int calls;

        private CountingPublisher(PublicationStatus status) {
            this.status = status;
        }

        @Override
        public PublicationResult publish(
                Optional<EngineContext> expectedCurrent,
                EngineContext candidate) {
            calls++;
            return new PublicationResult() {
                @Override
                public PublicationStatus status() {
                    return status;
                }
            };
        }

        /** 返回真实 publisher 调用次数。 */
        private int calls() {
            return calls;
        }
    }

    /** 可变取消令牌。 */
    private static final class MutableCancellation implements CancellationToken {
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

    /** 每次读取稳定递增的时钟。 */
    private static final class StableClock implements MonotonicClock {
        private long value;

        @Override
        public long nanoTime() {
            return value++;
        }
    }

    /** 在指定读取序号抛异常的时钟。 */
    private static final class ThrowingClock implements MonotonicClock {
        private final int throwOnCall;
        private int calls;

        private ThrowingClock(int throwOnCall) {
            this.throwOnCall = throwOnCall;
        }

        @Override
        public long nanoTime() {
            calls++;
            if (calls == throwOnCall) {
                throw new IllegalStateException("controlled-clock-failure");
            }
            return calls;
        }
    }
}
