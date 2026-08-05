package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 / I006 RED：公开比较必须在外部物化前生效预算，并在候选间共享缓存。
 */
class CompilerPipelineReworkI006Test {

    /** 外部嵌套 Set 必须在读取超出 edge 上限前拒绝，禁止整体复制。 */
    @Test
    void nestedExternalSetStopsBeforeBulkCopy() {
        Set<Object> left = singletonIdentitySet(
                new GuardedSet(4, 32, 6, "set"));
        Set<Object> right = singletonIdentitySet(
                new GuardedSet(4, 32, 6, "set"));

        ArtifactSnapshots.ComparisonLimitException failure = assertThrows(
                ArtifactSnapshots.ComparisonLimitException.class,
                () -> ArtifactSnapshots.controlledEquals(
                        left,
                        right,
                        new ArtifactSnapshots.ComparisonLimits(
                                32,
                                64,
                                3,
                                64)));

        assertTrue(failure.getMessage().contains("traversed-edges"));
    }

    /** 外部嵌套 Map 必须增量读取，禁止在预算前复制全部 entry。 */
    @Test
    void nestedExternalMapStopsBeforeBulkCopy() {
        Set<Object> left = singletonIdentitySet(
                new GuardedMap(2, 32, 6));
        Set<Object> right = singletonIdentitySet(
                new GuardedMap(2, 32, 6));

        ArtifactSnapshots.ComparisonLimitException failure = assertThrows(
                ArtifactSnapshots.ComparisonLimitException.class,
                () -> ArtifactSnapshots.controlledEquals(
                        left,
                        right,
                        new ArtifactSnapshots.ComparisonLimits(
                                32,
                                64,
                                4,
                                64)));

        assertTrue(failure.getMessage().contains("traversed-edges"));
    }

    /** List equality 不得调用外部 List.size()，也不得据此预分配。 */
    @Test
    void externalListSizeIsNotTrusted() {
        Object frozen = ArtifactSnapshots.freeze(Arrays.<Object>asList(
                "a",
                Arrays.asList("b", "c")));
        List<Object> external = new ThrowingSizeList(Arrays.<Object>asList(
                "a",
                Arrays.asList("b", "c")));

        assertTrue(assertDoesNotThrow(() -> frozen.equals(external)));
    }

    /** 非 RandomAccess List 必须通过 Iterator 线性读取，禁止 get(index)。 */
    @Test
    void sequentialListEqualityDoesNotUseIndexedGet() {
        List<Object> source = new ArrayList<Object>();
        for (int index = 0; index < 256; index++) {
            source.add("v-" + index);
        }
        Object frozen = ArtifactSnapshots.freeze(source);
        GetForbiddenSequentialList external =
                new GetForbiddenSequentialList(source);

        assertTrue(assertDoesNotThrow(() -> frozen.equals(external)));
        assertEquals(256, external.nextCalls());
        assertEquals(0, external.getCalls());
    }

    /** 多个候选引用同一 DAG 时，NOT_EQUAL pair 必须在一次 contains 中复用。 */
    @Test
    void listContainsReusesNotEqualPairAcrossCandidates() {
        Object stored = sharedDag(18, new CountingLeaf("stored"));
        List<Object> source = new ArrayList<Object>();
        for (int index = 0; index < 12; index++) {
            source.add(stored);
        }
        List<?> frozen = (List<?>) ArtifactSnapshots.freeze(source);
        Object query = ArtifactSnapshots.freeze(
                sharedDag(18, new CountingLeaf("missing")));
        CountingLeaf.reset(3);

        boolean present = assertDoesNotThrow(() -> frozen.contains(query));

        assertFalse(present);
        assertTrue(CountingLeaf.calls() <= 2,
                "同一 NOT_EQUAL pair 不得按候选数重复比较");
    }

