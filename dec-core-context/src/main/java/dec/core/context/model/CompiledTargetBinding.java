package dec.core.context.model;

import java.util.Objects;

/**
 * 编译器已经解析完成的目标绑定。
 * Runtime 只能消费这里的精确结果，不允许重新解析 selector 文本。
 */
public final class CompiledTargetBinding implements Comparable<CompiledTargetBinding> {
    /** 目标绑定的两种冻结形态。 */
    public enum Kind {
        TARGET_MAIN,
        PROPERTY_PATH
    }

    private final ViewKey targetViewKey;
    private final Kind kind;
    private final String exactResolvedValue;

    private CompiledTargetBinding(
            ViewKey targetViewKey,
            Kind kind,
            String exactResolvedValue) {
        this.targetViewKey = Objects.requireNonNull(targetViewKey, "targetViewKey");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.exactResolvedValue = requireExact(exactResolvedValue);
    }

    /** 创建精确 target-main 绑定。 */
    public static CompiledTargetBinding targetMain(
            ViewKey targetViewKey,
            String exactResolvedValue) {
        return new CompiledTargetBinding(
                targetViewKey,
                Kind.TARGET_MAIN,
                exactResolvedValue);
    }

    /** 创建精确 property-path 绑定。 */
    public static CompiledTargetBinding propertyPath(
            ViewKey targetViewKey,
            String exactResolvedValue) {
        return new CompiledTargetBinding(
                targetViewKey,
                Kind.PROPERTY_PATH,
                exactResolvedValue);
    }

    public ViewKey targetViewKey() {
        return targetViewKey;
    }

    public Kind kind() {
        return kind;
    }

    public String exactResolvedValue() {
        return exactResolvedValue;
    }

    /** 返回稳定文本，供 semantic digest 与运行时 binding proof 使用。 */
    public String canonicalForm() {
        return targetViewKey.canonical()
                + ":"
                + kind.name()
                + ":"
                + exactResolvedValue;
    }

    @Override
    public int compareTo(CompiledTargetBinding other) {
        Objects.requireNonNull(other, "other");
        int value = targetViewKey.compareTo(other.targetViewKey);
        if (value != 0) {
            return value;
        }
        value = kind.compareTo(other.kind);
        return value != 0
                ? value
                : exactResolvedValue.compareTo(other.exactResolvedValue);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CompiledTargetBinding)) {
            return false;
        }
        CompiledTargetBinding that = (CompiledTargetBinding) other;
        return targetViewKey.equals(that.targetViewKey)
                && kind == that.kind
                && exactResolvedValue.equals(that.exactResolvedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetViewKey, kind, exactResolvedValue);
    }

    @Override
    public String toString() {
        return canonicalForm();
    }

    /** 禁止 runtime binding 依赖 trim 或空文本修复。 */
    private static String requireExact(String value) {
        Objects.requireNonNull(value, "exactResolvedValue");
        if (value.isEmpty() || !value.equals(value.trim())) {
            throw new IllegalArgumentException(
                    "exactResolvedValue must be non-blank and trimmed");
        }
        return value;
    }
}
