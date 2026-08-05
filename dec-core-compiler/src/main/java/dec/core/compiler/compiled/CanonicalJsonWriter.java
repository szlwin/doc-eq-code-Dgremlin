package dec.core.compiler.compiled;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * DEC-SEMANTIC-DIGEST-V1 使用的严格 canonical JSON 编码器。
 *
 * <p>该编码器只接受受控 JSON 值闭包，不执行反射 Bean 序列化或
 * 容错类型转换，避免未来类型扩展悄然改变摘要。</p>
 */
final class CanonicalJsonWriter {
    private static final Comparator<String> CODE_POINT_ORDER =
            new Comparator<String>() {
                @Override
                public int compare(String left, String right) {
                    return compareCodePoints(left, right);
                }
            };

    /** 工具类不允许实例化。 */
    private CanonicalJsonWriter() {
    }

    /** 将受控 JSON 值编码为无 BOM、无空白的 canonical JSON。 */
    static String write(Object value) {
        StringBuilder result = new StringBuilder();
        appendValue(
                result,
                value,
                new IdentityHashMap<Object, Boolean>());
        return result.toString();
    }

    /** 返回领域排序与 Object key 共用的 Unicode code point 顺序。 */
    static Comparator<String> codePointOrder() {
        return CODE_POINT_ORDER;
    }

    /** 按 JSON 值类型执行严格编码，并使用 active-path 检测循环。 */
    private static void appendValue(
            StringBuilder target,
            Object value,
            IdentityHashMap<Object, Boolean> activePath) {
        if (value == null) {
            target.append("null");
        } else if (value instanceof String) {
            appendString(target, (String) value);
        } else if (value instanceof Boolean) {
            target.append(((Boolean) value).booleanValue() ? "true" : "false");
        } else if (value instanceof Number) {
            target.append(canonicalNumber((Number) value));
        } else if (value instanceof Map) {
            appendObject(target, (Map<?, ?>) value, activePath);
        } else if (value instanceof Iterable) {
            appendIterable(target, (Iterable<?>) value, activePath);
        } else if (value.getClass().isArray()) {
            appendArray(target, value, activePath);
        } else {
            throw new IllegalArgumentException(
                    "unsupported canonical JSON value: "
                            + value.getClass().getName());
        }
    }

    /** 对 Object key 使用 code point 排序，并拒绝重复 canonical key。 */
    private static void appendObject(
            StringBuilder target,
            Map<?, ?> values,
            IdentityHashMap<Object, Boolean> activePath) {
        Objects.requireNonNull(values, "values");
        enter(values, activePath);
        try {
            List<ObjectEntry> entries = new ArrayList<ObjectEntry>();
            for (Map.Entry<?, ?> entry : values.entrySet()) {
                Map.Entry<?, ?> checkedEntry = Objects.requireNonNull(
                        entry,
                        "object entry");
                Object rawKey = Objects.requireNonNull(
                        checkedEntry.getKey(),
                        "object key");
                if (!(rawKey instanceof String)) {
                    throw new IllegalArgumentException(
                            "canonical JSON object key must be String");
                }
                entries.add(new ObjectEntry(
                        (String) rawKey,
                        checkedEntry.getValue()));
            }
            Collections.sort(entries, new Comparator<ObjectEntry>() {
                @Override
                public int compare(ObjectEntry left, ObjectEntry right) {
                    return CODE_POINT_ORDER.compare(left.key, right.key);
                }
            });
            for (int index = 1; index < entries.size(); index++) {
                if (entries.get(index - 1).key.equals(entries.get(index).key)) {
                    throw new IllegalArgumentException(
                            "duplicate canonical JSON object key: "
                                    + entries.get(index).key);
                }
            }

            target.append('{');
            for (int index = 0; index < entries.size(); index++) {
                if (index > 0) {
                    target.append(',');
                }
                ObjectEntry entry = entries.get(index);
                appendString(target, entry.key);
                target.append(':');
                appendValue(target, entry.value, activePath);
            }
            target.append('}');
        } finally {
            leave(values, activePath);
        }
    }

    /** 按调用方已冻结的领域顺序编码 Iterable。 */
    private static void appendIterable(
            StringBuilder target,
            Iterable<?> values,
            IdentityHashMap<Object, Boolean> activePath) {
        Objects.requireNonNull(values, "values");
        enter(values, activePath);
        try {
            target.append('[');
            boolean first = true;
            for (Object value : values) {
                if (!first) {
                    target.append(',');
                }
                appendValue(target, value, activePath);
                first = false;
            }
            target.append(']');
        } finally {
            leave(values, activePath);
        }
    }

