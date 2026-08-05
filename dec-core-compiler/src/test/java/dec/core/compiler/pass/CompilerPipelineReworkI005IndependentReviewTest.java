package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractList;
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
 * TASK-P1-T12 / I005 独立 Review：公开 equality/query、预算与 Collection 合同。
 */
class CompilerPipelineReworkI005IndependentReviewTest {

    /** 两个独立 Session 的共享 DAG Set 必须可线性 contains 和 equals。 */
    @Test
    void setContainsAndEqualsAcrossIndependentSessions() {
        ReviewLeaf.reset();
        Set<Object> leftSource = identitySet(sharedDag(24, new ReviewLeaf("set")));
        Set<Object> rightSource = identitySet(sharedDag(24, new ReviewLeaf("set")));
        Set<?> left = (Set<?>) ArtifactSnapshots.freeze(leftSource);
        Set<?> right = (Set<?>) ArtifactSnapshots.freeze(rightSource);

        assertTrue(left.contains(right.iterator().next()));
        assertTrue(left.equals(right));
        assertTrue(right.equals(left));
        assertEquals(left.hashCode(), right.hashCode());
        assertTrue(ReviewLeaf.calls() <= 8,
                "shared leaf pairs must not be expanded repeatedly");
    }

    /** Map equality、containsKey 和 containsValue 必须共用受控图比较语义。 */
    @Test
    void mapQueriesAndEqualsAcrossIndependentSessions() {
        ReviewLeaf.reset();
        Map<Object, Object> leftSource = new IdentityHashMap<Object, Object>();
        leftSource.put(
                sharedDag(20, new ReviewLeaf("key")),
                sharedDag(20, new ReviewLeaf("value")));
        Map<Object, Object> rightSource = new IdentityHashMap<Object, Object>();
        rightSource.put(
                sharedDag(20, new ReviewLeaf("key")),
                sharedDag(20, new ReviewLeaf("value")));
        Map<?, ?> left = (Map<?, ?>) ArtifactSnapshots.freeze(leftSource);
        Map<?, ?> right = (Map<?, ?>) ArtifactSnapshots.freeze(rightSource);
        Map.Entry<?, ?> query = right.entrySet().iterator().next();

        assertTrue(left.containsKey(query.getKey()));
        assertTrue(left.containsValue(query.getValue()));
        assertTrue(left.equals(right));
        assertTrue(right.equals(left));
        assertEquals(left.hashCode(), right.hashCode());
        assertTrue(ReviewLeaf.calls() <= 16,
                "map query leaf pairs must remain near unique graph size");
    }

    /** Frozen Entry 与普通 Entry 必须保持对称 equality 和标准 hash。 */
    @Test
    void frozenEntryIsSymmetricAndHashCompatible() {
        Map<Object, Object> source = new LinkedHashMap<Object, Object>();
        source.put(Arrays.asList("key"), Arrays.asList("value"));
        Map<?, ?> frozen = (Map<?, ?>) ArtifactSnapshots.freeze(source);
        Map.Entry<?, ?> frozenEntry = frozen.entrySet().iterator().next();
        Map.Entry<Object, Object> ordinary =
                new AbstractMap.SimpleImmutableEntry<Object, Object>(
                        Arrays.asList("key"),
                        Arrays.asList("value"));

        assertTrue(frozenEntry.equals(ordinary));
        assertTrue(ordinary.equals(frozenEntry));
        assertEquals(ordinary.hashCode(), frozenEntry.hashCode());
        assertTrue(frozen.entrySet().contains(ordinary));
    }

    /** List equality 必须保持自反、对称、传递以及 null/type 安全。 */
    @Test
    void listEqualityIsReflexiveSymmetricAndTransitive() {
        Object first = ArtifactSnapshots.freeze(sharedDag(12, "leaf"));
        Object second = ArtifactSnapshots.freeze(sharedDag(12, "leaf"));
        Object third = ArtifactSnapshots.freeze(sharedDag(12, "leaf"));

        assertTrue(first.equals(first));
        assertTrue(first.equals(second));
        assertTrue(second.equals(first));
        assertTrue(second.equals(third));
        assertTrue(first.equals(third));
        assertFalse(first.equals(null));
        assertFalse(first.equals("not-a-list"));
        assertEquals(first.hashCode(), second.hashCode());
    }

    /** hash 相同但嵌套结构不同仍必须执行精确比较。 */
    @Test
    void nestedHashCollisionDoesNotProduceFalseEquality() {
        Object left = ArtifactSnapshots.freeze(Arrays.<Object>asList(
                Arrays.<Object>asList(new CollisionLeaf("left")),
                "tail"));
        Object right = ArtifactSnapshots.freeze(Arrays.<Object>asList(
                Arrays.<Object>asList(new CollisionLeaf("right")),
                "tail"));

        assertEquals(left.hashCode(), right.hashCode());
        assertFalse(left.equals(right));
    }

