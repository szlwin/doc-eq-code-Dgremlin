package dec.core.compiler.api;

import dec.core.context.model.Diagnostic;
import java.util.List;
import java.util.Objects;

/**
 * 成功发布与失败编译共享的不可变终态结果。
 */
public abstract class CompilationResult {
    private final String sessionId;
    private final List<Diagnostic> diagnostics;

    /**
     * 标识 Diagnostic 的所有权处理方式。
     */
    enum DiagnosticOwnership {
        COPY,
        PUBLISHED_MODEL_FACT
    }

    /**
     * 冻结所有终态结果共享的会话身份和 Diagnostic 快照。
     *
     * <p>默认路径执行防御性复制，适用于失败结果及外部扩展子类。</p>
     *
     * @param sessionId 隔离编译会话的稳定身份
     * @param diagnostics 已由具体子类型校验的 Diagnostic
     */
    protected CompilationResult(String sessionId, List<Diagnostic> diagnostics) {
        this(sessionId, diagnostics, DiagnosticOwnership.COPY);
    }

    /**
     * 为同包内成功结果提供受控的发布事实复用入口。
     *
     * <p>只有 {@link DiagnosticOwnership#PUBLISHED_MODEL_FACT} 可以直接复用
     * {@code CompiledModelSet.diagnostics()}；其他路径仍执行防御性复制。</p>
     */
    CompilationResult(
            String sessionId,
            List<Diagnostic> diagnostics,
            DiagnosticOwnership ownership) {
        this.sessionId = ApiContracts.requireText(sessionId, "sessionId");
        DiagnosticOwnership requiredOwnership = Objects.requireNonNull(
                ownership,
                "ownership");
        if (requiredOwnership == DiagnosticOwnership.PUBLISHED_MODEL_FACT) {
            // 模型已在 T01 发布边界完成排序与不可变冻结，成功结果必须复用同一事实实例。
            this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
        } else {
            this.diagnostics = ApiContracts.immutableDiagnostics(diagnostics);
        }
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
