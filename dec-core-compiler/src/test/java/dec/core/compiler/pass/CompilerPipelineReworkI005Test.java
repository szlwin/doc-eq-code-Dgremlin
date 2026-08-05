package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 / I005 RED：冻结结果 equality/query 必须按唯一图规模受控执行。
 */
class CompilerPipelineReworkI005Test {

    /** 两个独立 freeze Session 的 24 层共享 DAG equals 不得按路径指数展开。 */
    @Test
    void sharedDagListEqualsIsLinearAcrossFreezeSessions() {
        CountingLeaf.reset(200);
        Object left = ArtifactSnapshots.freeze(sharedDag(24, new CountingLeaf("same")));
        Object right = ArtifactSnapshots.freeze(sharedDag(24, new CountingLeaf("same")));

        boolean equal = assertDoesNotThrow(() -> left.equals(right));

        assertTrue(equal);
        assertTrue(CountingLeaf.calls() <= 64,
                "leaf equals calls must follow unique graph size: "
                        + CountingLeaf.calls());
    }

    /** FrozenSet.contains 查询独立共享 DAG 时不得调用递归 List equals。 */
    @Test
    void sharedDagSetContainsIsLinear() {
        CountingLeaf.reset(200);
        Object stored = sharedDag(24, new CountingLeaf("set"));
        Set<Object> source = Collections.newSetFromMap(
                new IdentityHashMap<Object, Boolean>());
        source.add(stored);
        Set<?> frozen = (Set<?>) ArtifactSnapshots.freeze(source);
        Object query = ArtifactSnapshots.freeze(
                sharedDag(24, new CountingLeaf("set")));

        boolean present = assertDoesNotThrow(() -> frozen.contains(query));

        assertTrue(present);
        assertTrue(CountingLeaf.calls() <= 64,
                "set query must compare each shared pair once: "
                        + CountingLeaf.calls());
    }

    /** FrozenMap.get/containsKey 查询共享 DAG key 时必须复用同一受控比较合同。 */
    @Test
    void sharedDagMapQueriesAreLinear() {
        CountingLeaf.reset(300);
        Map<Object, Object> source = new IdentityHashMap<Object, Object>();
        source.put(sharedDag(24, new CountingLeaf("map")), "value");
        Map<?, ?> frozen = (Map<?, ?>) ArtifactSnapshots.freeze(source);
        Object query = ArtifactSnapshots.freeze(
                sharedDag(24, new CountingLeaf("map")));

        Object value = assertDoesNotThrow(() -> frozen.get(query));
        boolean present = assertDoesNotThrow(() -> frozen.containsKey(query));

        assertEquals("value", value);
        assertTrue(present);
        assertTrue(CountingLeaf.calls() <= 128,
                "map queries must remain linear: " + CountingLeaf.calls());
    }

    /** FrozenEntrySet.contains 必须受控比较普通外部 Entry 的 key/value。 */
    @Test
    void sharedDagEntrySetContainsIsLinear() {
        CountingLeaf.reset(200);
        Map<Object, Object> source = new IdentityHashMap<Object, Object>();
        source.put(sharedDag(24, new CountingLeaf("entry")), "value");
        Map<?, ?> frozen = (Map<?, ?>) ArtifactSnapshots.freeze(source);
        Object query = ArtifactSnapshots.freeze(
                sharedDag(24, new CountingLeaf("entry")));
        Map.Entry<Object, Object> external =
                new AbstractMap.SimpleImmutableEntry<Object, Object>(
                        query,
                        "value");

        boolean present = assertDoesNotThrow(
                () -> frozen.entrySet().contains(external));

        assertTrue(present);
        assertTrue(CountingLeaf.calls() <= 64,
                "entry query must compare each shared pair once: "
                        + CountingLeaf.calls());
    }

    /** comparison depth 在边界成功，超过边界时稳定拒绝。 */
    @Test
    void comparisonDepthBudgetHasExactBoundary() {
        Object leftBoundary = ArtifactSnapshots.freeze(nestedChain(3, "leaf"));
        Object rightBoundary = ArtifactSnapshots.freeze(nestedChain(3, "leaf"));
        Object leftTooDeep = ArtifactSnapshots.freeze(nestedChain(4, "leaf"));
        Object rightTooDeep = ArtifactSnapshots.freeze(nestedChain(4, "leaf"));

        assertTrue(assertDoesNotThrow(() -> controlledEquals(
                leftBoundary,
                rightBoundary,
                4,
                32,
                64,
                32)));
        RuntimeException failure = assertThrows(RuntimeException.class,
                () -> controlledEquals(
                        leftTooDeep,
                        rightTooDeep,
                        4,
                        64,
                        128,
                        64));
        assertEquals("ComparisonLimitException",
                failure.getClass().getSimpleName());
    }

    /** 同一共享 pair 命中 memo 后不重复消耗 pair budget。 */
    @Test
    void comparisonPairBudgetCountsUniquePairs() {
        Object left = ArtifactSnapshots.freeze(sharedDag(5, "leaf"));
        Object right = ArtifactSnapshots.freeze(sharedDag(5, "leaf"));

        assertTrue(assertDoesNotThrow(() -> controlledEquals(
                left,
                right,
                16,
                7,
                32,
                16)));
        RuntimeException failure = assertThrows(RuntimeException.class,
                () -> controlledEquals(
                        left,
                        right,
                        16,
                        5,
                        32,
                        16));
        assertEquals("ComparisonLimitException",
                failure.getClass().getSimpleName());
    }

