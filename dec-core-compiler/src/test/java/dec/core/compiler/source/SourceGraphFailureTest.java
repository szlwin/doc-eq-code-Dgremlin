package dec.core.compiler.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * 验证缺失 Source、重复身份和 Provider 合同违规的稳定失败语义。
 */
class SourceGraphFailureTest {
    @Test
    void failsWhenExplicitSourceIsMissing() {
        SourceTestFixture.InMemoryProvider provider = SourceTestFixture.provider(
                SourceTestFixture.FileSetOrder.FORWARD);
        provider.removeSingle(SourceTestFixture.ORDER_RULE);

        SourceGraphResolutionResult result = resolve(provider);

        assertFailedWith(result, DiagnosticCode.MIX_SOURCE_NOT_FOUND);
    }

    @Test
    void rejectsDuplicateSourceIdWithSameOrDifferentContent() {
        DocumentSource first = SourceTestFixture.source(
                "classpath:mix/data/duplicate.xml",
                "same");
        DocumentSource same = SourceTestFixture.source(
                "classpath:mix/data/duplicate.xml",
                "same");
        DocumentSource different = SourceTestFixture.source(
                "classpath:mix/data/duplicate.xml",
                "different");

        SourceTestFixture.InMemoryProvider sameProvider = SourceTestFixture.provider(
                SourceTestFixture.FileSetOrder.FORWARD);
        sameProvider.putFileSet(
                SourceTestFixture.DATA_ROOT,
                Arrays.asList(first, same));
        assertFailedWith(
                resolve(sameProvider),
                DiagnosticCode.MIX_SOURCE_DUPLICATE_ID);

        SourceTestFixture.InMemoryProvider differentProvider =
                SourceTestFixture.provider(SourceTestFixture.FileSetOrder.FORWARD);
        differentProvider.putFileSet(
                SourceTestFixture.DATA_ROOT,
                Arrays.asList(first, different));
        assertFailedWith(
                resolve(differentProvider),
                DiagnosticCode.MIX_SOURCE_DUPLICATE_ID);
    }

    @Test
    void convertsThrowingProviderAndInvalidSingleCardinalityToPolicyFailure() {
        SourceTestFixture.InMemoryProvider throwing = SourceTestFixture.provider(
                SourceTestFixture.FileSetOrder.FORWARD);
        throwing.throwOn(SourceTestFixture.SYSTEMS);
        assertFailedWith(resolve(throwing), DiagnosticCode.MIX_SOURCE_POLICY);

        SourceTestFixture.InMemoryProvider invalidSingle = SourceTestFixture.provider(
                SourceTestFixture.FileSetOrder.FORWARD);
        invalidSingle.putSingleResult(
                SourceTestFixture.SYSTEMS,
                SourceTestFixture.customResult(
                        SourceResolutionStatus.RESOLVED,
                        Arrays.asList(
                                SourceTestFixture.source(
                                        SourceTestFixture.SYSTEMS,
                                        "<systems/>"),
                                SourceTestFixture.source(
                                        SourceTestFixture.USER_RULE,
                                        "<rule-views/>")),
                        Collections.<Diagnostic>emptyList()));
        assertFailedWith(
                resolve(invalidSingle),
                DiagnosticCode.MIX_SOURCE_POLICY);
    }

    @Test
    void convertsEmptyFileSetAndNullProviderResultToPolicyFailure() {
        SourceTestFixture.InMemoryProvider emptyFileSet =
                SourceTestFixture.provider(SourceTestFixture.FileSetOrder.FORWARD);
        emptyFileSet.putFileSet(
                SourceTestFixture.VIEW_ROOT,
                Collections.<DocumentSource>emptyList());
        assertFailedWith(
                resolve(emptyFileSet),
                DiagnosticCode.MIX_SOURCE_POLICY);

        SourceTestFixture.InMemoryProvider nullResult = SourceTestFixture.provider(
                SourceTestFixture.FileSetOrder.FORWARD);
        nullResult.putSingleResult(SourceTestFixture.BUSINESS, null);
        assertFailedWith(resolve(nullResult), DiagnosticCode.MIX_SOURCE_POLICY);
    }

    /**
     * 调用固定 root resolver。
     */
    private static SourceGraphResolutionResult resolve(
            DocumentSourceProvider provider) {
        return new MixSourceResolver().resolve(
                new SourceReference(SourceTestFixture.ROOT),
                provider,
                SourceTestFixture.policy());
    }

    /**
     * 断言失败结果包含指定错误且没有部分图。
     */
    private static void assertFailedWith(
            SourceGraphResolutionResult result,
            DiagnosticCode code) {
        assertEquals(SourceGraphResolutionStatus.FAILED, result.status());
        assertFalse(result.graph().isPresent());
        assertTrue(result.diagnostics().stream()
                .map(Diagnostic::code)
                .anyMatch(current -> current == code));
    }
}
