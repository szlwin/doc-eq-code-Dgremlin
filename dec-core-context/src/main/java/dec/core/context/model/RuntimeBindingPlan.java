package dec.core.context.model;

import java.util.Objects;

/**
 * P2 runtime 目标选择的编译期计划。
 * source TargetKey 与已解析 target binding 必须作为同一不可变事实传递。
 */
public final class RuntimeBindingPlan implements Comparable<RuntimeBindingPlan> {
    private final TargetKey sourceTargetKey;
    private final CompiledTargetBinding compiledTargetBinding;

    private RuntimeBindingPlan(
            TargetKey sourceTargetKey,
            CompiledTargetBinding compiledTargetBinding) {
        this.sourceTargetKey = Objects.requireNonNull(
                sourceTargetKey,
                "sourceTargetKey");
        this.compiledTargetBinding = Objects.requireNonNull(
                compiledTargetBinding,
                "compiledTargetBinding");
    }

    /** 使用已经编译的精确 target facts 创建运行计划。 */
    public static RuntimeBindingPlan exact(
            TargetKey source,
            CompiledTargetBinding binding) {
        return new RuntimeBindingPlan(source, binding);
    }

    public TargetKey sourceTargetKey() {
        return sourceTargetKey;
    }

    public CompiledTargetBinding compiledTargetBinding() {
        return compiledTargetBinding;
    }

    /** 返回不含 caller lexical 的稳定 plan 表示。 */
    public String canonicalForm() {
        return sourceTargetKey.toString()
                + "->"
                + compiledTargetBinding.canonicalForm();
    }

    @Override
    public int compareTo(RuntimeBindingPlan other) {
        Objects.requireNonNull(other, "other");
        int value = sourceTargetKey.compareTo(other.sourceTargetKey);
        return value != 0
                ? value
                : compiledTargetBinding.compareTo(other.compiledTargetBinding);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RuntimeBindingPlan)) {
            return false;
        }
        RuntimeBindingPlan that = (RuntimeBindingPlan) other;
        return sourceTargetKey.equals(that.sourceTargetKey)
                && compiledTargetBinding.equals(that.compiledTargetBinding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceTargetKey, compiledTargetBinding);
    }

    @Override
    public String toString() {
        return canonicalForm();
    }
}
