package dec.core.compiler.information;

import dec.core.compiler.raw.RawDefinition;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.InformationKey;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * T09 Diagnostic 创建与稳定排序工具。
 */
final class InformationDiagnostics {
    private InformationDiagnostics() {
    }

    /** 创建 owner 身份失配 Diagnostic。 */
    static Diagnostic owner(
            RawDefinition definition,
            InformationKey key) {
        return create(
                DiagnosticCode.MIX_INFORMATION_OWNER,
                "information.owner.invalid",
                key,
                definition.sourceRef(),
                "请修复 Information owner 与 T07 Symbol 身份");
    }

    /** 创建 common 成员越界 Diagnostic。 */
    static Diagnostic commonMember(
            RawDefinition definition,
            InformationKey key) {
        return create(
                DiagnosticCode.MIX_COMMON_MEMBER,
                "information.common.member.invalid",
                key,
                definition.sourceRef(),
                "common 仅允许 name 与 expression，且 data/view/rule/model 成员必须为空");
    }

    /** 创建稳定 T09 Diagnostic。 */
    static Diagnostic create(
            DiagnosticCode code,
            String messageKey,
            InformationKey key,
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
                "information-compilation");
    }

    /** 去重后按 Diagnostic.compareTo 稳定排序。 */
    static List<Diagnostic> sorted(Set<Diagnostic> diagnostics) {
        List<Diagnostic> sorted = new ArrayList<Diagnostic>(diagnostics);
        Collections.sort(sorted);
        return sorted;
    }
}
