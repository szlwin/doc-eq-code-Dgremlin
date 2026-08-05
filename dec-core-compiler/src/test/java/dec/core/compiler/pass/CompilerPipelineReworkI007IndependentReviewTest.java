package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 / I007 独立 Review：复核 collision 异常边界与正常容器兼容性。
 */
class CompilerPipelineReworkI007IndependentReviewTest {

    /** Map duplicate canonical key 必须使用稳定内部异常和固定消息。 */
    @Test
    void mapCollisionUsesStableExceptionAndMessage() {
        ArtifactSnapshots.CanonicalCollisionException failure = assertThrows(
                ArtifactSnapshots.CanonicalCollisionException.class,
                () -> controlledEquals(
                        collidingIdentityMap("v1", "v2"),
                        collidingIdentityMap("v1", "v2")));

        assertEquals(
                "artifact comparison canonical collision: map-key",
                failure.getMessage());
    }

    /** Set duplicate canonical element 必须使用稳定内部异常和固定消息。 */
    @Test
    void setCollisionUsesStableExceptionAndMessage() {
        ArtifactSnapshots.CanonicalCollisionException failure = assertThrows(
                ArtifactSnapshots.CanonicalCollisionException.class,
                () -> controlledEquals(
                        collidingIdentitySet(),
                        collidingIdentitySet()));

        assertEquals(
                "artifact comparison canonical collision: set-element",
                failure.getMessage());
    }

    /** FrozenMap 与同 size/hash 的非法外部 Map 比较时仍必须 fail-closed。 */
    @Test
    void frozenMapRejectsSameHashInvalidExternalMap() {
        Map<Object, Object> valid = new LinkedHashMap<Object, Object>();
        valid.put(new HashCollisionKey("left"), "v1");
        valid.put(new HashCollisionKey("right"), "v2");
        Map<?, ?> frozen = (Map<?, ?>) ArtifactSnapshots.freeze(valid);
        Map<Object, Object> invalid = collidingIdentityMap("v1", "v2");

        ArtifactSnapshots.CanonicalCollisionException failure = assertThrows(
                ArtifactSnapshots.CanonicalCollisionException.class,
                () -> frozen.equals(invalid));

        assertEquals(
                "artifact comparison canonical collision: map-key",
                failure.getMessage());
    }

    /** FrozenSet 与同 size/hash 的非法外部 Set 比较时仍必须 fail-closed。 */
    @Test
    void frozenSetRejectsSameHashInvalidExternalSet() {
        Set<Object> valid = new LinkedHashSet<Object>();
        valid.add(new HashCollisionKey("left"));
        valid.add(new HashCollisionKey("right"));
        Set<?> frozen = (Set<?>) ArtifactSnapshots.freeze(valid);
        Set<Object> invalid = collidingIdentitySet();

        ArtifactSnapshots.CanonicalCollisionException failure = assertThrows(
                ArtifactSnapshots.CanonicalCollisionException.class,
                () -> frozen.equals(invalid));

        assertEquals(
                "artifact comparison canonical collision: set-element",
                failure.getMessage());
    }

    /** 单个 Map.Entry 的相同 key 不属于容器 duplicate-key collision。 */
    @Test
    void mapEntryComparisonDoesNotApplyContainerCollisionGate() {
        Map.Entry<Object, Object> left =
                new AbstractMap.SimpleImmutableEntry<Object, Object>(
                        new EqualKey("same"),
                        "value");
        Map.Entry<Object, Object> right =
                new AbstractMap.SimpleImmutableEntry<Object, Object>(
                        new EqualKey("same"),
                        "value");

        assertTrue(controlledEquals(left, right));
    }

    /** 嵌套 Map collision 必须保留内层 map-key 失败原因。 */
    @Test
    void nestedMapCollisionKeepsInnerFailureReason() {
        Set<Object> left = identitySet(collidingIdentityMap("v1", "v2"));
        Set<Object> right = identitySet(collidingIdentityMap("v1", "v2"));

        ArtifactSnapshots.CanonicalCollisionException failure = assertThrows(
                ArtifactSnapshots.CanonicalCollisionException.class,
                () -> controlledEquals(left, right));

        assertEquals(
                "artifact comparison canonical collision: map-key",
                failure.getMessage());
    }

