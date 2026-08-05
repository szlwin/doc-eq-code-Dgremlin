package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CancellationToken;
import dec.core.compiler.api.CompilationObserver;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.MonotonicClock;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.context.EngineContext;
import dec.core.context.model.DiagnosticCode;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 / I004 独立 Review：共享 identity、组合预算和失败映射。
 */
class CompilerPipelineReworkI004IndependentReviewTest {

    /** List 与 Optional 多次引用同一子图时必须复用同一冻结对象。 */
    @Test
    void optionalAndListShareOneFrozenIdentity() {
        List<String> shared = new ArrayList<String>();
        shared.add("shared");
        List<Object> source = Arrays.<Object>asList(
                shared,
                Optional.of(shared));

        List<?> frozen = (List<?>) ArtifactSnapshots.freeze(source);
        Optional<?> optional = (Optional<?>) frozen.get(1);

        assertSame(frozen.get(0), optional.get());
    }

    /** Map 的 key 与 value 指向同一 source identity 时必须复用快照。 */
    @Test
    void mapKeyAndValueShareOneFrozenIdentity() {
        List<String> shared = new ArrayList<String>();
        shared.add("identity");
        Map<Object, Object> source = new IdentityHashMap<Object, Object>();
        source.put(shared, shared);

        Map<?, ?> frozen = (Map<?, ?>) ArtifactSnapshots.freeze(source);
        Map.Entry<?, ?> entry = frozen.entrySet().iterator().next();

        assertSame(entry.getKey(), entry.getValue());
    }

    /** Result 必须保留共享 DAG 的冻结 identity，且成功路径只发布一次。 */
    @Test
    void pipelineResultPreservesSharedSnapshotIdentity() {
        List<String> shared = new ArrayList<String>();
        shared.add("result-shared");
        List<Object> source = Arrays.<Object>asList(shared, shared);
        CountingPublisher publisher = new CountingPublisher();

        PipelineExecutionResult result = execute(
                passesWithArtifact("shared-result", source),
                publisher);

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        assertEquals(1, publisher.calls());
        List<?> frozen = (List<?>) result.artifacts().get("shared-result");
        assertSame(frozen.get(0), frozen.get(1));
    }

    /** 已冻结共享节点在更深路径再次出现时不能绕过深度预算。 */
    @Test
    void frozenSharedNodeCannotBypassDeeperPathLimit() {
        List<String> shared = Collections.singletonList("shared");
        List<Object> source = Arrays.<Object>asList(
                shared,
                Collections.singletonList(
                        Collections.singletonList(shared)));

        assertThrows(ArtifactSnapshots.ResourceLimitException.class,
                () -> ArtifactSnapshots.freeze(
                        source,
                        new ArtifactSnapshots.Limits(3, 20, 20, 20)));
    }

    /**
     * 共享 DAG 作为 Set 元素时，构建目标 Set 不得递归展开其 hashCode。
     */
    @Test
    void sharedDagSetElementDoesNotAmplifyHashComputation() {
        AtomicInteger hashCalls = new AtomicInteger();
        CountingImmutableArtifact leaf = new CountingImmutableArtifact(
                hashCalls,
                64);
        Object dag = sharedDag(20, leaf);
        Set<Object> source = Collections.newSetFromMap(
                new IdentityHashMap<Object, Boolean>());
        source.add(dag);

        Set<?> frozen = (Set<?>) assertDoesNotThrow(
                () -> ArtifactSnapshots.freeze(source));

        assertEquals(1, frozen.size());
        assertTrue(hashCalls.get() <= 4,
                "目标 Set 不应递归计算共享 DAG：" + hashCalls.get());
    }

