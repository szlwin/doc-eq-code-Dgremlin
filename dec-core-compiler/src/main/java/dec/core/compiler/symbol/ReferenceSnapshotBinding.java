package dec.core.compiler.symbol;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;
import java.util.List;

/**
 * 校验 Resolver 输入与 SymbolTable 构建输入属于同一完整 Raw 快照。
 */
final class ReferenceSnapshotBinding {
    private static final String PASS = "reference-resolution";
    private static final SourceRef UNKNOWN_SOURCE =
            new SourceRef("<unknown-reference-source>", 0, 0, "/");

    private ReferenceSnapshotBinding() {
    }

    /** 完整值语义一致时返回 true。 */
    static boolean matches(RawDefinitionSet current, RawDefinitionSet expected) {
        return expected.equals(current);
    }

    /** 定位首个差异并创建稳定的快照不匹配 Diagnostic。 */
    static Diagnostic mismatch(
            RawDefinitionSet current,
            RawDefinitionSet expected) {
        List<RawDefinition> currentValues = current.definitions();
        List<RawDefinition> expectedValues = expected.definitions();
        int common = Math.min(currentValues.size(), expectedValues.size());
        for (int index = 0; index < common; index++) {
            RawDefinition actual = currentValues.get(index);
            RawDefinition original = expectedValues.get(index);
            if (!actual.equals(original)) {
                return diagnostic(
                        actual.sourceRef(),
                        Collections.singletonList(original.sourceRef()));
            }
        }
        if (currentValues.size() > common) {
            return diagnostic(
                    currentValues.get(common).sourceRef(),
                    Collections.<SourceRef>emptyList());
        }
        if (expectedValues.size() > common) {
            SourceRef missing = expectedValues.get(common).sourceRef();
            return diagnostic(missing, Collections.singletonList(missing));
        }
        return diagnostic(UNKNOWN_SOURCE, Collections.<SourceRef>emptyList());
    }

    private static Diagnostic diagnostic(
            SourceRef sourceRef,
            List<SourceRef> relatedRefs) {
        return new Diagnostic(
                DiagnosticCode.MIX_REF_UNKNOWN,
                DiagnosticSeverity.ERROR,
                "reference.input.snapshot-mismatch",
                null,
                sourceRef,
                relatedRefs,
                "请使用生成当前 SymbolTable 的同一完整 RawDefinitionSet 快照",
                PASS);
    }
}
