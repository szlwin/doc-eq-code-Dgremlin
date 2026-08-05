package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 / I006 独立 Review：复核 operation cache、iterator 和精确预算边界。
 */
class CompilerPipelineReworkI006IndependentReviewTest {

    /** lastIndexOf 的全部候选必须共享同一 NOT_EQUAL pair 结果。 */
    @Test
    void reverseListQuerySharesOperationCache() {
        Object stored = sharedDag(14, new CountingLeaf("stored"));
        List<Object> values = new ArrayList<Object>();
        for (int index = 0; index < 10; index++) {
            values.add(stored);
        }
        List<?> frozen = (List<?>) ArtifactSnapshots.freeze(values);
        Object query = ArtifactSnapshots.freeze(
                sharedDag(14, new CountingLeaf("missing")));
        CountingLeaf.reset(3);

        int result = assertDoesNotThrow(() -> frozen.lastIndexOf(query));

        assertEquals(-1, result);
        assertTrue(CountingLeaf.calls() <= 2,
                "reverse query 不得按候选数重复展开共享 pair");
    }

    /** Map 多个 key 共享子图时，get 与 containsKey 各自保持近线性。 */
    @Test
    void mapKeyQueriesShareCompletedChildPairs() {
        Object storedChild = sharedDag(12, new CountingLeaf("shared"));
        Map<Object, Object> source = new LinkedHashMap<Object, Object>();
        for (int index = 0; index < 10; index++) {
            source.put(Arrays.<Object>asList(storedChild, "k-" + index), index);
        }
        Map<?, ?> frozen = (Map<?, ?>) ArtifactSnapshots.freeze(source);
        Object queryChild = ArtifactSnapshots.freeze(
                sharedDag(12, new CountingLeaf("different")));
        Object query = Arrays.<Object>asList(queryChild, "missing");

        CountingLeaf.reset(3);
        assertNull(assertDoesNotThrow(() -> frozen.get(query)));
        assertTrue(CountingLeaf.calls() <= 2);

        CountingLeaf.reset(3);
        assertFalse(assertDoesNotThrow(() -> frozen.containsKey(query)));
        assertTrue(CountingLeaf.calls() <= 2);
    }

    /** Set 多个 element 的相等前缀 pair 必须跨候选复用。 */
    @Test
    void setQueryReusesEqualPrefixPair() {
        Object storedChild = sharedDag(12, new CountingLeaf("equal"));
        Set<Object> source = Collections.newSetFromMap(
                new IdentityHashMap<Object, Boolean>());
        for (int index = 0; index < 10; index++) {
            source.add(Arrays.<Object>asList(storedChild, "s-" + index));
        }
        Set<?> frozen = (Set<?>) ArtifactSnapshots.freeze(source);
        Object queryChild = ArtifactSnapshots.freeze(
                sharedDag(12, new CountingLeaf("equal")));
        Object query = Arrays.<Object>asList(queryChild, "missing");
        CountingLeaf.reset(3);

        assertFalse(assertDoesNotThrow(() -> frozen.contains(query)));
        assertTrue(CountingLeaf.calls() <= 2,
                "EQUAL 子 pair 应在 operation 内跨候选复用");
    }

    /** entrySet.contains 的 key 相等前缀不得按 entry 数重复比较。 */
    @Test
    void entrySetQueryReusesEqualPrefixPair() {
        Object storedChild = sharedDag(10, new CountingLeaf("entry"));
        Map<Object, Object> source = new LinkedHashMap<Object, Object>();
        for (int index = 0; index < 8; index++) {
            source.put(Arrays.<Object>asList(storedChild, "e-" + index), "v");
        }
        Map<?, ?> frozen = (Map<?, ?>) ArtifactSnapshots.freeze(source);
        Object queryChild = ArtifactSnapshots.freeze(
                sharedDag(10, new CountingLeaf("entry")));
        Map.Entry<Object, Object> query =
                new AbstractMap.SimpleImmutableEntry<Object, Object>(
                        Arrays.<Object>asList(queryChild, "missing"),
                        "v");
        CountingLeaf.reset(3);

        assertFalse(assertDoesNotThrow(
                () -> frozen.entrySet().contains(query)));
        assertTrue(CountingLeaf.calls() <= 2);
    }