    /** final Pass 内资源超限必须准确分类且完全不触达 publisher。 */
    @Test
    void publicationPassResourceFailureBlocksPublisher() {
        CountingPublisher publisher = new CountingPublisher();
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(9, new PublicationCompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.PUBLICATION_PASS;
            }

            @Override
            public PassResult execute(PublicationPassContext context) {
                ArtifactSnapshots.freeze(nestedChain(257));
                context.prepare(PipelineTestSupport.candidate());
                return PassResult.passed();
            }
        });

        PipelineExecutionResult result = execute(passes, publisher);

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_PUBLICATION_BLOCKED
                        && "pipeline.artifact.resource-exceeded".equals(
                                diagnostic.messageKey())
                        && CompilerPipeline.PUBLICATION_PASS.equals(
                                diagnostic.pass())));
    }

    /** 非资源型容器迭代异常仍应使用普通 Pass failure，不能伪装成预算超限。 */
    @Test
    void iteratorFailureRemainsOrdinaryPassFailure() {
        CountingPublisher publisher = new CountingPublisher();
        List<Object> failing = new AbstractList<Object>() {
            @Override
            public Object get(int index) {
                throw new IllegalStateException("controlled-iterator-failure");
            }

            @Override
            public int size() {
                return 1;
            }

            @Override
            public Iterator<Object> iterator() {
                throw new IllegalStateException("controlled-iterator-failure");
            }
        };

        PipelineExecutionResult result = execute(
                passesWithArtifact("failing-iterator", failing),
                publisher);

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "pipeline.pass.failure".equals(diagnostic.messageKey())));
        assertTrue(result.diagnostics().stream().noneMatch(diagnostic ->
                "pipeline.artifact.resource-exceeded".equals(
                        diagnostic.messageKey())));
    }

    /** 所有预算参数必须严格为正，避免无意义或含糊的边界。 */
    @Test
    void limitsRejectZeroAndNegativeValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new ArtifactSnapshots.Limits(0, 1, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ArtifactSnapshots.Limits(1, 0, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ArtifactSnapshots.Limits(1, 1, 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ArtifactSnapshots.Limits(1, 1, 1, -1));
    }

    /** List、Set、Map 与 Optional 组合快照必须保持不可变。 */
    @Test
    void compositeSnapshotIsDeeplyUnmodifiable() {
        Set<String> sourceSet = new LinkedHashSet<String>();
        sourceSet.add("set-item");
        Map<String, Object> source = new LinkedHashMap<String, Object>();
        source.put("list", new ArrayList<String>(
                Collections.singletonList("list-item")));
        source.put("set", sourceSet);
        source.put("optional", Optional.of(
                new ArrayList<String>(Collections.singletonList("optional-item"))));

        Map<?, ?> frozen = (Map<?, ?>) ArtifactSnapshots.freeze(source);
        List<?> frozenList = (List<?>) frozen.get("list");
        Set<?> frozenSet = (Set<?>) frozen.get("set");
        Optional<?> frozenOptional = (Optional<?>) frozen.get("optional");

        assertThrows(UnsupportedOperationException.class,
                () -> ((Map<Object, Object>) frozen).put("late", "mutation"));
        assertThrows(UnsupportedOperationException.class,
                () -> ((List<Object>) frozenList).add("mutation"));
        assertThrows(UnsupportedOperationException.class,
                () -> ((Set<Object>) frozenSet).add("mutation"));
        assertThrows(UnsupportedOperationException.class,
                () -> ((List<Object>) frozenOptional.get()).add("mutation"));
    }

    /** 创建指定容器深度的单元素无环链。 */
    private static Object nestedChain(int depth) {
        Object value = "leaf";
        for (int index = 0; index < depth; index++) {
            value = Collections.singletonList(value);
        }
        return value;
    }

    /** 创建每层两次引用同一子节点的共享 DAG。 */
    private static Object sharedDag(int depth, Object leaf) {
        Object value = leaf;
        for (int index = 0; index < depth; index++) {
            value = Arrays.<Object>asList(value, value);
        }
        return value;
    }

    /** 创建在首 Pass 写入指定 artifact 的固定十阶段 Pipeline。 */
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

    /** 执行 Pipeline 并使用计数 Publisher 验证提交边界。 */
    private static PipelineExecutionResult execute(
            List<CompilerPass> passes,
            CountingPublisher publisher) {
        return new CompilerPipeline(passes).execute(
                PipelineTestSupport.request(
                        new IncrementingClock(),
                        new NeverCancelled(),
                        Optional.empty(),
                        new StableObserver()),
                PipelineTestSupport.publicationRequest(publisher));
    }

    /** 记录真实 publisher 调用次数。 */
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

    /** 受信任的不可变叶子，用于统计目标容器 hash 放大。 */
    private static final class CountingImmutableArtifact
            implements ImmutablePipelineArtifact {
        private final AtomicInteger hashCalls;
        private final int limit;

        private CountingImmutableArtifact(
                AtomicInteger hashCalls,
                int limit) {
            this.hashCalls = hashCalls;
            this.limit = limit;
        }

        @Override
        public int hashCode() {
            int current = hashCalls.incrementAndGet();
            if (current > limit) {
                throw new IllegalStateException("shared DAG hash expanded repeatedly");
            }
            return 31;
        }

        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    /** 提供稳定递增纳秒值。 */
    private static final class IncrementingClock implements MonotonicClock {
        private long value;

        @Override
        public long nanoTime() {
            return value++;
        }
    }

    /** 永不取消的稳定 Token。 */
    private static final class NeverCancelled implements CancellationToken {
        @Override
        public boolean isCancellationRequested() {
            return false;
        }
    }

    /** 不改变语义事实的稳定 Observer。 */
    private static final class StableObserver implements CompilationObserver {
        @Override
        public void onTiming(CompilationTiming timing) {
        }

        @Override
        public void onStateTransition(SessionStateTransition transition) {
        }
    }
}
