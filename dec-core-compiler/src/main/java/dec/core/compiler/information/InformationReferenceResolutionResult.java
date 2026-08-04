package dec.core.compiler.information;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.InformationKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 单个 AST 引用解析的不可变结果。
 */
public final class InformationReferenceResolutionResult {
    private final List<InformationKey> references;
    private final List<Diagnostic> diagnostics;

    /** 冻结引用解析结果。 */
    private InformationReferenceResolutionResult(
            List<InformationKey> references,
            List<Diagnostic> diagnostics) {
        List<InformationKey> keyCopy = new ArrayList<InformationKey>(
                Objects.requireNonNull(references, "references"));
        Collections.sort(keyCopy);
        this.references = Collections.unmodifiableList(keyCopy);
        List<Diagnostic> diagnosticCopy = new ArrayList<Diagnostic>(
                Objects.requireNonNull(diagnostics, "diagnostics"));
        Collections.sort(diagnosticCopy);
        this.diagnostics = Collections.unmodifiableList(diagnosticCopy);
    }

    /** 创建成功结果。 */
    public static InformationReferenceResolutionResult resolved(
            List<InformationKey> references) {
        return new InformationReferenceResolutionResult(
                references,
                Collections.<Diagnostic>emptyList());
    }

    /** 创建失败结果。 */
    public static InformationReferenceResolutionResult failed(
            List<Diagnostic> diagnostics) {
        if (Objects.requireNonNull(diagnostics, "diagnostics").isEmpty()) {
            throw new IllegalArgumentException("failed resolution needs diagnostics");
        }
        return new InformationReferenceResolutionResult(
                Collections.<InformationKey>emptyList(),
                diagnostics);
    }

    /** 返回稳定排序且不可修改的精确 InformationKey。 */
    public List<InformationKey> references() {
        return references;
    }

    /** 返回稳定排序 Diagnostic。 */
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }
}