    /** 多个 Map value 共享同一 DAG 时，containsValue 必须复用 operation cache。 */
    @Test
    void mapContainsValueReusesNotEqualPairAcrossEntries() {
        Object stored = sharedDag(16, new CountingLeaf("value"));
        Map<Object, Object> source = new LinkedHashMap<Object, Object>();
        for (int index = 0; index < 12; index++) {
            source.put("k-" + index, stored);
        }
        Map<?, ?> frozen = (Map<?, ?>) ArtifactSnapshots.freeze(source);
        Object query = ArtifactSnapshots.freeze(
                sharedDag(16, new CountingLeaf("other")));
        CountingLeaf.reset(3);

        boolean present = assertDoesNotThrow(
                () -> frozen.containsValue(query));

        assertFalse(present);
        assertTrue(CountingLeaf.calls() <= 2,
                "containsValue 必须共享已完成子 pair");
    }

    /** 无限 iterator 必须在预算边界前停止，不得依赖超时或 OOM。 */
    @Test
    void infiniteIteratorFailsAtEdgeBudget() {
        Set<Object> left = singletonIdentitySet(new InfiniteSet(10));
        Set<Object> right = singletonIdentitySet(new InfiniteSet(10));

        ArtifactSnapshots.ComparisonLimitException failure = assertThrows(
                ArtifactSnapshots.ComparisonLimitException.class,
                () -> ArtifactSnapshots.controlledEquals(
                        left,
                        right,
                        new ArtifactSnapshots.ComparisonLimits(
                                32,
                                64,
                                5,
                                64)));

        assertTrue(failure.getMessage().contains("traversed-edges"));
    }

    /** 小规模 RandomAccess List 仍保持既有精确 equality 合同。 */
    @Test
    void ordinaryListControlRemainsGreen() {
        Object left = ArtifactSnapshots.freeze(Arrays.<Object>asList(
                "a",
                Arrays.asList("b", "c")));
        Object right = ArtifactSnapshots.freeze(Arrays.<Object>asList(
                "a",
                Arrays.asList("b", "c")));

        assertTrue(left.equals(right));
        assertEquals(left.hashCode(), right.hashCode());
    }

    /** 构造只包含一个元素、按 identity 保存的 Set。 */
    private static Set<Object> singletonIdentitySet(Object value) {
        Set<Object> result = Collections.newSetFromMap(
                new java.util.IdentityHashMap<Object, Boolean>());
        result.add(value);
        return result;
    }

    /** 构造每层两次引用同一子图的共享 DAG。 */
    private static Object sharedDag(int depth, Object leaf) {
        Object value = leaf;
        for (int index = 0; index < depth; index++) {
            value = Arrays.<Object>asList(value, value);
        }
        return value;
    }

    /** size() 不可信但 iterator 正常的外部 List。 */
    private static final class ThrowingSizeList extends AbstractList<Object> {
        private final List<Object> values;

        private ThrowingSizeList(List<Object> values) {
            this.values = values;
        }

        @Override
        public Object get(int index) {
            throw new AssertionError("外部 List.get(index) 不得被调用");
        }

        @Override
        public int size() {
            throw new AssertionError("外部 List.size() 不得被调用");
        }

        @Override
        public Iterator<Object> iterator() {
            return values.iterator();
        }
    }

