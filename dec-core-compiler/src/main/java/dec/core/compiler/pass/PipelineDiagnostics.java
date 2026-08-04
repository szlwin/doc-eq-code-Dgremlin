package dec.core.compiler.pass;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;

/**
 * 为 Pipeline 级失败构造稳定、无外部副作用的 Diagnostic。
 */
final class PipelineDiagnostics {
    private static final SourceRef SOURCE = new SourceRef(
            "<pipeline>", 0, 0, "/compiler-pipeline");

    /** 工具类不允许实例化。 */
    private PipelineDiagnostics() {
    }

    /** 创建尚未实现行为的受控 RED Diagnostic。 */
    static Diagnostic notImplemented() {
        return error(
                DiagnosticCode.MIX_PUBLICATION_BLOCKED,
                "pipeline.not-implemented",
                "CompilerPipeline");
    }

    /** 创建协作式取消 Diagnostic。 */
    static Diagnostic cancelled(String pass) {
        return error(
                DiagnosticCode.MIX_COMPILATION_CANCELLED,
                "pipeline.cancelled",
                pass);
    }

    /** 创建绝对截止时间已到 Diagnostic。 */
    static Diagnostic timedOut(String pass) {
        return error(
                DiagnosticCode.MIX_COMPILATION_TIMED_OUT,
                "pipeline.timed-out",
                pass);
    }

    /** 创建非 Publication Pass 的受控异常 Diagnostic。 */
    static Diagnostic passFailure(String pass) {
        return error(
                DiagnosticCode.MIX_PUBLICATION_BLOCKED,
                "pipeline.pass.failure",
                pass);
    }

    /** 创建 Publication Pass 异常 Diagnostic。 */
    static Diagnostic publicationFailure() {
        return error(
                DiagnosticCode.MIX_PUBLICATION_FAILURE,
                "pipeline.publication.failure",
                CompilerPipeline.PUBLICATION_PASS);
    }

    /** 构造统一 ERROR Diagnostic。 */
    private static Diagnostic error(
            DiagnosticCode code,
            String messageKey,
            String pass) {
        return new Diagnostic(
                code,
                DiagnosticSeverity.ERROR,
                messageKey,
                null,
                SOURCE,
                Collections.<SourceRef>emptyList(),
                null,
                pass);
    }
}
