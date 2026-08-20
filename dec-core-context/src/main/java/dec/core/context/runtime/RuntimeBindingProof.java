package dec.core.context.runtime;

import dec.core.context.model.RuntimeBindingPlan;
import java.util.Objects;

/** P2 runtime target 的精确编译计划证明；不携带权限，只证明解析来源。 */
public final class RuntimeBindingProof {
    private final RuntimeBindingPlan runtimeBindingPlan;
    private final String value;

    private RuntimeBindingProof(RuntimeBindingPlan runtimeBindingPlan, String value) {
        this.runtimeBindingPlan = runtimeBindingPlan;
        this.value = requireExact(value);
    }

    /** 当前 MODEL 路径使用同一 captured Context 中的精确 RuntimeBindingPlan 创建证明。 */
    public static RuntimeBindingProof exact(RuntimeBindingPlan runtimeBindingPlan) {
        RuntimeBindingPlan checked = Objects.requireNonNull(runtimeBindingPlan, "runtimeBindingPlan");
        return new RuntimeBindingProof(checked, checked.canonicalForm());
    }

    /** DESIGN-P2-R30 保留的 R29 neutral construction surface。 */
    public static RuntimeBindingProof exact(String value) {
        return new RuntimeBindingProof(null, value);
    }

    /** MODEL 同源校验使用；digest-only compatibility proof 不携带可执行 plan。 */
    public RuntimeBindingPlan runtimeBindingPlan() {
        return runtimeBindingPlan;
    }

    /** R29 保留 getter；值精确、区分大小写且不可 trim 修复。 */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof RuntimeBindingProof
                && value.equals(((RuntimeBindingProof) other).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    private static String requireExact(String value) {
        Objects.requireNonNull(value, "value");
        if (value.trim().isEmpty() || !value.equals(value.trim())) {
            throw new IllegalArgumentException("value must be non-blank and trimmed");
        }
        return value;
    }
}
