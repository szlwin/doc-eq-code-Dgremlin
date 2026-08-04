package dec.core.compiler.information;

import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * T09 全批编译结果；失败时绝不携带部分 Compilation。
 */
public final class InformationCompilationResult {
    private final InformationCompilationStatus status;
    private final InformationCompilation compilation;
    private final List<Diagnostic> diagnostics;

    /** 冻结成功或失败结果。 */
    private InformationCompilationResult(
            InformationCompilationStatus status,
            InformationCompilation compilation,
            List<Diagnostic> diagnostics) {
        this.status = Objects.requireNonNull(status, "status");
        this.compilation = compilation;
        List<Diagnostic> copy = new ArrayList<Diagnostic>(
                Objects.requireNonNull(diagnostics, "diagnostics"));
        Collections.sort(copy);
        this.diagnostics = Collections.unmodifiableList(copy);
    }

    /** 创建成功结果。 */
    public static InformationCompilationResult compiled(
            InformationCompilation compilation) {
        return new InformationCompilationResult(
                InformationCompilationStatus.COMPILED,
                Objects.requireNonNull(compilation, "compilation"),
                Collections.<Diagnostic>emptyList());
    }

    /** 创建失败且无部分结果的结果。 */
    public static InformationCompilationResult failed(
            List<Diagnostic> diagnostics) {
        if (Objects.requireNonNull(diagnostics, "diagnostics").isEmpty()) {
            throw new IllegalArgumentException("failed result needs diagnostics");
        }
        return new InformationCompilationResult(
                InformationCompilationStatus.FAILED,
                null,
                diagnostics);
    }

    /** 返回结果状态。 */
    public InformationCompilationStatus status() {
        return status;
    }

    /** 成功时返回完整 Compilation。 */
    public Optional<InformationCompilation> compilation() {
        return Optional.ofNullable(compilation);
    }

    /** 返回稳定排序 Diagnostic。 */
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }
}
