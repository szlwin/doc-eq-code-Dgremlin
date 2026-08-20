package dec.core.model.runtime;

import dec.core.context.data.ModelData;
import dec.core.model.container.Container;
import java.util.Objects;

/** MODEL minted trusted handle；无 public constructor/wrap/rebind/ModelData getter。 */
public final class RuntimeModelHandle {
    private final RuntimeModelProvenance provenance;
    private final ModelData modelData;
    private final Container container;

    RuntimeModelHandle(RuntimeModelProvenance provenance, ModelData modelData, Container container) {
        this.provenance = Objects.requireNonNull(provenance, "provenance");
        this.modelData = Objects.requireNonNull(modelData, "modelData");
        this.container = Objects.requireNonNull(container, "container");
    }

    public RuntimeModelProvenance provenance() { return provenance; }
    /** 仅 MODEL 内部 Session/effect 使用，禁止把 trusted ModelData 暴露给 STARTER/业务层。 */
    ModelData modelData() { return modelData; }
    /** 仅 MODEL effect 边界执行已经绑定同一 ModelData 的 production Container。 */
    Container container() { return container; }
}
