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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 / I004：artifact snapshot 资源边界与共享 DAG 阻断测试。
 */
class CompilerPipelineReworkI004Test {
    private static final int DEFAULT_MAX_DEPTH = 256;

    /** 默认深度预算超限必须形成稳定失败，不能执行 publisher。 */
    @Test
    void defaultDepthLimitFailsClosedWithoutJvmError() {
        CountingPublisher publisher = new CountingPublisher();
        Object deep = nestedChain(DEFAULT_MAX_DEPTH + 1);

        PipelineExecutionResult result = assertDoesNotThrow(() -> execute(
                passesWithArtifact("deep-chain", deep),
                publisher));

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, publisher.calls());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code() == DiagnosticCode.MIX_PUBLICATION_BLOCKED
                        && "pipeline.artifact.resource-exceeded".equals(
                                diagnostic.messageKey())));
    }

    /** 深度恰好等于默认预算时仍应成功发布。 */
    @Test
    void defaultDepthBoundarySucceeds() {
        CountingPublisher publisher = new CountingPublisher();

        PipelineExecutionResult result = execute(
                passesWithArtifact(
                        "depth-boundary",
                        nestedChain(DEFAULT_MAX_DEPTH)),
                publisher);

        assertEquals(CompilationSessionState.PUBLISHED, result.state(),
                result.diagnostics().toString());
        assertEquals(1, publisher.calls());
    }

    /** 24 层共享 DAG 必须线性遍历并复用同一个冻结子图 identity。 */
    @Test
    void sharedDagDepth24IsMemoizedAndLinear() {
        AtomicInteger reads = new AtomicInteger();
        Object source = sharedDag(24, reads, 200);

        Object frozen = assertDoesNotThrow(() -> ArtifactSnapshots.freeze(source));
        List<?> root = (List<?>) frozen;

        assertSame(root.get(0), root.get(1));
        assertTrue(reads.get() <= 64, "共享 DAG 不应重复展开：" + reads.get());
    }

    /** 小预算下深度等于上限成功，超过上限稳定失败。 */
    @Test
    void smallDepthBudgetHasExactBoundary() {
        assertDoesNotThrow(() -> freezeWithLimits(
                nestedChain(4), 4, 20, 20, 20));
        assertThrows(IllegalArgumentException.class, () -> freezeWithLimits(
                nestedChain(5), 4, 20, 20, 20));
    }

    /** 唯一容器预算按 source identity 计数，并具有精确边界。 */
    @Test
    void uniqueContainerBudgetHasExactBoundary() {
        assertDoesNotThrow(() -> freezeWithLimits(
                uniqueChildren(3), 8, 4, 10, 10));
        assertThrows(IllegalArgumentException.class, () -> freezeWithLimits(
                uniqueChildren(4), 8, 4, 20, 20));
    }

    /** 宽 List 与 Set 超过 edge 预算时都必须稳定失败。 */
    @Test
    void wideListAndSetRespectEdgeBudget() {
        List<Integer> list = new ArrayList<Integer>();
        Set<Integer> set = new LinkedHashSet<Integer>();
        for (int index = 0; index < 5; index++) {
            list.add(Integer.valueOf(index));
            set.add(Integer.valueOf(index));
        }

        assertThrows(IllegalArgumentException.class, () -> freezeWithLimits(
                list, 8, 8, 4, 8));
        assertThrows(IllegalArgumentException.class, () -> freezeWithLimits(
                set, 8, 8, 4, 8));
    }

    /** Map entry 与 key/value edge 预算必须在物化前阻断。 */
    @Test
    void mapEntryBudgetFailsBeforeMaterialization() {
        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put("a", "1");
        values.put("b", "2");
        values.put("c", "3");

        assertThrows(IllegalArgumentException.class, () -> freezeWithLimits(
                values, 8, 8, 20, 2));
        assertThrows(IllegalArgumentException.class, () -> freezeWithLimits(
                values, 8, 8, 5, 8));
    }

    /** 原有循环图必须继续稳定拒绝，不能被共享 memoization 误判为 DAG。 */
    @Test
    void cycleStillFailsClosed() {
        List<Object> cycle = new ArrayList<Object>();
        cycle.add(cycle);

        assertThrows(IllegalArgumentException.class,
                () -> ArtifactSnapshots.freeze(cycle));
    }

    /** 创建指定容器深度的单元素无环链。 */
    private static Object nestedChain(int depth) {
        Object value = "leaf";
        for (int index = 0; index < depth; index++) {
            value = Collections.singletonList(value);
        }
        return value;
    }

    /** 创建包含指定数量唯一空 List 子节点的根 List。 */
    private static List<Object> uniqueChildren(int count) {
        List<Object> root = new ArrayList<Object>();
        for (int index = 0; index < count; index++) {
            root.add(new ArrayList<Object>());
        }
        return root;
    }

    /**
     * 创建每层两次引用同一子节点的共享 DAG，并通过读取上限避免旧实现耗尽堆。
     */
    private static Object sharedDag(
            int depth,
            AtomicInteger reads,
            int readLimit) {
        Object value = "leaf";
        for (int index = 0; index < depth; index++) {
            value = new CountingPairList(value, reads, readLimit);
        }
        return value;
    }

    /** 通过反射调用 I004 package-private limits API，使 RED 可在 I003 Head 编译。 */
    private static Object freezeWithLimits(
            Object value,
            int maxDepth,
            int maxUniqueContainers,
            int maxEdges,
            int maxMapEntries) {
        try {
            Class<?> limitsType = Class.forName(
                    "dec.core.compiler.pass.ArtifactSnapshots$Limits");
            Constructor<?> constructor = limitsType.getDeclaredConstructor(
                    int.class,
                    int.class,
                    int.class,
                    int.class);
            constructor.setAccessible(true);
            Object limits = constructor.newInstance(
                    Integer.valueOf(maxDepth),
                    Integer.valueOf(maxUniqueContainers),
                    Integer.valueOf(maxEdges),
                    Integer.valueOf(maxMapEntries));
            Method method = ArtifactSnapshots.class.getDeclaredMethod(
                    "freeze",
                    Object.class,
                    limitsType);
            method.setAccessible(true);
            return method.invoke(null, value, limits);
        } catch (InvocationTargetException failure) {
            Throwable cause = failure.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException("artifact snapshot failed", cause);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("I004 limits API missing", failure);
        }
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

    /** 执行 Pipeline 并使用计数 Publisher 验证资源失败不触达外部提交。 */
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

    /** 每次 get 都计数的两元素共享 List。 */
    private static final class CountingPairList extends AbstractList<Object> {
        private final Object child;
        private final AtomicInteger reads;
        private final int readLimit;

        private CountingPairList(
                Object child,
                AtomicInteger reads,
                int readLimit) {
            this.child = child;
            this.reads = reads;
            this.readLimit = readLimit;
        }

        @Override
        public Object get(int index) {
            int current = reads.incrementAndGet();
            if (current > readLimit) {
                throw new IllegalStateException("shared DAG expanded repeatedly");
            }
            if (index < 0 || index >= 2) {
                throw new IndexOutOfBoundsException(String.valueOf(index));
            }
            return child;
        }

        @Override
        public int size() {
            return 2;
        }
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
