package dec.core.compiler.symbol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dec.core.context.model.DataKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T07 I002：Diagnostic 去重必须保持线性插入步骤的确定性 Oracle。
 */
class DiagnosticAccumulatorReworkTest {

    /**
     * N 次不同 Diagnostic 报告必须恰好执行 N 次哈希集合插入尝试。
     */
    @Test
    void usesOneDeduplicationStepPerDiagnosticReport() {
        DiagnosticAccumulator accumulator = new DiagnosticAccumulator();
        int reportCount = 6;
        for (int index = 0; index < reportCount; index++) {
            accumulator.add(diagnostic(index));
        }

        assertEquals(reportCount, accumulator.size());
        assertEquals(reportCount, accumulator.deduplicationSteps());
    }

    /**
     * 完全相同 Diagnostic 只保留一次，但每次报告仍只有一次集合 add。
     */
    @Test
    void deduplicatesExactDiagnosticWithOneStepPerReport() {
        DiagnosticAccumulator accumulator = new DiagnosticAccumulator();
        Diagnostic diagnostic = diagnostic(1);

        accumulator.add(diagnostic);
        accumulator.add(diagnostic);

        assertEquals(1, accumulator.size());
        assertEquals(2, accumulator.deduplicationSteps());
        List<Diagnostic> values = accumulator.values();
        assertEquals(Collections.singletonList(diagnostic), values);
        assertThrows(UnsupportedOperationException.class,
                () -> values.clear());
    }

    /**
     * 创建内容互异但合同一致的 ERROR Diagnostic。
     */
    private static Diagnostic diagnostic(int index) {
        SourceRef sourceRef = new SourceRef(
                "diagnostic-" + index + ".xml",
                index + 1,
                1,
                "/definition[" + index + "]");
        return new Diagnostic(
                DiagnosticCode.MIX_SYMBOL_DUPLICATE,
                DiagnosticSeverity.ERROR,
                "symbol.duplicate",
                new DataKey("data-" + index),
                sourceRef,
                Collections.<SourceRef>emptyList(),
                "请删除重复定义",
                "symbol-registration");
    }
}
