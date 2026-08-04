package dec.core.compiler.deferred;

import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Deferred 批量分类的不可变结果。
 */
public final class DeferredClassificationResult {
    private final DeferredClassificationStatus status;
    private final DeferredRegistry registry;
    private final List<Diagnostic> diagnostics;

    private DeferredClassificationResult(
            DeferredClassificationStatus status,
            DeferredRegistry registry,
            List<Diagnostic> diagnostics) {
        this.status = Objects.requireNonNull(status, "status");
        this.registry = registry;
        List<Diagnostic> copy = new ArrayList<Diagnostic>(
                Objects.requireNonNull(diagnostics, "diagnostics"));
        Collections.sort(copy);
        this.diagnostics = Collections.unmodifiableList(copy);
    }

    /** 创建成功结果。 */
    static DeferredClassificationResult classified(DeferredRegistry registry) {
        return new DeferredClassificationResult(
                DeferredClassificationStatus.CLASSIFIED,
                Objects.requireNonNull(registry, "registry"),
                Collections.<Diagnostic>emptyList());
    }

    /** 创建失败结果，失败结果不得携带部分 Registry。 */
    static DeferredClassificationResult failed(List<Diagnostic> diagnostics) {
        return new DeferredClassificationResult(
                DeferredClassificationStatus.FAILED,
                null,
                diagnostics);
    }

    public DeferredClassificationStatus status() {
        return status;
    }

    public Optional<DeferredRegistry> registry() {
        return Optional.ofNullable(registry);
    }

    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }
}
