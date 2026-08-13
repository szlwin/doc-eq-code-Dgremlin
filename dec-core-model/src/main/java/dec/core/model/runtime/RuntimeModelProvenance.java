package dec.core.model.runtime;

import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.model.ViewKey;
import java.util.Objects;

/** Handle 的不可变来源证明；只描述捕获 plan/view，不暴露真实 ModelData。 */
public final class RuntimeModelProvenance {
    private final RuntimeBindingPlan runtimeBindingPlan;
    private final ViewKey materializedViewKey;
    RuntimeModelProvenance(RuntimeBindingPlan runtimeBindingPlan, ViewKey materializedViewKey) {
        this.runtimeBindingPlan = Objects.requireNonNull(runtimeBindingPlan, "runtimeBindingPlan");
        this.materializedViewKey = Objects.requireNonNull(materializedViewKey, "materializedViewKey");
    }
    public RuntimeBindingPlan runtimeBindingPlan() { return runtimeBindingPlan; }
    public ViewKey materializedViewKey() { return materializedViewKey; }
}