    /** Set 内嵌普通 List 的 canonicalization 不得调用 size/get。 */
    @Test
    void canonicalNestedListUsesIteratorOnly() {
        Set<Object> left = identitySet(
                new IteratorOnlyList(Arrays.<Object>asList("a", "b", "c")));
        Set<Object> right = identitySet(
                new IteratorOnlyList(Arrays.<Object>asList("a", "b", "c")));

        assertTrue(assertDoesNotThrow(() -> ArtifactSnapshots.controlledEquals(
                left,
                right,
                new ArtifactSnapshots.ComparisonLimits(
                        16,
                        64,
                        64,
                        64))));
    }

    /** Map canonicalization 不得读取 entrySet.size() 或预分配该容量。 */
    @Test
    void canonicalMapDoesNotReadEntrySetSize() {
        Map<Object, Object> left = new IteratorOnlyMap("k", "v");
        Map<Object, Object> right = new IteratorOnlyMap("k", "v");

        assertTrue(assertDoesNotThrow(() -> ArtifactSnapshots.controlledEquals(
                left,
                right,
                new ArtifactSnapshots.ComparisonLimits(
                        16,
                        64,
                        64,
                        64))));
    }

    /** Iterator 自身业务异常必须原样传播，不得伪装成 ComparisonLimitException。 */
    @Test
    void iteratorBusinessFailureIsNotRemapped() {
        Set<Object> left = new FailingIteratorSet();
        Set<Object> right = new FailingIteratorSet();

        MarkerFailure failure = assertThrows(
                MarkerFailure.class,
                () -> ArtifactSnapshots.controlledEquals(
                        left,
                        right,
                        new ArtifactSnapshots.ComparisonLimits(
                                16,
                                64,
                                64,
                                64)));

        assertEquals("iterator-business-failure", failure.getMessage());
    }

    /** 宽 Set 在第 maxEdges+1 个元素读取前稳定拒绝。 */
    @Test
    void wideSetStopsBeforeNextBeyondBudget() {
        CountingSet left = new CountingSet(100);
        CountingSet right = new CountingSet(100);

        ArtifactSnapshots.ComparisonLimitException failure = assertThrows(
                ArtifactSnapshots.ComparisonLimitException.class,
                () -> ArtifactSnapshots.controlledEquals(
                        left,
                        right,
                        new ArtifactSnapshots.ComparisonLimits(
                                16,
                                64,
                                4,
                                64)));

        assertTrue(failure.getMessage().contains("traversed-edges"));
        assertEquals(4, left.nextCalls());
        assertEquals(0, right.nextCalls());
    }

    /** Map 一条 entry 消耗两条 edge，第二条 entry 不得在预算后读取。 */
    @Test
    void mapStopsBeforeSecondEntryRead() {
        CountingMap left = new CountingMap(100);
        CountingMap right = new CountingMap(100);

        ArtifactSnapshots.ComparisonLimitException failure = assertThrows(
                ArtifactSnapshots.ComparisonLimitException.class,
                () -> ArtifactSnapshots.controlledEquals(
                        left,
                        right,
                        new ArtifactSnapshots.ComparisonLimits(
                                16,
                                64,
                                2,
                                64)));

        assertTrue(failure.getMessage().contains("traversed-edges"));
        assertEquals(1, left.nextCalls());
        assertEquals(0, right.nextCalls());
    }

    /** 循环普通 List 必须稳定拒绝，不得递归耗尽 JVM 栈。 */
    @Test
    void cyclicExternalListsAreRejectedIteratively() {
        List<Object> left = new ArrayList<Object>();
        List<Object> right = new ArrayList<Object>();
        left.add(left);
        right.add(right);

        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> ArtifactSnapshots.controlledEquals(
                        left,
                        right,
                        new ArtifactSnapshots.ComparisonLimits(
                                16,
                                64,
                                64,
                                64)));

