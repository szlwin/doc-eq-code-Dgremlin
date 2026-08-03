package dec.core.compiler.raw;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;
import java.util.List;

/**
 * 将 Canonical 文档转换为 RawDefinitionSet 的无状态 Builder 接缝。
 *
 * <p>Architecture Skeleton 只冻结 API 与失败边界；完整结构验证和提取由
 * Development 阶段实现。</p>
 */
public final class RawDefinitionBuilder {

    /**
     * 创建无跨调用可变状态的 Builder。
     */
    public RawDefinitionBuilder() {
    }

    /**
     * Skeleton 阶段以受控失败表示行为尚未实现，不泄露部分集合。
     *
     * @param documents 有序 Canonical 文档
     * @return 当前固定为 FAILED 的架构接缝结果
     */
    public RawBuildResult build(List<CanonicalDocumentNode> documents) {
        SourceRef sourceRef = documents != null
                && !documents.isEmpty()
                && documents.get(0) != null
                ? documents.get(0).sourceRef()
                : new SourceRef("<unknown-canonical-source>", 0, 0, "/");
        Diagnostic diagnostic = new Diagnostic(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                DiagnosticSeverity.ERROR,
                "raw.builder.not-implemented",
                null,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                "请在 Development 阶段实现冻结的 RawDefinitionSet 合同",
                "raw-definition-builder");
        return RawBuildResult.failed(Collections.singletonList(diagnostic));
    }
}