    /** 普通 hash collision 但 equals 不同的 Map key 仍必须精确区分。 */
    @Test
    void ordinaryMapHashCollisionIsNotCanonicalCollision() {
        Map<Object, Object> left = new LinkedHashMap<Object, Object>();
        left.put(new HashCollisionKey("left"), "value");
        Map<Object, Object> right = new LinkedHashMap<Object, Object>();
        right.put(new HashCollisionKey("right"), "value");

        assertFalse(controlledEquals(left, right));
    }

    /** 标准 Map 会折叠 equality-equal key，折叠后的正常结构保持相等。 */
    @Test
    void standardMapCollapsedEqualKeysRemainValid() {
        Map<Object, Object> left = new LinkedHashMap<Object, Object>();
        left.put(new EqualKey("same"), "old");
        left.put(new EqualKey("same"), "new");
        Map<Object, Object> right = new LinkedHashMap<Object, Object>();
        right.put(new EqualKey("same"), "new");

        assertEquals(1, left.size());
        assertTrue(controlledEquals(left, right));
    }

    /** 空与单元素 Set/Map 不得被 duplicate 扫描误拒绝。 */
    @Test
    void emptyAndSingletonContainersRemainValid() {
        assertTrue(controlledEquals(
                Collections.emptySet(),
                Collections.emptySet()));
        assertTrue(controlledEquals(
                Collections.singleton("value"),
                Collections.singleton("value")));
        assertTrue(controlledEquals(
                Collections.emptyMap(),
                Collections.emptyMap()));
        assertTrue(controlledEquals(
                Collections.singletonMap("key", "value"),
                Collections.singletonMap("key", "value")));
    }

    /** collision 异常终止一个 operation 后，不得污染后续独立正常比较。 */
    @Test
    void collisionDoesNotPoisonLaterOperation() {
        assertThrows(
                ArtifactSnapshots.CanonicalCollisionException.class,
                () -> controlledEquals(
                        collidingIdentityMap("v1", "v2"),
                        collidingIdentityMap("v1", "v2")));

        Map<Object, Object> left = new LinkedHashMap<Object, Object>();
        left.put("key", "value");
        Map<Object, Object> right = new LinkedHashMap<Object, Object>();
        right.put("key", "value");
        assertTrue(controlledEquals(left, right));
    }

    /** 使用固定测试预算执行 package-private comparison。 */
    private static boolean controlledEquals(Object left, Object right) {
        return ArtifactSnapshots.controlledEquals(
                left,
                right,
                new ArtifactSnapshots.ComparisonLimits(
                        32,
                        256,
                        1024,
                        256));
    }

    /** 构造 equality-equal、identity-distinct key 的非法 identity Map。 */
    private static Map<Object, Object> collidingIdentityMap(
            Object firstValue,
            Object secondValue) {
        Map<Object, Object> values = new IdentityHashMap<Object, Object>();
        values.put(new EqualKey("same"), firstValue);
        values.put(new EqualKey("same"), secondValue);
        return values;
    }

    /** 构造 equality-equal、identity-distinct element 的非法 identity Set。 */
    private static Set<Object> collidingIdentitySet() {
        Set<Object> values = Collections.newSetFromMap(
                new IdentityHashMap<Object, Boolean>());
        values.add(new EqualKey("same"));
        values.add(new EqualKey("same"));
        return values;
    }

    /** 构造单元素 identity Set，用于验证嵌套 collision。 */
    private static Set<Object> identitySet(Object value) {
        Set<Object> values = Collections.newSetFromMap(
                new IdentityHashMap<Object, Boolean>());
        values.add(value);
        return values;
    }

    /** equals 相同且 hash 固定的非法 collision key。 */
    private static final class EqualKey {
        private final String value;

        private EqualKey(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return 7;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof EqualKey
                    && value.equals(((EqualKey) other).value);
        }
    }

    /** hash 相同但 equals 按 value 区分的合法不可变 key。 */
    private static final class HashCollisionKey
            implements ImmutablePipelineArtifact {
        private final String value;

        private HashCollisionKey(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return 7;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof HashCollisionKey
                    && value.equals(((HashCollisionKey) other).value);
        }
    }
}
