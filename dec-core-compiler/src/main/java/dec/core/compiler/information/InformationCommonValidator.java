package dec.core.compiler.information;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.InformationKey;
import java.util.Set;

/**
 * common System 与 common Information 的 P1 结构边界校验器。
 */
final class InformationCommonValidator {
    private InformationCommonValidator() {
    }

    /** 验证 canonical common System 的 data/view/rule-file section 不携带成员。 */
    static void validateSystem(
            RawDefinitionSet definitions,
            Set<Diagnostic> diagnostics) {
        for (RawDefinition definition
                : definitions.definitions(RawDefinitionKind.SYSTEM)) {
            if (!InformationIdentity.isCommonSystemName(definition.name())) {
                continue;
            }
            for (RawNodeBody child : definition.body().children()) {
                if ("information-info".equals(child.name())) {
                    continue;
                }
                if (!isEmptySection(child)) {
                    diagnostics.add(InformationDiagnostics.commonMember(
                            definition,
                            null));
                }
            }
        }
    }

    /** canonical common 不允许 ModelAccess，避免 P1 引入运行时读取。 */
    static void validateModelAccess(
            RawDefinitionSet definitions,
            Set<Diagnostic> diagnostics) {
        for (RawDefinition definition
                : definitions.definitions(RawDefinitionKind.MODEL_ACCESS)) {
            if (InformationIdentity.isCommonOwner(definition.ownerToken())) {
                diagnostics.add(InformationDiagnostics.commonMember(
                        definition,
                        null));
            }
        }
    }

    /** common Information 只允许 name 与 expression，且 body 不允许额外成员。 */
    static boolean validInformation(
            RawDefinition definition,
            InformationKey ownerKey,
            Set<Diagnostic> diagnostics) {
        boolean valid = true;
        for (String attribute : definition.attributes().keySet()) {
            if (!"name".equals(attribute) && !"expression".equals(attribute)) {
                valid = false;
            }
        }
        valid = valid
                && definition.attributes().containsKey("expression")
                && !definition.body().scalar().isPresent()
                && definition.body().children().isEmpty();
        if (!valid) {
            diagnostics.add(InformationDiagnostics.commonMember(
                    definition,
                    ownerKey));
        }
        return valid;
    }

    /** 判断 common System section 是否完全为空。 */
    private static boolean isEmptySection(RawNodeBody section) {
        return section.attributes().isEmpty()
                && !section.scalar().isPresent()
                && section.children().isEmpty();
    }
}