        assertTrue(failure.getMessage().contains("cyclic"));
    }

    /** 构造 identity Set，允许测试自定义外部容器而不触发其 hash。 */
    private static Set<Object> identitySet(Object value) {
        Set<Object> set = Collections.newSetFromMap(
                new IdentityHashMap<Object, Boolean>());
        set.add(value);
        return set;
    }

    /** 构造每层两次引用同一 child 的共享 DAG。 */
    private static Object sharedDag(int depth, Object leaf) {
        Object value = leaf;
        for (int index = 0; index < depth; index++) {
            value = Arrays.<Object>asList(value, value);
        }
        return value;
    }

    /** 只允许 iterator 访问的普通 List。 */
    private static final class IteratorOnlyList extends AbstractList<Object> {
        private final List<Object> values;

        private IteratorOnlyList(List<Object> values) {
            this.values = values;
        }

        @Override
        public Object get(int index) {
            throw new AssertionError("canonicalization 不得调用 get(index)");
        }

        @Override
        public int size() {
            throw new AssertionError("canonicalization 不得调用 size()");
        }

        @Override
        public Iterator<Object> iterator() {
            return values.iterator();
        }
    }

    /** entrySet 仅允许 iterator 访问的普通 Map。 */
    private static final class IteratorOnlyMap
            extends AbstractMap<Object, Object> {
        private final Object key;
        private final Object value;

        private IteratorOnlyMap(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            return new AbstractSet<Map.Entry<Object, Object>>() {
                @Override
                public Iterator<Map.Entry<Object, Object>> iterator() {
                    return Collections.<Map.Entry<Object, Object>>singletonList(
                            new SimpleImmutableEntry<Object, Object>(key, value))
                            .iterator();
                }

                @Override
                public int size() {
                    throw new AssertionError("entrySet.size() 不得被读取");
                }
            };
        }
    }

    /** iterator.next() 主动抛出稳定业务异常。 */
    private static final class FailingIteratorSet extends AbstractSet<Object> {
        @Override
        public Iterator<Object> iterator() {
            return new Iterator<Object>() {
                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Object next() {
                    throw new MarkerFailure("iterator-business-failure");
                }
            };
        }

        @Override
        public int size() {
            return 1;
        }
    }

    /** 用于确认业务异常未被资源异常覆盖。 */
    private static final class MarkerFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private MarkerFailure(String message) {
            super(message);
        }
    }

    /** 统计真实 next() 调用次数的宽 Set。 */
    private static final class CountingSet extends AbstractSet<Object> {
        private final int count;
        private final AtomicInteger nextCalls = new AtomicInteger();

        private CountingSet(int count) {
            this.count = count;
        }

        @Override
        public Iterator<Object> iterator() {
            return new Iterator<Object>() {
                private int index;

                @Override
                public boolean hasNext() {
                    return index < count;
                }

                @Override
                public Object next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    nextCalls.incrementAndGet();
                    return "s-" + index++;
                }
            };
        }

        @Override
        public int size() {
            return count;
        }

        private int nextCalls() {
            return nextCalls.get();
        }
    }

    /** 统计 entry iterator 读取次数的宽 Map。 */
    private static final class CountingMap extends AbstractMap<Object, Object> {
        private final int count;
        private final AtomicInteger nextCalls = new AtomicInteger();

        private CountingMap(int count) {
            this.count = count;
        }

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            return new AbstractSet<Map.Entry<Object, Object>>() {
                @Override
                public Iterator<Map.Entry<Object, Object>> iterator() {
                    return new Iterator<Map.Entry<Object, Object>>() {
                        private int index;

                        @Override
                        public boolean hasNext() {
                            return index < count;
                        }

                        @Override
                        public Map.Entry<Object, Object> next() {
                            if (!hasNext()) {
                                throw new NoSuchElementException();
                            }
                            nextCalls.incrementAndGet();
                            int current = index++;
                            return new SimpleImmutableEntry<Object, Object>(
                                    "k-" + current,
                                    "v-" + current);
                        }
                    };
                }

                @Override
                public int size() {
                    return count;
                }
            };
        }

        private int nextCalls() {
            return nextCalls.get();
        }
    }

    /** 固定 hash 并统计 equals，用于 operation cache 复核。 */
    private static final class CountingLeaf implements ImmutablePipelineArtifact {
        private static final AtomicInteger CALLS = new AtomicInteger();
        private static volatile int guard;
        private final String value;

        private CountingLeaf(String value) {
            this.value = value;
        }

        private static void reset(int newGuard) {
            CALLS.set(0);
            guard = newGuard;
        }

        private static int calls() {
            return CALLS.get();
        }

        @Override
        public int hashCode() {
            return 41;
        }

        @Override
        public boolean equals(Object other) {
            int current = CALLS.incrementAndGet();
            if (current > guard) {
                throw new IllegalStateException(
                        "independent review detected repeated pair work");
            }
            return other instanceof CountingLeaf
                    && value.equals(((CountingLeaf) other).value);
        }
    }
}