    /** 缓存 hash 相同但结构不同不能直接判定相等。 */
    @Test
    void equalHashDoesNotHideDifferentStructure() {
        Object left = ArtifactSnapshots.freeze(Arrays.<Object>asList(
                new FixedHashLeaf("left"),
                "tail"));
        Object right = ArtifactSnapshots.freeze(Arrays.<Object>asList(
                new FixedHashLeaf("right"),
                "tail"));

        assertEquals(left.hashCode(), right.hashCode());
        assertFalse(left.equals(right));
    }

    /** 小规模 Frozen 与普通 Java Collection 必须保持对称和 hash 一致。 */
    @Test
    void ordinaryCollectionsRemainSymmetric() {
        List<Object> ordinaryList = Arrays.<Object>asList(
                "a",
                Arrays.asList("b", "c"));
        Set<Object> ordinarySet = new LinkedHashSet<Object>(
                Arrays.<Object>asList("x", "y"));
        Map<Object, Object> ordinaryMap = new LinkedHashMap<Object, Object>();
        ordinaryMap.put(Arrays.asList("key"), Arrays.asList("value"));

        Object frozenList = ArtifactSnapshots.freeze(ordinaryList);
        Object frozenSet = ArtifactSnapshots.freeze(ordinarySet);
        Object frozenMap = ArtifactSnapshots.freeze(ordinaryMap);

        assertTrue(frozenList.equals(ordinaryList));
        assertTrue(ordinaryList.equals(frozenList));
        assertEquals(ordinaryList.hashCode(), frozenList.hashCode());
        assertTrue(frozenSet.equals(ordinarySet));
        assertTrue(ordinarySet.equals(frozenSet));
        assertEquals(ordinarySet.hashCode(), frozenSet.hashCode());
        assertTrue(frozenMap.equals(ordinaryMap));
        assertTrue(ordinaryMap.equals(frozenMap));
        assertEquals(ordinaryMap.hashCode(), frozenMap.hashCode());
    }

    /** 构造每层两次引用同一子图的共享 DAG。 */
    private static Object sharedDag(int depth, Object leaf) {
        Object value = leaf;
        for (int index = 0; index < depth; index++) {
            value = Arrays.<Object>asList(value, value);
        }
        return value;
    }

    /** 构造指定深度的单元素无环链。 */
    private static Object nestedChain(int depth, Object leaf) {
        Object value = leaf;
        for (int index = 0; index < depth; index++) {
            value = Collections.singletonList(value);
        }
        return value;
    }

    /**
     * 反射调用 I005 package-private 比较边界，使 RED 可在旧生产代码上编译。
     */
    private static boolean controlledEquals(
            Object left,
            Object right,
            int maxDepth,
            int maxPairs,
            int maxEdges,
            int maxCanonicalNodes) {
        try {
            Class<?> limitsType = Class.forName(
                    "dec.core.compiler.pass.ArtifactSnapshots$ComparisonLimits");
            Constructor<?> constructor = limitsType.getDeclaredConstructor(
                    int.class,
                    int.class,
                    int.class,
                    int.class);
            constructor.setAccessible(true);
            Object limits = constructor.newInstance(
                    Integer.valueOf(maxDepth),
                    Integer.valueOf(maxPairs),
                    Integer.valueOf(maxEdges),
                    Integer.valueOf(maxCanonicalNodes));
            Method method = ArtifactSnapshots.class.getDeclaredMethod(
                    "controlledEquals",
                    Object.class,
                    Object.class,
                    limitsType);
            method.setAccessible(true);
            return ((Boolean) method.invoke(null, left, right, limits))
                    .booleanValue();
        } catch (InvocationTargetException failure) {
            Throwable cause = failure.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException("I005 comparison invocation failed", cause);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("I005 comparison API missing", failure);
        }
    }

    /** 统计 leaf equals 调用并在指数展开时快速中止 RED。 */
    private static final class CountingLeaf implements ImmutablePipelineArtifact {
        private static final AtomicInteger CALLS = new AtomicInteger();
        private static volatile int limit;
        private final String value;

        private CountingLeaf(String value) {
            this.value = value;
        }

        /** 重置操作计数及保护上限。 */
        private static void reset(int newLimit) {
            CALLS.set(0);
            limit = newLimit;
        }

        /** 返回真实 equals 调用次数。 */
        private static int calls() {
            return CALLS.get();
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            int current = CALLS.incrementAndGet();
            if (current > limit) {
                throw new IllegalStateException(
                        "shared DAG equality expanded repeatedly");
            }
            return other instanceof CountingLeaf
                    && value.equals(((CountingLeaf) other).value);
        }
    }

    /** 固定 hash、按 value 精确比较的 immutable leaf。 */
    private static final class FixedHashLeaf implements ImmutablePipelineArtifact {
        private final String value;

        private FixedHashLeaf(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return 17;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof FixedHashLeaf
                    && value.equals(((FixedHashLeaf) other).value);
        }
    }
}
