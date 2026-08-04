package dec.core.compiler.modelaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T10 fail-closed、歧义、重叠写和输入快照 Oracle。
 */
class ModelAccessFailureTest {

    /** ref@view 未在当前 System 声明时必须拒绝。 */
    @Test
    void rejectsViewNotDeclaredByCurrentSystem() {
        assertFailedWith(ModelAccessTestFixture.undeclaredView(),
                "MIX-REF-VIEW-NOT-DECLARED",
                "modelaccess.view.not-declared");
    }

    /** model-ref 未命中精确 ViewKey 时必须拒绝。 */
    @Test
    void rejectsUnknownSourceView() {
        assertFailedWith(ModelAccessTestFixture.unknownSourceView(),
                "MIX-MODEL-ACCESS-NOT-FOUND",
                "modelaccess.source-view.not-found");
    }

    /** selector 大小写差异不得折叠或模糊降级。 */
    @Test
    void rejectsCaseInsensitiveFallback() {
        assertFailedWith(ModelAccessTestFixture.caseMismatch(),
                "MIX-MODEL-ACCESS-NOT-FOUND",
                "modelaccess.selector.not-found");
    }

    /** property path 中间段非复合时必须返回专用 Diagnostic。 */
    @Test
    void rejectsNonCompositeIntermediateSegment() {
        assertFailedWith(ModelAccessTestFixture.nonComposite(),
                "MIX-MODEL-ACCESS-NON-COMPOSITE",
                "modelaccess.selector.non-composite");
    }

    /** 同层多个精确候选必须拒绝，不得任取一个。 */
    @Test
    void rejectsAmbiguousPropertyCandidates() {
        assertFailedWith(ModelAccessTestFixture.ambiguousProperty(),
                "MIX-MODEL-ACCESS-AMBIGUOUS",
                "modelaccess.selector.ambiguous");
    }

    /** 完全重复 ref 必须阻断整批发布。 */
    @Test
    void rejectsDuplicateReference() {
        assertFailedWith(ModelAccessTestFixture.duplicateReference(),
                "MIX-MODEL-ACCESS-AMBIGUOUS",
                "modelaccess.binding.duplicate");
    }

    /** WRITE source path 相同或祖先/后代重叠必须拒绝。 */
    @Test
    void rejectsOverlappingWritePaths() {
        assertFailedWith(ModelAccessTestFixture.overlappingWrite(),
                "MIX-MODEL-ACCESS-AMBIGUOUS",
                "modelaccess.write.overlap");
    }

    /** 输入快照不一致必须早于 resolver，且只返回一项稳定错误。 */
    @Test
    void rejectsSnapshotMismatchBeforeResolver() {
        RawDefinitionSet original = ModelAccessTestFixture.targetMainPriority();
        SymbolTable symbols = ModelAccessTestFixture.symbols(original);
        RawDefinitionSet changed = ModelAccessTestFixture.changedSnapshot(original);
        ModelAccessTestFixture.CountingCompilation counted =
                ModelAccessTestFixture.compileWithCountingResolver(changed, symbols);

        assertEquals(0, counted.calls());
        Object result = counted.result();
        assertEquals("FAILED", ModelAccessTestFixture.status(result));
        assertEquals(null, ModelAccessTestFixture.compilation(result));
        List<Diagnostic> diagnostics = ModelAccessTestFixture.diagnostics(result);
        assertEquals(1, diagnostics.size());
        assertEquals("modelaccess.input.snapshot-mismatch",
                diagnostics.get(0).messageKey());
    }

    /** 混合成功与失败定义时不得发布部分 Binding 或 Deferred。 */
    @Test
    void rejectsWholeBatchWithoutPartialPublication() {
        RawDefinitionSet definitions = ModelAccessTestFixture.mixedBatchFailure();
        Object result = ModelAccessTestFixture.compile(
                definitions,
                ModelAccessTestFixture.symbols(definitions));
        assertEquals("FAILED", ModelAccessTestFixture.status(result));
        assertEquals(null, ModelAccessTestFixture.compilation(result));
        assertFalse(ModelAccessTestFixture.diagnostics(result).isEmpty());
    }

    /** Diagnostic 必须去重、稳定排序。 */
    @Test
    void returnsStableSortedDiagnostics() {
        RawDefinitionSet definitions = ModelAccessTestFixture.mixedBatchFailure();
        Object first = ModelAccessTestFixture.compile(
                definitions,
                ModelAccessTestFixture.symbols(definitions));
        Object second = ModelAccessTestFixture.compile(
                definitions,
                ModelAccessTestFixture.symbols(definitions));
        List<Diagnostic> firstDiagnostics = ModelAccessTestFixture.diagnostics(first);
        List<Diagnostic> secondDiagnostics = ModelAccessTestFixture.diagnostics(second);
        assertEquals(firstDiagnostics, secondDiagnostics);
        List<Diagnostic> sorted = new ArrayList<Diagnostic>(firstDiagnostics);
        Collections.sort(sorted);
        assertEquals(sorted, firstDiagnostics);
    }

    /** 执行失败断言并核对精确 code/messageKey。 */
    private static void assertFailedWith(
            RawDefinitionSet definitions,
            String code,
            String messageKey) {
        Object result = ModelAccessTestFixture.compile(
                definitions,
                ModelAccessTestFixture.symbols(definitions));
        assertEquals("FAILED", ModelAccessTestFixture.status(result));
        assertEquals(null, ModelAccessTestFixture.compilation(result));
        List<Diagnostic> diagnostics = ModelAccessTestFixture.diagnostics(result);
        assertTrue(diagnostics.stream().anyMatch(diagnostic ->
                code.equals(diagnostic.code().code())
                        && messageKey.equals(diagnostic.messageKey())),
                diagnostics.toString());
    }
}
