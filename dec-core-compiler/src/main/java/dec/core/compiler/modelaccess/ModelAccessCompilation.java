package dec.core.compiler.modelaccess;

import dec.core.context.model.DeferredRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * T10 全批成功后发布的不可变 Binding 与 P2 Deferred 快照。
 */
public final class ModelAccessCompilation {
    private final List<ModelAccessBinding> bindings;
    private final DeferredRegistry deferredRegistry;

    /** 冻结稳定排序 Binding 与 Deferred Registry。 */
    public ModelAccessCompilation(
            List<ModelAccessBinding> bindings,
            DeferredRegistry deferredRegistry) {
        List<ModelAccessBinding> copy = new ArrayList<ModelAccessBinding>(
                Objects.requireNonNull(bindings, "bindings"));
        Collections.sort(copy);
        this.bindings = Collections.unmodifiableList(copy);
        this.deferredRegistry = Objects.requireNonNull(
                deferredRegistry,
                "deferredRegistry");
    }

    public List<ModelAccessBinding> bindings() {
        return bindings;
    }

    public DeferredRegistry deferredRegistry() {
        return deferredRegistry;
    }

    public int size() {
        return bindings.size();
    }
}
