package dec.core.compiler.symbol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T07 独立 Review 对完整两遍、上下文隔离和失败结果的负向 Oracle。
 */
class SymbolIndependentReviewTest {

    /**
     * 第一遍已有重复时仍必须完成第二遍，收集子定义重复后统一失败。
     */
    @Test
    void collectsFirstAndSecondPassDuplicatesBeforeFailure() {
        SymbolBuildResult result = new SymbolTableBuilder().build(
                new RawDefinitionSet(Arrays.asList(
                        definition(RawDefinitionKind.DATA, 0, null, "duplicate"),
                        definition(RawDefinitionKind.DATA, 1, null, "duplicate"),
                        definition(RawDefinitionKind.SYSTEM, 2, null, "system"),
                        definition(RawDefinitionKind.INFORMATION, 3,
                                "system", "status"),
                        definition(RawDefinitionKind.INFORMATION, 4,
                                "system", "status"))));

        assertEquals(SymbolBuildStatus.FAILED, result.status());
        assertFalse(result.symbolTable().isPresent());
        assertEquals(2, result.diagnostics().size());
        assertEquals(DiagnosticCode.MIX_SYMBOL_DUPLICATE,
                result.diagnostics().get(0).code());
        assertEquals(DiagnosticCode.MIX_SYMBOL_DUPLICATE,
                result.diagnostics().get(1).code());
    }

    /**
     * 进入 Business 文档后必须清除旧 System，不能把非法 Information 绑定到前文。
     */
    @Test
    void doesNotLeakSystemOwnerAcrossBusinessDocument() {
        SymbolBuildResult result = new SymbolTableBuilder().build(
                new RawDefinitionSet(Arrays.asList(
                        definition(RawDefinitionKind.SYSTEM, 0, null, "system"),
                        definition(RawDefinitionKind.BUSINESS_SCOPE, 1,
                                null, "scope"),
                        definition(RawDefinitionKind.INFORMATION, 2,
                                "system", "status"))));

        assertEquals(SymbolBuildStatus.FAILED, result.status());
        assertEquals("symbol.owner.context.invalid",
                result.diagnostics().get(0).messageKey());
    }

    /**
     * 进入 System 文档后必须清除旧 Action，不能把非法 Produce 绑定到前文。
     */
    @Test
    void doesNotLeakBusinessOwnerAcrossSystemDocument() {
        SymbolBuildResult result = new SymbolTableBuilder().build(
                new RawDefinitionSet(Arrays.asList(
                        definition(RawDefinitionKind.BUSINESS_SCOPE, 0,
                                null, "scope"),
                        definition(RawDefinitionKind.DIRECTORY, 1,
                                "scope", "directory"),
                        definition(RawDefinitionKind.ACTION, 2,
                                "directory", "action"),
                        definition(RawDefinitionKind.SYSTEM, 3,
                                null, "system"),
                        definition(RawDefinitionKind.PRODUCE, 4,
                                "directory/action", null))));

        assertEquals(SymbolBuildStatus.FAILED, result.status());
        assertEquals("symbol.owner.context.invalid",
                result.diagnostics().get(0).messageKey());
    }

    /**
     * FAILED 公共工厂必须拒绝非 ERROR 或非 symbol-registration 的诊断。
     */
    @Test
    void enforcesFailedDiagnosticBoundary() {
        SourceRef sourceRef = ref(0);
        Diagnostic warning = diagnostic(
                DiagnosticSeverity.WARNING,
                "symbol-registration",
                sourceRef);
        Diagnostic wrongPass = diagnostic(
                DiagnosticSeverity.ERROR,
                "wrong-pass",
                sourceRef);

        assertThrows(IllegalArgumentException.class,
                () -> SymbolBuildResult.failed(
                        Collections.singletonList(warning)));
        assertThrows(IllegalArgumentException.class,
                () -> SymbolBuildResult.failed(
                        Collections.singletonList(wrongPass)));
    }

    private static Diagnostic diagnostic(
            DiagnosticSeverity severity,
            String pass,
            SourceRef sourceRef) {
        return new Diagnostic(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                severity,
                "symbol.review",
                null,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                "请修复测试输入",
                pass);
    }

    private static RawDefinition definition(
            RawDefinitionKind kind,
            long ordinal,
            String owner,
            String name) {
        SourceRef sourceRef = ref(ordinal);
        return new RawDefinition(
                kind,
                ordinal,
                sourceRef,
                owner == null ? Optional.<String>empty() : Optional.of(owner),
                name == null ? Optional.<String>empty() : Optional.of(name),
                Collections.<String, String>emptyMap(),
                Collections.emptyList(),
                new RawNodeBody(
                        kind.name().toLowerCase(),
                        Collections.<String, String>emptyMap(),
                        Optional.<String>empty(),
                        Collections.<RawNodeBody>emptyList(),
                        sourceRef),
                DocumentFormat.XML,
                "1.0");
    }

    private static SourceRef ref(long ordinal) {
        return new SourceRef(
                "review-" + ordinal + ".xml",
                (int) ordinal + 1,
                1,
                "/definition[" + ordinal + "]");
    }
}
