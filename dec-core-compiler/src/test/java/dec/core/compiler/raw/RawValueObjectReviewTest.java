package dec.core.compiler.raw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * T06 独立 Review 针对公开 Raw 值对象不变量的补充 Oracle。
 */
class RawValueObjectReviewTest {

    /**
     * RawDefinitionSet 必须拒绝非 0 起始或存在间隙的 ordinal。
     */
    @Test
    void rejectsNonContinuousSourceOrdinals() {
        assertThrows(IllegalArgumentException.class,
                () -> new RawDefinitionSet(Arrays.asList(
                        definition(0L, "first"),
                        definition(2L, "third"))));
        assertThrows(IllegalArgumentException.class,
                () -> new RawDefinitionSet(Collections.singletonList(
                        definition(1L, "second"))));
    }

    /**
     * FAILED 结果必须逐项拒绝 null Diagnostic，并按 Diagnostic 自然顺序冻结。
     */
    @Test
    void freezesSortedNonNullDiagnostics() {
        assertThrows(NullPointerException.class,
                () -> RawBuildResult.failed(Collections.singletonList(null)));

        Diagnostic later = diagnostic("z.xml", 2, "raw.structure.unknown");
        Diagnostic earlier = diagnostic("a.xml", 1, "raw.document.root.unsupported");
        RawBuildResult result = RawBuildResult.failed(Arrays.asList(later, earlier));

        assertEquals(Arrays.asList(earlier, later), result.diagnostics());
        assertThrows(UnsupportedOperationException.class,
                () -> result.diagnostics().add(later));
    }

    /**
     * 创建满足其它构造约束的最小 RawDefinition。
     */
    private static RawDefinition definition(long ordinal, String name) {
        SourceRef sourceRef = new SourceRef(name + ".xml", 1, 1, "/" + name);
        RawNodeBody body = new RawNodeBody(
                "data",
                Collections.singletonMap("name", name),
                Optional.<String>empty(),
                Collections.<RawNodeBody>emptyList(),
                sourceRef);
        return new RawDefinition(
                RawDefinitionKind.DATA,
                ordinal,
                sourceRef,
                Optional.<String>empty(),
                Optional.of(name),
                Collections.singletonMap("name", name),
                Collections.<RawReference>emptyList(),
                body,
                DocumentFormat.XML,
                "1.0");
    }

    /**
     * 创建可排序的稳定 Diagnostic。
     */
    private static Diagnostic diagnostic(
            String sourceId,
            int line,
            String messageKey) {
        return new Diagnostic(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                DiagnosticSeverity.ERROR,
                messageKey,
                null,
                new SourceRef(sourceId, line, 1, "/"),
                Collections.<SourceRef>emptyList(),
                "请修复 Raw 结构",
                "raw-definition-builder");
    }
}