    /** canonical node 预算必须在公开 Set 比较的物化前稳定拒绝。 */
    @Test
    void canonicalNodeBudgetFailsClosed() {
        Set<Object> left = identitySet(sharedDag(4, "leaf"));
        Set<Object> right = identitySet(sharedDag(4, "leaf"));

        assertTrue(ArtifactSnapshots.controlledEquals(
                left,
                right,
                new ArtifactSnapshots.ComparisonLimits(16, 32, 64, 12)));
        ArtifactSnapshots.ComparisonLimitException failure = assertThrows(
                ArtifactSnapshots.ComparisonLimitException.class,
                () -> ArtifactSnapshots.controlledEquals(
                        left,
                        right,
                        new ArtifactSnapshots.ComparisonLimits(
                                16,
                                32,
                                64,
                                4)));
        assertTrue(failure.getMessage().contains("canonical-nodes"));
    }

    /** edge 预算必须在 Map key/value 继续展开前稳定拒绝。 */
    @Test
    void comparisonEdgeBudgetFailsClosed() {
        Map<Object, Object> left = new IdentityHashMap<Object, Object>();
        left.put(sharedDag(3, "key"), sharedDag(3, "value"));
        Map<Object, Object> right = new IdentityHashMap<Object, Object>();
        right.put(sharedDag(3, "key"), sharedDag(3, "value"));

        assertTrue(ArtifactSnapshots.controlledEquals(
                left,
                right,
                new ArtifactSnapshots.ComparisonLimits(16, 64, 64, 32)));
        ArtifactSnapshots.ComparisonLimitException failure = assertThrows(
                ArtifactSnapshots.ComparisonLimitException.class,
                () -> ArtifactSnapshots.controlledEquals(
                        left,
                        right,
                        new ArtifactSnapshots.ComparisonLimits(
                                16,
                                64,
                                2,
                                32)));
        assertTrue(failure.getMessage().contains("traversed-edges"));
    }

    /** Frozen receiver 不得调用普通外部 List 自身的 equals/hashCode。 */
    @Test
    void externalListQueryDoesNotInvokeContainerEqualityOrHash() {
        Object frozen = ArtifactSnapshots.freeze(Arrays.<Object>asList(
                "a",
                Arrays.asList("b", "c")));
        ThrowingContainerMethodsList external =
                new ThrowingContainerMethodsList();

        assertTrue(frozen.equals(external));
        assertTrue(((List<?>) frozen).contains(Arrays.asList("b", "c")));
    }

    /** 构造只包含一个元素、按 identity 保存的 Set。 */
    private static Set<Object> identitySet(Object value) {
        Set<Object> set = Collections.newSetFromMap(
                new IdentityHashMap<Object, Boolean>());
        set.add(value);
        return set;
    }

    /** 构造每层两次引用同一子图的共享 DAG。 */
    private static Object sharedDag(int depth, Object leaf) {
        Object value = leaf;
        for (int index = 0; index < depth; index++) {
            value = Arrays.<Object>asList(value, value);
        }
        return value;
    }

    /** 统计真实 leaf equality 调用。 */
    private static final class ReviewLeaf implements ImmutablePipelineArtifact {
        private static final AtomicInteger CALLS = new AtomicInteger();
        private final String value;

        private ReviewLeaf(String value) {
            this.value = value;
        }

        /** 重置调用计数。 */
        private static void reset() {
            CALLS.set(0);
        }

        /** 返回调用次数。 */
        private static int calls() {
            return CALLS.get();
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            CALLS.incrementAndGet();
            return other instanceof ReviewLeaf
                    && value.equals(((ReviewLeaf) other).value);
        }
    }

    /** 固定 hash、按 value 精确区分的 immutable leaf。 */
    private static final class CollisionLeaf implements ImmutablePipelineArtifact {
        private final String value;

        private CollisionLeaf(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return 23;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof CollisionLeaf
                    && value.equals(((CollisionLeaf) other).value);
        }
    }

    /** get/size 正常，但容器 equals/hashCode 一旦被调用就立即失败。 */
    private static final class ThrowingContainerMethodsList
            extends AbstractList<Object> {
        private final List<Object> values = Arrays.<Object>asList(
                "a",
                Arrays.asList("b", "c"));

        @Override
        public Object get(int index) {
            return values.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public boolean equals(Object other) {
            throw new AssertionError("external list equals must not be called");
        }

        @Override
        public int hashCode() {
            throw new AssertionError("external list hashCode must not be called");
        }
    }
}
