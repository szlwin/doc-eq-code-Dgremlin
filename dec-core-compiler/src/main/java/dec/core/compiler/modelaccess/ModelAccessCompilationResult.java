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

    /** 创建不携带 Diagnostic 的成功结果。 */
    public static ModelAccessCompilationResult compiled(
            ModelAccessCompilation compilation) {
        return new ModelAccessCompilationResult(
                ModelAccessCompilationStatus.COMPILED,
                Objects.requireNonNull(compilation, "compilation"),
                Collections.<Diagnostic>emptyList());
    }

    /** 创建不携带部分 Compilation 的失败结果。 */
    public static ModelAccessCompilationResult failed(List<Diagnostic> diagnostics) {
        if (Objects.requireNonNull(diagnostics, "diagnostics").isEmpty()) {
            throw new IllegalArgumentException("failed result needs diagnostics");
        }
        return new ModelAccessCompilationResult(
                ModelAccessCompilationStatus.FAILED,
                null,
                diagnostics);
    }

    /** 返回 COMPILED 或 FAILED。 */
    public ModelAccessCompilationStatus status() {
        return status;
    }

    /** 成功时返回完整 Compilation，失败时为空。 */
    public Optional<ModelAccessCompilation> compilation() {
        return Optional.ofNullable(compilation);
    }

    /** 返回稳定排序且不可修改的 Diagnostic。 */
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ModelAccessCompilationResult)) {
            return false;
        }
        ModelAccessCompilationResult that =
                (ModelAccessCompilationResult) other;
        return status == that.status
                && Objects.equals(compilation, that.compilation)
                && diagnostics.equals(that.diagnostics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, compilation, diagnostics);
    }

    @Override
    public String toString() {
        return "ModelAccessCompilationResult{status=" + status
                + ", compilation=" + compilation
                + ", diagnostics=" + diagnostics + '}';
    }
}
