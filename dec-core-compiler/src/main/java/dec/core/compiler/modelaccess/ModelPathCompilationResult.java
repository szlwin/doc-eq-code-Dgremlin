package dec.core.compiler.modelaccess;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.ModelPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** ModelPathCompiler 的原子结果；失败时不得携带部分路径。 */
public final class ModelPathCompilationResult {
    private final List<ModelPath> paths;
    private final List<Diagnostic> diagnostics;

    private ModelPathCompilationResult(
            List<ModelPath> paths,
            List<Diagnostic> diagnostics) {
        this.paths = paths;
        this.diagnostics = diagnostics;
    }

    /** 创建完整成功结果。 */
    public static ModelPathCompilationResult compiled(List<ModelPath> paths) {
        List<ModelPath> copy = new ArrayList<ModelPath>(
                Objects.requireNonNull(paths, "paths"));
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("compiled paths must not be empty");
        }
        Collections.sort(copy);
        return new ModelPathCompilationResult(
                Collections.unmodifiableList(copy),
                Collections.<Diagnostic>emptyList());
    }

    /** 创建不携带部分路径的失败结果。 */
    public static ModelPathCompilationResult failed(List<Diagnostic> diagnostics) {
        List<Diagnostic> copy = new ArrayList<Diagnostic>(
                Objects.requireNonNull(diagnostics, "diagnostics"));
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("failed result needs diagnostics");
        }
        Collections.sort(copy);
        return new ModelPathCompilationResult(
                Collections.<ModelPath>emptyList(),
                Collections.unmodifiableList(copy));
    }

    public boolean compiled() {
        return diagnostics.isEmpty();
    }

    public List<ModelPath> paths() {
        return paths;
    }

    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }
}
