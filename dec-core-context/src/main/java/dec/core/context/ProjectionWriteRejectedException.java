package dec.core.context;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;

/**
 * Projection 兼容写入被拒绝时使用的稳定专用异常。
 *
 * <p>本类型先冻结公共结构；Diagnostic 构造逻辑在 Development 阶段完成。</p>
 */
public final class ProjectionWriteRejectedException extends UnsupportedOperationException {
    private final String operation;

    /**
     * 保存被拒绝的兼容写操作名称。
     *
     * @param operation 稳定操作名称
     */
    public ProjectionWriteRejectedException(String operation) {
        super("Architecture skeleton only: " + operation);
        this.operation = operation;
    }

    /** 返回被拒绝的兼容写操作。 */
    public String operation() {
        return operation;
    }

    /**
     * 返回稳定错误码。
     *
     * @return Development 阶段完成后的 Projection 写入错误码
     */
    public DiagnosticCode diagnosticCode() {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    /**
     * 返回稳定 Diagnostic。
     *
     * @return Development 阶段完成后的拒绝事实
     */
    public Diagnostic diagnostic() {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }
}