    /** 只允许 ListIterator 顺序访问的非 RandomAccess List。 */
    private static final class GetForbiddenSequentialList
            extends java.util.AbstractSequentialList<Object> {
        private final List<Object> values;
        private final AtomicInteger nextCalls = new AtomicInteger();
        private final AtomicInteger getCalls = new AtomicInteger();

        private GetForbiddenSequentialList(List<Object> values) {
            this.values = new ArrayList<Object>(values);
        }

        @Override
        public ListIterator<Object> listIterator(int index) {
            final ListIterator<Object> delegate = values.listIterator(index);
            return new ListIterator<Object>() {
                @Override
                public boolean hasNext() {
                    return delegate.hasNext();
                }

                @Override
                public Object next() {
                    nextCalls.incrementAndGet();
                    return delegate.next();
                }

                @Override
                public boolean hasPrevious() {
                    return delegate.hasPrevious();
                }

                @Override
                public Object previous() {
                    return delegate.previous();
                }

                @Override
                public int nextIndex() {
                    return delegate.nextIndex();
                }

                @Override
                public int previousIndex() {
                    return delegate.previousIndex();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void set(Object value) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void add(Object value) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public Object get(int index) {
            getCalls.incrementAndGet();
            throw new AssertionError("非 RandomAccess List 不得使用 get(index)");
        }

        private int nextCalls() {
            return nextCalls.get();
        }

        private int getCalls() {
            return getCalls.get();
        }
    }

    /** 有限 size 但 iterator 产出更多元素，用于发现整体复制。 */
    private static final class GuardedSet extends AbstractSet<Object> {
        private final int declaredSize;
        private final int produced;
        private final int guard;
        private final String prefix;

        private GuardedSet(
                int declaredSize,
                int produced,
                int guard,
                String prefix) {
            this.declaredSize = declaredSize;
            this.produced = produced;
            this.guard = guard;
            this.prefix = prefix;
        }

        @Override
        public Iterator<Object> iterator() {
            return new GuardedIterator(produced, guard, prefix);
        }

        @Override
        public int size() {
            return declaredSize;
        }
    }

    /** entrySet 在复制时持续产出 entry，用于发现 Map bulk materialization。 */
    private static final class GuardedMap extends AbstractMap<Object, Object> {
        private final int declaredSize;
        private final int produced;
        private final int guard;

        private GuardedMap(int declaredSize, int produced, int guard) {
            this.declaredSize = declaredSize;
            this.produced = produced;
            this.guard = guard;
        }

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            return new AbstractSet<Map.Entry<Object, Object>>() {
                @Override
                public Iterator<Map.Entry<Object, Object>> iterator() {
                    final Iterator<Object> keys =
                            new GuardedIterator(produced, guard, "map");
                    return new Iterator<Map.Entry<Object, Object>>() {
                        @Override
                        public boolean hasNext() {
                            return keys.hasNext();
                        }

                        @Override
                        public Map.Entry<Object, Object> next() {
                            Object key = keys.next();
                            return new SimpleImmutableEntry<Object, Object>(
                                    key,
                                    "value");
                        }
                    };
                }

                @Override
                public int size() {
                    return declaredSize;
                }
            };
        }
    }

    /** 超过 guard 次 next() 时快速失败，避免 RED 长时间运行。 */
    private static final class GuardedIterator implements Iterator<Object> {
        private final int produced;
        private final int guard;
        private final String prefix;
        private int index;

        private GuardedIterator(int produced, int guard, String prefix) {
            this.produced = produced;
            this.guard = guard;
            this.prefix = prefix;
        }

        @Override
        public boolean hasNext() {
            return index < produced;
        }

        @Override
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (index >= guard) {
                throw new IllegalStateException(
                        "comparison budget did not stop iterator: " + prefix);
            }
            return prefix + "-" + index++;
        }
    }

    /** 持续产出元素，但设置快速保护防止旧实现无限复制。 */
    private static final class InfiniteSet extends AbstractSet<Object> {
        private final int guard;

        private InfiniteSet(int guard) {
            this.guard = guard;
        }

        @Override
        public Iterator<Object> iterator() {
            return new Iterator<Object>() {
                private int index;

                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Object next() {
                    if (index >= guard) {
                        throw new IllegalStateException(
                                "comparison budget did not stop infinite iterator");
                    }
                    return "infinite-" + index++;
                }
            };
        }

        @Override
        public int size() {
            return 1;
        }
    }

    /** 固定 hash 并统计 equals，用于检测跨候选 pair cache。 */
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
            return 37;
        }

        @Override
        public boolean equals(Object other) {
            int current = CALLS.incrementAndGet();
            if (current > guard) {
                throw new IllegalStateException(
                        "同一共享 pair 被重复展开");
            }
            return other instanceof CountingLeaf
                    && value.equals(((CountingLeaf) other).value);
        }
    }
}
