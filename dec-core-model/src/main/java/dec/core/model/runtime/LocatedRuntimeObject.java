package dec.core.model.runtime;

import dec.core.context.runtime.ResolvedRuntimeTarget;
import java.util.Objects;

/** Session 成功 exact-locate 后的 MODEL trusted 结果；不暴露 ModelData。 */
public final class LocatedRuntimeObject {
    private final ResolvedRuntimeTarget target;

    LocatedRuntimeObject(ResolvedRuntimeTarget target) {
        this.target = Objects.requireNonNull(target, "target");
    }

    public ResolvedRuntimeTarget target() {
        return target;
    }
}
