package dec.core.compiler.modelaccess;

import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;

/**
 * T10 稳定 Diagnostic 创建工具。
 */
final class ModelAccessDiagnostics {
    private ModelAccessDiagnostics() {
    }

    /** 创建入口完整输入快照失配错误。 */
    static Diagnostic snapshotMismatch(RawDefinitionSet definitions) {
        SourceRef sourceRef = definitions.definitions().isEmpty()
                ? new SourceRef("<modelaccess-input>", 0, 0, "/")
                : definitions.definitions().get(0).sourceRef();
        return create(
                DiagnosticCode.MIX_MODEL_ACCESS_AMBIGUOUS,
                "modelaccess.input.snapshot-mismatch",
                null,
                sourceRef,
                "RawDefinitionSet 必须与生成 SymbolTable 的完整输入快照一致");
    }

    /** 创建 Architecture Skeleton 尚未接入真实 selector 的受控错误。 */
    static Diagnostic notImplemented(RawDefinitionSet definitions) {
        SourceRef sourceRef = definitions.definitions().isEmpty()
                ? new SourceRef("<modelaccess-input>", 0, 0, "/")
                : definitions.definitions().get(0).sourceRef();
        return create(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                "modelaccess.selector.not-implemented",
                null,
                sourceRef,
                "Architecture Skeleton 尚未接入真实 selector 实现");
    }

    /** 创建稳定的 T10 ERROR。 */
    static Diagnostic create(
            DiagnosticCode code,
            String messageKey,
            DefinitionKey key,
            SourceRef sourceRef,
            String hint) {
        return new Diagnostic(
                code,
                DiagnosticSeverity.ERROR,
                messageKey,
                key,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                hint,
                "modelaccess-compilation");
    }
}
