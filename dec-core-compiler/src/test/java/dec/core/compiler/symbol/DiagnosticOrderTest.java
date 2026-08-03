package dec.core.compiler.symbol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * T08 多错误完整收集、去重和稳定排序 Oracle。
 */
class DiagnosticOrderTest {

    /** 所有独立错误必须在一次解析中完整收集。 */
    @Test
    void collectsAllReferenceFailuresWithoutFailFast() {
        ReferenceResolutionResult result = invalidResult();

        assertEquals(ReferenceResolutionStatus.FAILED, result.status());
        assertEquals(9, result.diagnostics().size());
        assertFalse(result.resolvedReferences().isPresent());
    }

    /** Diagnostic 必须严格复用 Context compareTo 稳定顺序。 */
    @Test
    void sortsDiagnosticsBySourceCodeAndDefinitionKey() {
        List<Diagnostic> diagnostics = invalidResult().diagnostics();
        List<Diagnostic> sorted = new ArrayList<Diagnostic>(diagnostics);
        Collections.sort(sorted);

        assertEquals(sorted, diagnostics);
    }

    /** 重复 RawReference 只保留一个完全相同的 Diagnostic。 */
    @Test
    void deduplicatesExactlyEqualDiagnostics() {
        List<Diagnostic> diagnostics = invalidResult().diagnostics();
        assertEquals(diagnostics.size(), new HashSet<Diagnostic>(diagnostics).size());
        assertEquals(1L, diagnostics.stream()
                .filter(diagnostic -> "g-business.xml"
                        .equals(diagnostic.sourceRef().sourceId()))
                .count());
    }

    /** 同一不可变输入重复解析必须产生完全相同结果。 */
    @Test
    void repeatsDeterministically() {
        RawDefinitionSet definitions = ReferenceTestFixture.invalidDefinitions();
        SymbolTable symbols = ReferenceTestFixture.symbols(definitions);
        ReferenceResolver resolver = new ReferenceResolver();

        ReferenceResolutionResult first = resolver.resolve(definitions, symbols);
        ReferenceResolutionResult second = resolver.resolve(definitions, symbols);
        assertEquals(first.status(), second.status());
        assertEquals(first.diagnostics(), second.diagnostics());
        assertEquals(first.resolvedReferences(), second.resolvedReferences());
    }

    private static ReferenceResolutionResult invalidResult() {
        RawDefinitionSet definitions = ReferenceTestFixture.invalidDefinitions();
        return new ReferenceResolver().resolve(
                definitions,
                ReferenceTestFixture.symbols(definitions));
    }
}
