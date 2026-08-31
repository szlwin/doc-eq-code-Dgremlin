package dec.core.context.model;

import java.util.Objects;

/**
 * 编译器发布的单条精确 model-access 兼容元数据。
 * 只有静态校验通过的规则进入本类型；缺失 exact key 表示未声明。
 */
public final class CompiledModelAccessRule implements Comparable<CompiledModelAccessRule> {
    private final ModelAccessRuleKey key;
    private final AccessCompilationStatus status;
    private final RuntimeBindingPlan runtimeBindingPlan;
    private final SourceRef sourceRef;

    private CompiledModelAccessRule(
            ModelAccessRuleKey key,
            AccessCompilationStatus status,
            RuntimeBindingPlan runtimeBindingPlan,
            SourceRef sourceRef) {
        this.key = Objects.requireNonNull(key, "key");
        this.status = requirePublishedStatus(status);
        this.runtimeBindingPlan = Objects.requireNonNull(
                runtimeBindingPlan,
                "runtimeBindingPlan");
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
    }

    /** 创建一条可发布的 exact model-access rule。 */
    public static CompiledModelAccessRule of(
            ModelAccessRuleKey key,
            AccessCompilationStatus status,
            RuntimeBindingPlan runtimeBindingPlan,
            SourceRef sourceRef) {
        return new CompiledModelAccessRule(
                key,
                status,
                runtimeBindingPlan,
                sourceRef);
    }

    public ModelAccessRuleKey key() {
        return key;
    }

    public AccessCompilationStatus status() {
        return status;
    }

    public RuntimeBindingPlan runtimeBindingPlan() {
        return runtimeBindingPlan;
    }

    public SourceRef sourceRef() {
        return sourceRef;
    }

    /** 返回稳定文本，供 PolicyIndex 与语义摘要使用。 */
    public String canonicalForm() {
        return key.toString()
                + "="
                + status.name()
                + "@"
                + runtimeBindingPlan.canonicalForm();
    }

    @Override
    public int compareTo(CompiledModelAccessRule other) {
        Objects.requireNonNull(other, "other");
        int value = key.compareTo(other.key);
        if (value != 0) {
            return value;
        }
        value = status.compareTo(other.status);
        if (value != 0) {
            return value;
        }
        value = runtimeBindingPlan.compareTo(other.runtimeBindingPlan);
        return value != 0 ? value : sourceRef.compareTo(other.sourceRef);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CompiledModelAccessRule)) {
            return false;
        }
        CompiledModelAccessRule that = (CompiledModelAccessRule) other;
        return key.equals(that.key)
                && status == that.status
                && runtimeBindingPlan.equals(that.runtimeBindingPlan)
                && sourceRef.equals(that.sourceRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, status, runtimeBindingPlan, sourceRef);
    }

    @Override
    public String toString() {
        return canonicalForm();
    }

    /** 简化运行模型只允许静态校验通过的规则进入不可变索引。 */
    private static AccessCompilationStatus requirePublishedStatus(
            AccessCompilationStatus status) {
        AccessCompilationStatus checked = Objects.requireNonNull(status, "status");
        if (checked != AccessCompilationStatus.STATIC_ALLOW) {
            throw new IllegalArgumentException(
                    "only STATIC_ALLOW may be published as model-access metadata");
        }
        return checked;
    }
}
