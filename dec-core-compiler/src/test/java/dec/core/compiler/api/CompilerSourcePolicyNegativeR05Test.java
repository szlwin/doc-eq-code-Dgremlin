package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.DocumentSource;
import dec.core.compiler.source.SourceReference;
import dec.core.compiler.source.SourceResolutionResult;
import dec.core.compiler.source.SourceResolutionResults;
import dec.core.compiler.source.SourceResolutionStatus;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 补充验证原始 DocumentSource URI 和第三方 Provider 结果的负向闭包。
 */
class CompilerSourcePolicyNegativeR05Test {
    @Test
    void documentSourceRejectsTraversalBeforeNormalizingItsUri() {
        AllowedRoot root = new AllowedRoot(
                URI.create("file:///workspace/config/"));

        assertThrows(
                IllegalArgumentException.class,
                () -> new DocumentSource(
                        "source:traversal",
                        URI.create(
                                "file:///workspace/config/nested/../mix.xml"),
                        DocumentFormat.XML,
                        root,
                        new byte[] {1},
                        "sha256:traversal"));
        assertThrows(
                IllegalArgumentException.class,
                () -> new AllowedRoot(
                        URI.create("classpath:config/%3Fraw=true")));
    }

    @Test
    void validatorConvertsEmptySingleAndResolvedErrorToPolicyFailure() {
        SourceReference reference = new SourceReference(
                "file:/workspace/config/root.xml");
        SourceResolutionResult emptySingle = SourceResolutionResults.validateSingle(
                reference,
                customResult(
                        SourceResolutionStatus.RESOLVED,
                        Collections.<DocumentSource>emptyList(),
                        Collections.<Diagnostic>emptyList()));
        assertPolicyFailure(emptySingle);

        SourceResolutionResult resolvedWithError =
                SourceResolutionResults.validateSingle(
                        reference,
                        customResult(
                                SourceResolutionStatus.RESOLVED,
                                Collections.singletonList(source(
                                        "source:root",
                                        "root.xml")),
                                Collections.singletonList(diagnostic(
                                        DiagnosticSeverity.ERROR))));
        assertPolicyFailure(resolvedWithError);
    }

    @Test
    void validatorConvertsFailedWithoutErrorAndNullResultToPolicyFailure() {
        SourceReference reference = new SourceReference(
                "file:/workspace/config/");
        SourceResolutionResult failedWithoutError =
                SourceResolutionResults.validateFileSet(
                        reference,
                        customResult(
                                SourceResolutionStatus.FAILED,
                                Collections.<DocumentSource>emptyList(),
                                Collections.singletonList(diagnostic(
                                        DiagnosticSeverity.WARNING))));
        assertPolicyFailure(failedWithoutError);

        SourceResolutionResult nullResult = SourceResolutionResults.validateSingle(
                reference,
                null);
        assertPolicyFailure(nullResult);
    }

    @Test
    void validatorDefensivelyCopiesAndSortsValidThirdPartyResult() {
        DocumentSource later = source("source:z", "z.xml");
        DocumentSource earlier = source("source:a", "a.xml");
        List<DocumentSource> mutableSources = new ArrayList<DocumentSource>(
                Arrays.asList(later, earlier));
        List<Diagnostic> mutableDiagnostics = new ArrayList<Diagnostic>(
                Collections.singletonList(diagnostic(
                        DiagnosticSeverity.WARNING)));

        SourceResolutionResult validated = SourceResolutionResults.validateFileSet(
                new SourceReference("file:/workspace/config/"),
                customResult(
                        SourceResolutionStatus.RESOLVED,
                        mutableSources,
                        mutableDiagnostics));
        mutableSources.clear();
        mutableDiagnostics.clear();

        assertEquals(SourceResolutionStatus.RESOLVED, validated.status());
        assertEquals(Arrays.asList(earlier, later), validated.sources());
        assertEquals(1, validated.diagnostics().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> validated.sources().clear());
        assertThrows(
                UnsupportedOperationException.class,
                () -> validated.diagnostics().clear());
    }

    /**
     * 断言 Provider 合同违规被转换为无部分候选的策略失败。
     */
    private static void assertPolicyFailure(SourceResolutionResult result) {
        assertEquals(SourceResolutionStatus.FAILED, result.status());
        assertTrue(result.sources().isEmpty());
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic ->
                        diagnostic.code() == DiagnosticCode.MIX_SOURCE_POLICY
                                && diagnostic.severity()
                                == DiagnosticSeverity.ERROR));
    }

    /**
     * 创建位于统一允许根中的完整文档来源。
     */
    private static DocumentSource source(String sourceId, String fileName) {
        AllowedRoot root = new AllowedRoot(
                URI.create("file:///workspace/config/"));
        return new DocumentSource(
                sourceId,
                URI.create("file:///workspace/config/" + fileName),
                DocumentFormat.XML,
                root,
                new byte[] {1},
                "sha256:" + sourceId);
    }

    /**
     * 创建用于模拟第三方 Provider 的可变解析结果。
     */
    private static SourceResolutionResult customResult(
            final SourceResolutionStatus status,
            final List<DocumentSource> sources,
            final List<Diagnostic> diagnostics) {
        return new SourceResolutionResult() {
            @Override
            public SourceResolutionStatus status() {
                return status;
            }

            @Override
            public List<DocumentSource> sources() {
                return sources;
            }

            @Override
            public List<Diagnostic> diagnostics() {
                return diagnostics;
            }
        };
    }

    /**
     * 创建 Source Provider 合同测试使用的 Diagnostic。
     */
    private static Diagnostic diagnostic(DiagnosticSeverity severity) {
        return new Diagnostic(
                DiagnosticCode.MIX_SOURCE_POLICY,
                severity,
                "source.provider.contract.test",
                null,
                new SourceRef("source:test", 1, 1, "/source"),
                Collections.<SourceRef>emptyList(),
                "修复 DocumentSourceProvider 返回合同",
                "SourceResolutionContractPass");
    }
}
