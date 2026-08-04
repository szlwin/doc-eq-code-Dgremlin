package dec.core.compiler.pass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 对跨 Result 边界暴露的 artifact 值执行受控不可变快照。
 */
final class ArtifactSnapshots {
    /** 工具类不允许实例化。 */
    private ArtifactSnapshots() {
    }

    /**
     * 递归复制受支持的不可变值和容器，拒绝未知可变对象及循环引用。
     */
    static Object freeze(Object value) {
        return freeze(
                Objects.requireNonNull(value, "value"),
                new IdentityHashMap<Object, Boolean>());
    }

    /**
     * 使用 identity 路径集合检测当前递归栈中的循环容器。
     */
    private static Object freeze(
            Object checked,
            IdentityHashMap<Object, Boolean> activePath) {
        if (isImmutableScalar(checked) || checked instanceof ImmutablePipelineArtifact) {
            return checked;
        }
        if (checked instanceof Optional<?>) {
            enterContainer(checked, activePath);
            try {
                Optional<?> optional = (Optional<?>) checked;
                return optional.isPresent()
                        ? Optional.of(freeze(optional.get(), activePath))
                        : Optional.empty();
            } finally {
                activePath.remove(checked);
            }
        }
        if (checked instanceof List<?>) {
            enterContainer(checked, activePath);
            try {
                List<Object> copy = new ArrayList<Object>();
                for (Object item : (List<?>) checked) {
                    copy.add(freeze(
                            Objects.requireNonNull(item, "artifact list item"),
                            activePath));
                }
                return Collections.unmodifiableList(copy);
            } finally {
                activePath.remove(checked);
            }
        }
        if (checked instanceof Set<?>) {
            enterContainer(checked, activePath);
            try {
                Set<Object> copy = new LinkedHashSet<Object>();
                for (Object item : (Set<?>) checked) {
                    copy.add(freeze(
                            Objects.requireNonNull(item, "artifact set item"),
                            activePath));
                }
                return Collections.unmodifiableSet(copy);
            } finally {
                activePath.remove(checked);
            }
        }
        if (checked instanceof Map<?, ?>) {
            enterContainer(checked, activePath);
            try {
                Map<Object, Object> copy = new LinkedHashMap<Object, Object>();
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) checked).entrySet()) {
                    Object key = freeze(
                            Objects.requireNonNull(entry.getKey(), "artifact map key"),
                            activePath);
                    Object value = freeze(
                            Objects.requireNonNull(entry.getValue(), "artifact map value"),
                            activePath);
                    copy.put(key, value);
                }
                return Collections.unmodifiableMap(copy);
            } finally {
                activePath.remove(checked);
            }
        }
        throw new IllegalArgumentException(
                "artifact value must be immutable or a supported container: "
                        + checked.getClass().getName());
    }

    /** 将容器加入当前递归路径；重复出现表示循环引用。 */
    private static void enterContainer(
            Object container,
            IdentityHashMap<Object, Boolean> activePath) {
        if (activePath.put(container, Boolean.TRUE) != null) {
            throw new IllegalArgumentException("artifact graph must not be cyclic");
        }
    }

    /** 判断值是否属于明确不可变的标量集合。 */
    private static boolean isImmutableScalar(Object value) {
        return value instanceof String
                || value instanceof Boolean
                || value instanceof Character
                || value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Float
                || value instanceof Double
                || value instanceof BigInteger
                || value instanceof BigDecimal
                || value instanceof Enum<?>;
    }
}
