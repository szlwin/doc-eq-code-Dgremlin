package dec.core.compiler.api;

import dec.core.context.model.Diagnostic;
import java.util.List;
import java.util.Objects;

/**
 * 编译失败终态。
 *
 * <p>该类型故意不提供模型、Context 或 Digest 访问器，避免调用方观察未发布候选。</p>
 */
public final class FailedCompilationResult extends CompilationResult {
    /**
     * 冻结失败会话身份和解释未发布原因的 Diagnostic。
     *
     * @param sessionId 失败编译会话的稳定身份
     * @param diagnostics 非空且至少包含一个 ERROR 的 Diagnostic 快照
     */
    public FailedCompilationResult(String sessionId, List<Diagnostic> diagnostics) {
        super(sessionId, ApiContracts.failedDiagnostics(diagnostics));
    }

    @Override
    public CompilationStatus status() {
        return CompilationStatus.FAILED;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof FailedCompilationResult
                && sessionId().equals(((FailedCompilationResult) other).sessionId())
                && diagnostics().equals(((FailedCompilationResult) other).diagnostics()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId(), diagnostics());
    }

    @Override
    public String toString() {
        return "FailedCompilationResult{"
                + "sessionId='" + sessionId() + '\''
                + ", diagnostics=" + diagnostics().size()
                + '}';
    }
}
