package dec.core.compiler.modelaccess;

import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * T10 全批结果；失败时不携带部分 Binding 或 Deferred。
 */
public final class ModelAccessCompilationResult {
    private final ModelAccessCompilationStatus status;
    private final ModelAccessCompilation compilation;
    private final List<Diagnostic> diagnostics;

    private ModelAccessCompilationResult(
            ModelAccessCompilationStatus status,
            ModelAccessCompilation compilation,
            List<Diagnostic> diagnostics) {
        this.status = Objects.requireNonNull(status, "status");
        this.compilation = compilation;
        List<Diagnostic> copy = new ArrayList<Diagnostic>(
                Objects.requireNonNull(diagnostics, "diagnostics"));
        Collections.sort(copy);
        this.diagnostics = Collections.unmodifiableList(copy);
    }

    public static ModelAccessCompilationResult compiled(
            ModelAccessCompilation compilation) {
        return new ModelAccessCompilationResult(
                ModelAccessCompilationStatus.COMPILED,
                Objects.requireNonNull(compilation, "compilation"),
                Collections.<Diagnostic>emptyList());
    }

    public static ModelAccessCompilationResult failed(List<Diagnostic> diagnostics) {
        if (Objects.requireNonNull(diagnostics, "diagnostics").isEmpty()) {
            throw new IllegalArgumentException("failed result needs diagnostics");
        }
        return new ModelAccessCompilationResult(
                ModelAccessCompilationStatus.FAILED,
                null,
                diagnostics);
    }

    public ModelAccessCompilationStatus status() {
        return status;
    }

    public Optional<ModelAccessCompilation> compilation() {
        return Optional.ofNullable(compilation);
    }

    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }
}
