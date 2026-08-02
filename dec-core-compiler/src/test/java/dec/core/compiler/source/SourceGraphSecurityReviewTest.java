package dec.core.compiler.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.DiagnosticCode;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * 独立 Review 补充的 SourceGraph 安全、环路和不可变性负向 Oracle。
 */
class SourceGraphSecurityReviewTest {
    @Test
    void rejectsEncodedTraversalAndOpaqueQueryBeforeProviderAccess() {
        SourceTestFixture.InMemoryProvider encodedProvider =
                SourceTestFixture.provider(SourceTestFixture.FileSetOrder.FORWARD);
        SourceGraphResolutionResult encoded = new MixSourceResolver().resolve(
                new SourceReference("classpath:mix/%2e%2e/secret.xml"),
                encodedProvider,
                SourceTestFixture.policy());
        assertFailedWith(encoded, DiagnosticCode.MIX_SOURCE_PATH_ESCAPE);
        assertEquals(0, encodedProvider.accessCount());

        SourceTestFixture.InMemoryProvider queryProvider =
                SourceTestFixture.provider(SourceTestFixture.FileSetOrder.FORWARD);
        SourceGraphResolutionResult query = new MixSourceResolver().resolve(
                new SourceReference(
                        "classpath:mix/orm-config.xml?raw=true"),
                queryProvider,
                SourceTestFixture.policy());
        assertFailedWith(query, DiagnosticCode.MIX_SOURCE_PATH_ESCAPE);
        assertEquals(0, queryProvider.accessCount());
    }

    @Test
    void convertsNullProviderToPolicyFailureWithoutPartialGraph() {
        SourceGraphResolutionResult result = new MixSourceResolver().resolve(
                new SourceReference(SourceTestFixture.ROOT),
                null,
                SourceTestFixture.policy());

        assertFailedWith(result, DiagnosticCode.MIX_SOURCE_POLICY);
    }

    @Test
    void rejectsDtdAndExternalEntityDeclarations() {
        SourceTestFixture.InMemoryProvider provider = SourceTestFixture.provider(
                SourceTestFixture.FileSetOrder.FORWARD);
        provider.putSingle(SourceTestFixture.source(
                SourceTestFixture.ROOT,
                "<!DOCTYPE orm-config SYSTEM \"https://example.invalid/source.dtd\">"
                        + "<orm-config/>"));

        SourceGraphResolutionResult result = resolve(provider);

        assertFailedWith(result, DiagnosticCode.MIX_SOURCE_POLICY);
        assertEquals(1, provider.accessCount());
    }

    @Test
    void rejectsDuplicateRootDeclarationAndAncestorCycle() {
        SourceTestFixture.InMemoryProvider duplicate = SourceTestFixture.provider(
                SourceTestFixture.FileSetOrder.FORWARD);
        duplicate.putSingle(SourceTestFixture.source(
                SourceTestFixture.ROOT,
                rootXml(
                        SourceTestFixture.SYSTEMS,
                        "<orm-file path=\"" + SourceTestFixture.DATA_ROOT
                                + "\"/>")));
        assertFailedWith(resolve(duplicate), DiagnosticCode.MIX_SOURCE_POLICY);

        SourceTestFixture.InMemoryProvider cycle = SourceTestFixture.provider(
                SourceTestFixture.FileSetOrder.FORWARD);
        cycle.putSingle(SourceTestFixture.source(
                SourceTestFixture.ROOT,
                rootXml(SourceTestFixture.ROOT, "")));
        SourceGraphResolutionResult cycleResult = resolve(cycle);
        assertFailedWith(cycleResult, DiagnosticCode.MIX_SOURCE_POLICY);
        assertEquals(3, cycle.accessCount());
    }

    @Test
    void publishesOnlyImmutableSuccessfulGraphCollections() {
        SourceGraphResolutionResult result = resolve(
                SourceTestFixture.provider(
                        SourceTestFixture.FileSetOrder.SHUFFLED));
        assertEquals(SourceGraphResolutionStatus.RESOLVED, result.status());
        assertTrue(result.graph().isPresent());

        MixSourceGraph graph = result.graph().get();
        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.manifest().sources().clear());
        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.manifest().sourceIds().clear());
        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.edges().clear());
        assertThrows(
                UnsupportedOperationException.class,
                () -> result.diagnostics().clear());
    }

    /**
     * 创建可注入重复声明或 system 环路的完整 root XML。
     */
    private static String rootXml(String systemPath, String extraDataDeclaration) {
        return "<orm-config>"
                + "<orm-data-file-info>"
                + "<orm-file path=\"" + SourceTestFixture.DATA_ROOT + "\"/>"
                + extraDataDeclaration
                + "</orm-data-file-info>"
                + "<orm-view-file-info><orm-file path=\""
                + SourceTestFixture.VIEW_ROOT
                + "\"/></orm-view-file-info>"
                + "<system-file-info><system-file path=\""
                + systemPath
                + "\"/></system-file-info>"
                + "<business-file-info><business-file path=\""
                + SourceTestFixture.BUSINESS
                + "\"/></business-file-info>"
                + "</orm-config>";
    }

    /**
     * 解析固定 root。
     */
    private static SourceGraphResolutionResult resolve(
            DocumentSourceProvider provider) {
        return new MixSourceResolver().resolve(
                new SourceReference(SourceTestFixture.ROOT),
                provider,
                SourceTestFixture.policy());
    }

    /**
     * 断言失败结果包含指定 ERROR 且不暴露部分图。
     */
    private static void assertFailedWith(
            SourceGraphResolutionResult result,
            DiagnosticCode code) {
        assertEquals(SourceGraphResolutionStatus.FAILED, result.status());
        assertFalse(result.graph().isPresent());
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.code() == code));
    }
}
