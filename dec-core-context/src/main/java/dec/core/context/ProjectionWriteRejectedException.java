package dec.core.context;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;
import java.util.Objects;

/**
 * Projection 兼容写入被拒绝时使用的稳定专用异常。
 *
 * <p>异常同时携带公共错误码与完整 Diagnostic，使调用方可以区分
 * Projection 写入拒绝和普通不可修改集合异常。</p>
 */
public final class ProjectionWriteRejectedException extends UnsupportedOperationException {
    private static final long serialVersionUID = 1L;
    private static final String SOURCE_ID = "synthetic:core-config-projection";

    private final String operation;
    private final Diagnostic diagnostic;

    /**
     * 创建一个稳定、不可变的 Projection 写入拒绝事实。
     *
     * @param operation 被拒绝的兼容写操作名称
     */
    public ProjectionWriteRejectedException(String operation) {
        super(DiagnosticCode.MIX_PROJECTION_WRITE.code()
                + ": CoreConfigProjection is read-only: "
                + String.valueOf(operation));
        this.operation = requireOperation(operation);
        this.diagnostic = createDiagnostic(this.operation);
    }

    /** 返回被拒绝的兼容写操作。 */
    public String operation() {
        return operation;
    }

    /** 返回稳定的 Projection 写入错误码。 */
    public DiagnosticCode diagnosticCode() {
        return diagnostic.code();
    }

    /** 返回描述本次拒绝的完整不可变 Diagnostic。 */
    public Diagnostic diagnostic() {
        return diagnostic;
    }

    /**
     * 规范化操作名称，避免空白操作形成不可追踪的错误事实。
     */
    private static String requireOperation(String operation) {
        String normalized = Objects.requireNonNull(operation, "operation").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("operation must not be blank");
        }
        return normalized;
    }

    /**
     * 为所有兼容写入口生成相同结构的稳定拒绝 Diagnostic。
     */
    private static Diagnostic createDiagnostic(String operation) {
        return new Diagnostic(
                DiagnosticCode.MIX_PROJECTION_WRITE,
                DiagnosticSeverity.ERROR,
                "projection.write.rejected",
                null,
                new SourceRef(
                        SOURCE_ID,
                        0,
                        0,
                        "/compatibility-write/" + operation),
                Collections.<SourceRef>emptyList(),
                "请重新编译并发布新的 CompiledModelSet，不要修改已发布 Projection",
                "CoreConfigProjection");
    }
}
