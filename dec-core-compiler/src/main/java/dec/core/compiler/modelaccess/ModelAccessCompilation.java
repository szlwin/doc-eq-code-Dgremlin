package dec.core.compiler.modelaccess;

import dec.core.context.model.DeferredRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 全批成功后发布的不可变 Binding 快照。
 */
public final class ModelAccessCompilation {
    private final List<ModelAccessBinding> bindings;
    private final DeferredRegistry deferredRegistry;

    /** 冻结稳定排序 Binding；Deferred 参数只允许传入兼容用空 Registry。 */
    public ModelAccessCompilation(
            List<ModelAccessBinding> bindings,
            DeferredRegistry deferredRegistry) {
        List<ModelAccessBinding> copy = new ArrayList<ModelAccessBinding>(
                Objects.requireNonNull(bindings, "bindings"));
        Collections.sort(copy);
        this.bindings = Collections.unmodifiableList(copy);
        DeferredRegistry checked = Objects.requireNonNull(
                deferredRegistry,
                "deferredRegistry");
        if (checked.size() != 0) {
            throw new IllegalArgumentException(
                    "model-access must not publish deferred definitions");
        }
        this.deferredRegistry = checked;
    }

    /** 返回稳定排序且不可修改的 Binding。 */
    public List<ModelAccessBinding> bindings() {
        return bindings;
    }

    /** 返回兼容旧 API 的空 Deferred Registry。 */
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
