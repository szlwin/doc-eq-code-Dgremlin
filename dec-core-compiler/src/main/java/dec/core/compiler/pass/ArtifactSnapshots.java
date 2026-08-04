package dec.core.compiler.pass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
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
     * 递归复制受支持的不可变值和容器，拒绝未知可变对象。
     */
    static Object freeze(Object value) {
        Object checked = Objects.requireNonNull(value, "value");
        if (isImmutableScalar(checked) || checked instanceof ImmutablePipelineArtifact) {
            return checked;
        }
        if (checked instanceof Optional<?>) {
            Optional<?> optional = (Optional<?>) checked;
            return optional.isPresent()
                    ? Optional.of(freeze(optional.get()))
                    : Optional.empty();
        }
        if (checked instanceof List<?>) {
            List<Object> copy = new ArrayList<Object>();
            for (Object item : (List<?>) checked) {
                copy.add(freeze(item));
            }
            return Collections.unmodifiableList(copy);
        }
        if (checked instanceof Set<?>) {
            Set<Object> copy = new LinkedHashSet<Object>();
            for (Object item : (Set<?>) checked) {
                copy.add(freeze(item));
            }
            return Collections.unmodifiableSet(copy);
        }
        if (checked instanceof Map<?, ?>) {
            Map<Object, Object> copy = new LinkedHashMap<Object, Object>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) checked).entrySet()) {
                copy.put(freeze(entry.getKey()), freeze(entry.getValue()));
            }
            return Collections.unmodifiableMap(copy);
        }
        throw new IllegalArgumentException(
                "artifact value must be immutable or a supported container: "
                        + checked.getClass().getName());
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
