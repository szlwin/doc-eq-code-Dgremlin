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

    /** 返回稳定排序且不可修改的 Binding。 */
    public List<ModelAccessBinding> bindings() {
        return bindings;
    }

    /** 返回本批 ModelAccess 的 P2 Deferred Registry。 */
    public DeferredRegistry deferredRegistry() {
        return deferredRegistry;
    }

    /** 返回成功发布的 Binding 数量。 */
    public int size() {
        return bindings.size();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ModelAccessCompilation)) {
            return false;
        }
        ModelAccessCompilation that = (ModelAccessCompilation) other;
        return bindings.equals(that.bindings)
                && deferredRegistry.equals(that.deferredRegistry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bindings, deferredRegistry);
    }

    @Override
    public String toString() {
        return "ModelAccessCompilation{bindings=" + bindings
                + ", deferredRegistry=" + deferredRegistry + '}';
    }
}
