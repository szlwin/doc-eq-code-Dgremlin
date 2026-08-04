package dec.core.compiler.information;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.InformationKey;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * 只使用精确 InformationKey 与 SymbolTable.find 的 owner-aware resolver。
 */
final class DefaultInformationReferenceResolver
        implements InformationReferenceResolver {

    @Override
    public InformationReferenceResolutionResult resolve(
            InformationKey owner,
            InformationExpressionAst ast,
            SymbolTable symbols,
            SourceRef sourceRef) {
        Set<InformationKey> resolved = new TreeSet<InformationKey>();
        Set<Diagnostic> diagnostics = new LinkedHashSet<Diagnostic>();
        boolean common = InformationIdentity.isCommon(owner.owner());
        for (String target : ast.references()) {
            Target parsed = Target.parse(target);
            if (parsed == null) {
                diagnostics.add(invalidTargetDiagnostic(
                        common,
                        owner,
                        sourceRef,
                        "Information 引用必须为两个非空 segment：system.name"));
                continue;
            }
            InformationKey targetKey;
            try {
                targetKey = new InformationKey(
                        new SystemKey(parsed.system),
                        parsed.name);
            } catch (IllegalArgumentException failure) {
                diagnostics.add(invalidTargetDiagnostic(
                        common,
                        owner,
                        sourceRef,
                        "Information 引用 segment 不得为空白"));
                continue;
            }
            Optional<RawDefinition> definition = symbols.find(targetKey);
            if (!definition.isPresent()
                    || definition.get().kind() != RawDefinitionKind.INFORMATION) {
                diagnostics.add(InformationDiagnostics.create(
                        DiagnosticCode.MIX_REF_UNKNOWN,
                        "information.reference.unknown",
                        targetKey,
                        sourceRef,
                        "请声明被引用的 qualified Information"));
                continue;
            }
            if (!common && !owner.owner().equals(targetKey.owner())) {
                diagnostics.add(InformationDiagnostics.create(
                        DiagnosticCode.MIX_INFORMATION_CROSS_SYSTEM,
                        "information.reference.cross-system",
                        targetKey,
                        sourceRef,
                        "普通 System expression 只能引用同 System Information"));
                continue;
            }
            resolved.add(targetKey);
        }
        if (!diagnostics.isEmpty()) {
            return InformationReferenceResolutionResult.failed(
                    InformationDiagnostics.sorted(diagnostics));
        }
        return InformationReferenceResolutionResult.resolved(
                new ArrayList<InformationKey>(resolved));
    }

    /** 创建普通或 common 的非法 lexical Diagnostic。 */
    private static Diagnostic invalidTargetDiagnostic(
            boolean common,
            InformationKey owner,
            SourceRef sourceRef,
            String hint) {
        return InformationDiagnostics.create(
                common
                        ? DiagnosticCode.MIX_COMMON_UNQUALIFIED
                        : DiagnosticCode.MIX_INFORMATION_OWNER,
                common
                        ? "information.common.reference.unqualified"
                        : "information.owner.invalid",
                owner,
                sourceRef,
                hint);
    }

    /** 严格解析恰好一个点分隔的两个非空 segment。 */
    private static final class Target {
        private final String system;
        private final String name;

        private Target(String system, String name) {
            this.system = system;
            this.name = name;
        }

        private static Target parse(String value) {
            if (value == null) {
                return null;
            }
            int separator = value.indexOf('.');
            if (separator <= 0
                    || separator != value.lastIndexOf('.')
                    || separator >= value.length() - 1) {
                return null;
            }
            String system = value.substring(0, separator);
            String name = value.substring(separator + 1);
            if (system.trim().isEmpty()
                    || name.trim().isEmpty()
                    || !system.equals(system.trim())
                    || !name.equals(name.trim())) {
                return null;
            }
            return new Target(system, name);
        }
    }
}
