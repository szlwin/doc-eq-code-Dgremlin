package dec.core.compiler.pass;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticSeverity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 单个 CompilerPass 的不可变执行结果。
 */
public final class PassResult {
    private final List<Diagnostic> diagnostics;

    /** 防御性复制并稳定排序本阶段 Diagnostic。 */
    private PassResult(List<Diagnostic> diagnostics) {
        List<Diagnostic> copy = new ArrayList<Diagnostic>(
                Objects.requireNonNull(diagnostics, "diagnostics"));
        if (copy.contains(null)) {
            throw new NullPointerException("diagnostics contains null");
        }
        Collections.sort(copy);
        this.diagnostics = Collections.unmodifiableList(copy);
    }

    /** 创建没有 Diagnostic 的通过结果。 */
    public static PassResult passed() {
        return new PassResult(Collections.<Diagnostic>emptyList());
    }

    /** 创建携带 Diagnostic 的结果；是否失败由 ERROR 严重级别决定。 */
    public static PassResult of(List<Diagnostic> diagnostics) {
        return new PassResult(diagnostics);
    }

    /** 返回稳定排序且不可变的 Diagnostic。 */
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /** 判断结果是否包含阻断 Pipeline 的 ERROR。 */
    public boolean hasErrors() {
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
                return true;
            }
        }
        return false;
    }
}
