package dec.core.compiler.api;

import dec.core.context.model.Diagnostic;
import java.util.List;

/**
 * 成功发布与失败编译共享的不可变终态结果。
 */
public abstract class CompilationResult {
    private final String sessionId;
    private final List<Diagnostic> diagnostics;

    /**
     * 冻结所有终态结果共享的会话身份和 Diagnostic 快照。
     *
     * @param sessionId 隔离编译会话的稳定身份
     * @param diagnostics 已由具体子类型校验的 Diagnostic
     */
    protected CompilationResult(String sessionId, List<Diagnostic> diagnostics) {
        this.sessionId = ApiContracts.requireText(sessionId, "sessionId");
        this.diagnostics = ApiContracts.immutableDiagnostics(diagnostics);
    }

    /**
     * 返回产生当前终态结果的稳定会话身份。
     */
    public final String sessionId() {
        return sessionId;
    }

    /**
     * 返回当前会话的终态。
     *
     * @return {@link CompilationStatus#PUBLISHED} 或 {@link CompilationStatus#FAILED}
     */
    public abstract CompilationStatus status();

    /**
     * 返回按稳定顺序冻结的只读 Diagnostic 快照。
     */
    public final List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /**
     * 返回当前结果是否代表已经成功发布。
     */
    public final boolean isPublished() {
        return status() == CompilationStatus.PUBLISHED;
    }
}
