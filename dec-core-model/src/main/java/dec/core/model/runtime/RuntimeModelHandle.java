package dec.core.model.runtime;

import dec.core.context.data.ModelData;
import java.util.Objects;

/** MODEL minted trusted handle；无 public constructor/wrap/rebind/ModelData getter。 */
public final class RuntimeModelHandle {
    private final RuntimeModelProvenance provenance;
    private final ModelData modelData;
    RuntimeModelHandle(RuntimeModelProvenance provenance, ModelData modelData) {
        this.provenance = Objects.requireNonNull(provenance, "provenance");
        this.modelData = Objects.requireNonNull(modelData, "modelData");
    }
    public RuntimeModelProvenance provenance() { return provenance; }
    /** 仅 MODEL 内部后续 Session/effect 使用，禁止把 trusted ModelData 暴露给 STARTER/业务层。 */
    ModelData modelData() { return modelData; }
}
