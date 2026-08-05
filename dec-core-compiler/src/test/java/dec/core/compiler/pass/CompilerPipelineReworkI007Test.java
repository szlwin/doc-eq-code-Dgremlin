package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T12 / I007 RED：阻断 duplicate canonical key/element 形成合法节点。
 */
class CompilerPipelineReworkI007Test {

    /** 两个具有相同非法 duplicate-key 结构的 IdentityHashMap 也必须 fail-closed。 */
    @Test
    void identicalInvalidIdentityMapsAreRejected() {
        Map<Object, Object> left = collidingIdentityMap("v1", "v2");
        Map<Object, Object> right = collidingIdentityMap("v1", "v2");

        assertThrows(
                IllegalArgumentException.class,
                () -> controlledEquals(left, right));
    }

    /** 单侧存在 duplicate canonical key 时不得只返回普通 NOT_EQUAL。 */
    @Test
    void oneInvalidIdentityMapIsRejectedBeforeComparisonCompletes() {
        Map<Object, Object> invalid = collidingIdentityMap("v1", "v2");
        Map<Object, Object> normal = new LinkedHashMap<Object, Object>();
        normal.put(new EqualKey("same"), "v1");

        assertThrows(
                IllegalArgumentException.class,
                () -> controlledEquals(invalid, normal));
    }

    /** identity-backed Set 中 equality-equal distinct element 必须稳定拒绝。 */
    @Test
    void duplicateCanonicalSetElementIsRejected() {
        Set<Object> left = collidingIdentitySet();
        Set<Object> right = collidingIdentitySet();

        assertThrows(
                IllegalArgumentException.class,
                () -> controlledEquals(left, right));
    }

    /** collision 嵌套在普通容器中时也不得形成可复用的合法 canonical node。 */
    @Test
    void nestedCanonicalCollisionIsRejected() {
        Set<Object> left = identitySet(collidingIdentityMap("v1", "v2"));
        Set<Object> right = identitySet(collidingIdentityMap("v1", "v2"));

        assertThrows(
                IllegalArgumentException.class,
                () -> controlledEquals(left, right));
    }

    /** 正常 LinkedHashMap 的精确相等语义保持。 */
    @Test
    void normalLinkedHashMapsRemainEqual() {
        Map<Object, Object> left = new LinkedHashMap<Object, Object>();
        left.put("a", "v1");
        left.put("b", "v2");
        Map<Object, Object> right = new LinkedHashMap<Object, Object>();
        right.put("a", "v1");
        right.put("b", "v2");

        assertTrue(controlledEquals(left, right));
    }

    /** hashCode 相同但 equals 不同的 key 不属于 canonical duplicate。 */
    @Test
    void ordinaryHashCollisionRemainsPreciselyDifferent() {
        Map<Object, Object> left = new LinkedHashMap<Object, Object>();
        left.put(new HashCollisionKey("left"), "value");
        Map<Object, Object> right = new LinkedHashMap<Object, Object>();
        right.put(new HashCollisionKey("right"), "value");

        assertFalse(controlledEquals(left, right));
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

    /** 构造包含两个 equality-equal、identity-distinct key 的非法 Map。 */
    private static Map<Object, Object> collidingIdentityMap(
            Object firstValue,
            Object secondValue) {
        Map<Object, Object> values = new IdentityHashMap<Object, Object>();
        values.put(new EqualKey("same"), firstValue);
        values.put(new EqualKey("same"), secondValue);
        return values;
    }

    /** 构造包含两个 equality-equal、identity-distinct element 的非法 Set。 */
    private static Set<Object> collidingIdentitySet() {
        Set<Object> values = Collections.newSetFromMap(
                new IdentityHashMap<Object, Boolean>());
        values.add(new EqualKey("same"));
        values.add(new EqualKey("same"));
        return values;
    }

    /** 构造单元素 identity-backed Set，用于嵌套 collision。 */
    private static Set<Object> identitySet(Object value) {
        Set<Object> values = Collections.newSetFromMap(
                new IdentityHashMap<Object, Boolean>());
        values.add(value);
        return values;
    }

    /** equals 相同但 identity 不同的 key/element。 */
    private static final class EqualKey {
        private final String value;

        private EqualKey(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof EqualKey
                    && value.equals(((EqualKey) other).value);
        }
    }

    /** hashCode 固定但 equals 按 value 精确区分。 */
    private static final class HashCollisionKey {
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
