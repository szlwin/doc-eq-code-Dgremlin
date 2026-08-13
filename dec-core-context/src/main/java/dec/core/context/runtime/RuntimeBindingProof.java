package dec.core.context.runtime;

import dec.core.context.model.RuntimeBindingPlan;
import java.util.Objects;

/** P2 runtime target 的精确编译计划证明；不携带权限，只证明解析来源。 */
public final class RuntimeBindingProof {
    private final RuntimeBindingPlan runtimeBindingPlan;

    private RuntimeBindingProof(RuntimeBindingPlan runtimeBindingPlan) {
        this.runtimeBindingPlan = Objects.requireNonNull(runtimeBindingPlan, "runtimeBindingPlan");
    }

    /** 使用同一 captured Context 中的精确 RuntimeBindingPlan 创建证明。 */
    public static RuntimeBindingProof exact(RuntimeBindingPlan runtimeBindingPlan) {
        return new RuntimeBindingProof(runtimeBindingPlan);
    }

    public RuntimeBindingPlan runtimeBindingPlan() {
        return runtimeBindingPlan;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof RuntimeBindingProof
                && runtimeBindingPlan.equals(((RuntimeBindingProof) other).runtimeBindingPlan);
    }

    @Override
    public int hashCode() {
        return runtimeBindingPlan.hashCode();
    }
}
