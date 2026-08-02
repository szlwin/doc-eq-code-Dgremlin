package dec.core.compiler.api;

import dec.core.context.model.Diagnostic;
import java.util.List;
import java.util.Objects;

/**
 * 编译失败终态。
 *
 * <p>该类型故意不提供模型、Context、Digest 或版本访问器，
 * 避免调用方观察未发布候选。</p>
 */
public final class FailedCompilationResult implements CompilationResult {
    private final List<Diagnostic> diagnostics;

    /**
     * 冻结至少包含一个 ERROR 的 Diagnostic 快照。
     */
    private FailedCompilationResult(List<Diagnostic> diagnostics) {
        this.diagnostics = ApiContracts.failedDiagnostics(diagnostics);
    }

    /**
     * 创建失败编译结果。
     *
     * @param diagnostics 非空且至少包含一个 ERROR 的 Diagnostic
     * @return 不暴露任何候选发布事实的失败结果
     */
    public static FailedCompilationResult failed(List<Diagnostic> diagnostics) {
        return new FailedCompilationResult(diagnostics);
    }

    @Override
    public CompilationStatus status() {
        return CompilationStatus.FAILED;
    }

    @Override
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof FailedCompilationResult
                && diagnostics.equals(((FailedCompilationResult) other).diagnostics));
    }

    @Override
    public int hashCode() {
        return Objects.hash(diagnostics);
    }

    @Override
    public String toString() {
        return "FailedCompilationResult{diagnostics=" + diagnostics.size() + '}';
    }
}
