package dec.core.compiler.modelaccess;

import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 单个 selector 的唯一解析结果；失败时不携带候选列表供调用方猜测。
 */
public final class ModelAccessResolution {
    private final TargetPropertyPath target;
    private final List<Diagnostic> diagnostics;

    private ModelAccessResolution(
            TargetPropertyPath target,
            List<Diagnostic> diagnostics) {
        this.target = target;
        List<Diagnostic> copy = new ArrayList<Diagnostic>(
                Objects.requireNonNull(diagnostics, "diagnostics"));
        Collections.sort(copy);
        this.diagnostics = Collections.unmodifiableList(copy);
    }

    /** 创建唯一成功目标。 */
    public static ModelAccessResolution resolved(TargetPropertyPath target) {
        return new ModelAccessResolution(
                Objects.requireNonNull(target, "target"),
                Collections.<Diagnostic>emptyList());
    }

    /** 创建无候选回退的失败结果。 */
    public static ModelAccessResolution failed(List<Diagnostic> diagnostics) {
        if (Objects.requireNonNull(diagnostics, "diagnostics").isEmpty()) {
            throw new IllegalArgumentException("failed resolution needs diagnostics");
        }
        return new ModelAccessResolution(null, diagnostics);
    }

    public Optional<TargetPropertyPath> target() {
        return Optional.ofNullable(target);
    }

    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }
}
