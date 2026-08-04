package dec.core.compiler.modelaccess;

import java.util.Objects;

/**
 * selector 的唯一解析结果：View target-main 或精确 property path。
 */
public final class TargetPropertyPath implements Comparable<TargetPropertyPath> {
    /** 目标解析类型。 */
    public enum Kind {
        TARGET_MAIN,
        PROPERTY_PATH
    }

    private final Kind kind;
    private final String value;

    /** 冻结解析类型和精确路径。 */
    public TargetPropertyPath(Kind kind, String value) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.value = new SystemViewSelector(value).value();
    }

    /** 创建 target-main 结果。 */
    public static TargetPropertyPath targetMain(String value) {
        return new TargetPropertyPath(Kind.TARGET_MAIN, value);
    }

    /** 创建 property path 结果。 */
    public static TargetPropertyPath propertyPath(String value) {
        return new TargetPropertyPath(Kind.PROPERTY_PATH, value);
    }

    /** 返回解析类型。 */
    public Kind kind() {
        return kind;
    }

    /** 返回精确目标值。 */
    public String value() {
        return value;
    }

    @Override
    public int compareTo(TargetPropertyPath other) {
        Objects.requireNonNull(other, "other");
        int comparison = kind.compareTo(other.kind);
        return comparison == 0 ? value.compareTo(other.value) : comparison;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TargetPropertyPath)) {
            return false;
        }
        TargetPropertyPath that = (TargetPropertyPath) other;
        return kind == that.kind && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, value);
    }

    @Override
    public String toString() {
        return kind + ":" + value;
    }
}
