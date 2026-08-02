package dec.core.compiler.api;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticSeverity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 集中维护 Compiler 公共 API 的参数校验和防御性复制规则。
 */
final class ApiContracts {
    private ApiContracts() {
        throw new AssertionError("No instances");
    }

    /**
     * 规范化必填标识符，并拒绝空值和空白字符串。
     */
    static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }

    /**
     * 按稳定顺序复制 Diagnostic，防止调用方继续修改输入集合。
     */
    static List<Diagnostic> immutableDiagnostics(List<Diagnostic> diagnostics) {
        Objects.requireNonNull(diagnostics, "diagnostics");
        List<Diagnostic> copy = new ArrayList<Diagnostic>(diagnostics.size());
        for (Diagnostic diagnostic : diagnostics) {
            copy.add(Objects.requireNonNull(diagnostic, "diagnostics contains null"));
        }
        Collections.sort(copy);
        return Collections.unmodifiableList(copy);
    }

    /**
     * 校验成功发布结果不能携带 ERROR Diagnostic。
     */
    static List<Diagnostic> publishedDiagnostics(List<Diagnostic> diagnostics) {
        List<Diagnostic> copy = immutableDiagnostics(diagnostics);
        for (Diagnostic diagnostic : copy) {
            if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
                throw new IllegalArgumentException(
                        "published diagnostics must not contain ERROR entries");
            }
        }
        return copy;
    }

    /**
     * 校验失败结果至少包含一个明确的 ERROR 原因。
     */
    static List<Diagnostic> failedDiagnostics(List<Diagnostic> diagnostics) {
        List<Diagnostic> copy = immutableDiagnostics(diagnostics);
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("failed diagnostics must not be empty");
        }
        for (Diagnostic diagnostic : copy) {
            if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
                return copy;
            }
        }
        throw new IllegalArgumentException(
                "failed diagnostics must contain at least one ERROR entry");
    }
}
