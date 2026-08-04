package dec.core.compiler.modelaccess;

import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * T10 稳定 Diagnostic 创建与排序工具。
 */
final class ModelAccessDiagnostics {
    private static final SourceRef UNKNOWN_SOURCE =
            new SourceRef("<modelaccess-input>", 0, 0, "/");

    private ModelAccessDiagnostics() {
    }

    /** 创建入口参数缺失错误。 */
    static Diagnostic inputRequired() {
        return create(
                DiagnosticCode.MIX_MODEL_ACCESS_NOT_FOUND,
                "modelaccess.input.required",
                null,
                UNKNOWN_SOURCE,
                "请提供完整 RawDefinitionSet 与 SymbolTable");
    }

    /** 创建入口完整输入快照失配错误。 */
    static Diagnostic snapshotMismatch(RawDefinitionSet definitions) {
        return create(
                DiagnosticCode.MIX_MODEL_ACCESS_AMBIGUOUS,
                "modelaccess.input.snapshot-mismatch",
                null,
                sourceOf(definitions),
                "RawDefinitionSet 必须与生成 SymbolTable 的完整输入快照一致");
    }

    /** 创建 ModelAccess owner 身份错误。 */
    static Diagnostic ownerInvalid(SourceRef sourceRef) {
        return create(
                DiagnosticCode.MIX_MODEL_ACCESS_NOT_FOUND,
                "modelaccess.owner.invalid",
                null,
                sourceRef,
                "ModelAccess owner 必须精确命中当前 System");
    }

    /** 创建共享模型 View 未找到错误。 */
    static Diagnostic sourceViewNotFound(
            DefinitionKey sourceView,
            SourceRef sourceRef) {
        return create(
                DiagnosticCode.MIX_MODEL_ACCESS_NOT_FOUND,
                "modelaccess.source-view.not-found",
                sourceView,
                sourceRef,
                "model-ref 必须精确命中已声明的全局 View");
    }

    /** 创建目标 View 未在当前 System 声明错误。 */
    static Diagnostic viewNotDeclared(
            DefinitionKey targetView,
            SourceRef sourceRef) {
        return create(
                DiagnosticCode.MIX_REF_VIEW_NOT_DECLARED,
                "modelaccess.view.not-declared",
                targetView,
                sourceRef,
                "ref@view 必须由当前 System 的 view-info 显式声明");
    }

    /** 创建 selector 未找到错误。 */
    static Diagnostic selectorNotFound(
            DefinitionKey targetView,
            SourceRef sourceRef) {
        return create(
                DiagnosticCode.MIX_MODEL_ACCESS_NOT_FOUND,
                "modelaccess.selector.not-found",
                targetView,
                sourceRef,
                "请使用区分大小写的 target-main 或同一 View property path");
    }

    /** 创建 selector 多候选错误。 */
    static Diagnostic selectorAmbiguous(
            DefinitionKey targetView,
            SourceRef sourceRef) {
        return create(
                DiagnosticCode.MIX_MODEL_ACCESS_AMBIGUOUS,
                "modelaccess.selector.ambiguous",
                targetView,
                sourceRef,
                "同一 property 层级必须只有一个精确候选");
    }

    /** 创建 selector 中间段非复合错误。 */
    static Diagnostic selectorNonComposite(
            DefinitionKey targetView,
            SourceRef sourceRef) {
        return create(
                DiagnosticCode.MIX_MODEL_ACCESS_NON_COMPOSITE,
                "modelaccess.selector.non-composite",
                targetView,
                sourceRef,
                "property path 的非末段必须包含子 property");
    }

    /** 创建完全重复 Binding 错误。 */
    static Diagnostic duplicateBinding(
            DefinitionKey targetView,
            SourceRef sourceRef) {
        return create(
                DiagnosticCode.MIX_MODEL_ACCESS_AMBIGUOUS,
                "modelaccess.binding.duplicate",
                targetView,
                sourceRef,
                "相同 source path、mode、View 与 selector 只能声明一次");
    }

    /** 创建 WRITE source path 重叠错误。 */
    static Diagnostic writeOverlap(SourceRef sourceRef) {
        return create(
                DiagnosticCode.MIX_MODEL_ACCESS_AMBIGUOUS,
                "modelaccess.write.overlap",
                null,
                sourceRef,
                "WRITE source path 不得相同，也不得形成祖先或后代关系");
    }

    /** 创建 read/write/ref 结构或 lexical 非法错误。 */
    static Diagnostic structureInvalid(SourceRef sourceRef) {
        return create(
                DiagnosticCode.MIX_MODEL_ACCESS_NOT_FOUND,
                "modelaccess.structure.invalid",
                null,
                sourceRef,
                "ModelAccess 只允许合法 read/write@path 与 ref@view/property");
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
                sourceRef == null ? UNKNOWN_SOURCE : sourceRef,
                Collections.<SourceRef>emptyList(),
                hint,
                "modelaccess-compilation");
    }

    /** 将去重后的 Diagnostic 按统一比较器稳定排序。 */
    static List<Diagnostic> sorted(Set<Diagnostic> diagnostics) {
        List<Diagnostic> sorted = new ArrayList<Diagnostic>(diagnostics);
        Collections.sort(sorted);
        return sorted;
    }

    /** 返回输入中第一个可用来源位置。 */
    private static SourceRef sourceOf(RawDefinitionSet definitions) {
        return definitions == null || definitions.definitions().isEmpty()
                ? UNKNOWN_SOURCE
                : definitions.definitions().get(0).sourceRef();
    }
}