    /** 按数组索引顺序编码对象或基础类型数组。 */
    private static void appendArray(
            StringBuilder target,
            Object values,
            IdentityHashMap<Object, Boolean> activePath) {
        enter(values, activePath);
        try {
            target.append('[');
            int length = Array.getLength(values);
            for (int index = 0; index < length; index++) {
                if (index > 0) {
                    target.append(',');
                }
                appendValue(target, Array.get(values, index), activePath);
            }
            target.append(']');
        } finally {
            leave(values, activePath);
        }
    }

    /** 在当前递归路径登记容器 identity，拒绝循环但允许共享 DAG。 */
    private static void enter(
            Object value,
            IdentityHashMap<Object, Boolean> activePath) {
        if (activePath.put(value, Boolean.TRUE) != null) {
            throw new IllegalArgumentException(
                    "cyclic canonical JSON value is not supported");
        }
    }

    /** 离开当前递归路径，允许同一不可变子图在其他位置复用。 */
    private static void leave(
            Object value,
            IdentityHashMap<Object, Boolean> activePath) {
        activePath.remove(value);
    }

    /** 使用标准 JSON escaping 编码字符串。 */
    private static void appendString(StringBuilder target, String value) {
        Objects.requireNonNull(value, "value");
        target.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"':
                    target.append("\\\"");
                    break;
                case '\\':
                    target.append("\\\\");
                    break;
                case '\b':
                    target.append("\\b");
                    break;
                case '\f':
                    target.append("\\f");
                    break;
                case '\n':
                    target.append("\\n");
                    break;
                case '\r':
                    target.append("\\r");
                    break;
                case '\t':
                    target.append("\\t");
                    break;
                default:
                    if (character < 0x20
                            || isUnpairedSurrogate(value, index, character)) {
                        appendUnicodeEscape(target, character);
                    } else {
                        target.append(character);
                    }
                    break;
            }
        }
        target.append('"');
    }

    /** 判断当前 UTF-16 code unit 是否为未配对 surrogate。 */
    private static boolean isUnpairedSurrogate(
            String value,
            int index,
            char character) {
        if (Character.isHighSurrogate(character)) {
            return index + 1 >= value.length()
                    || !Character.isLowSurrogate(value.charAt(index + 1));
        }
        return Character.isLowSurrogate(character)
                && (index == 0
                || !Character.isHighSurrogate(value.charAt(index - 1)));
    }

    /** 将控制字符或未配对 surrogate 编码为小写四位十六进制。 */
    private static void appendUnicodeEscape(
            StringBuilder target,
            char character) {
        target.append("\\u");
        String hex = Integer.toHexString(character);
        for (int index = hex.length(); index < 4; index++) {
            target.append('0');
        }
        target.append(hex);
    }

    /** 将 Number 转换为无指数、无多余零的 canonical decimal。 */
    private static String canonicalNumber(Number value) {
        Objects.requireNonNull(value, "value");
        BigDecimal decimal;
        if (value instanceof BigDecimal) {
            decimal = (BigDecimal) value;
        } else if (value instanceof BigInteger) {
            decimal = new BigDecimal((BigInteger) value);
        } else if (value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long) {
            decimal = BigDecimal.valueOf(value.longValue());
        } else if (value instanceof Float || value instanceof Double) {
            double number = value.doubleValue();
            if (Double.isNaN(number) || Double.isInfinite(number)) {
                throw new IllegalArgumentException(
                        "canonical JSON number must be finite");
            }
            decimal = BigDecimal.valueOf(number);
        } else {
            try {
                decimal = new BigDecimal(value.toString());
            } catch (NumberFormatException failure) {
                throw new IllegalArgumentException(
                        "invalid canonical JSON number",
                        failure);
            }
        }

        if (decimal.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        BigDecimal normalized = decimal.stripTrailingZeros();
        if (normalized.scale() < 0) {
            normalized = normalized.setScale(0);
        }
        return normalized.toPlainString();
    }

    /** 逐 code point 比较，避免 UTF-16 surrogate 顺序替代 Unicode 顺序。 */
    private static int compareCodePoints(String left, String right) {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        int leftIndex = 0;
        int rightIndex = 0;
        while (leftIndex < left.length() && rightIndex < right.length()) {
            int leftPoint = left.codePointAt(leftIndex);
            int rightPoint = right.codePointAt(rightIndex);
            if (leftPoint != rightPoint) {
                return Integer.compare(leftPoint, rightPoint);
            }
            leftIndex += Character.charCount(leftPoint);
            rightIndex += Character.charCount(rightPoint);
        }
        if (leftIndex == left.length() && rightIndex == right.length()) {
            return 0;
        }
        return leftIndex == left.length() ? -1 : 1;
    }

    /** 保存 Object entry 的 key/value，避免排序后再次调用不可信 Map.get。 */
    private static final class ObjectEntry {
        private final String key;
        private final Object value;

        private ObjectEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }
}
