package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.DocumentSource;
import dec.core.compiler.source.SourceResolutionResult;
import dec.core.compiler.source.SourceResolutionResults;
import dec.core.compiler.source.SourceResolutionStatus;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * 验证 SourceResolutionResult 的成功、失败和候选隔离不变量。
 */
class CompilerSourceResolutionBehaviorR04Test {
    @Test
    void resolvedFileSetSortsSourcesAndRejectsErrorDiagnostics() {
        DocumentSource later = source("source:z", "z.xml");
        DocumentSource earlier = source("source:a", "a.xml");
        Diagnostic warning = diagnostic(DiagnosticSeverity.WARNING);

        SourceResolutionResult result = SourceResolutionResults.resolvedFileSet(
                Arrays.asList(later, earlier),
                Collections.singletonList(warning));

        assertEquals(SourceResolutionStatus.RESOLVED, result.status());
        assertEquals(Arrays.asList(earlier, later), result.sources());
        assertEquals(Collections.singletonList(warning), result.diagnostics());
        assertThrows(
                UnsupportedOperationException.class,
                () -> result.sources().add(later));
        assertThrows(
                IllegalArgumentException.class,
                () -> SourceResolutionResults.resolvedSingle(
                        earlier,
                        Collections.singletonList(
                                diagnostic(DiagnosticSeverity.ERROR))));
    }

    @Test
    void failedResultCarriesNoSourceAndRequiresErrorDiagnostic() {
        Diagnostic error = diagnostic(DiagnosticSeverity.ERROR);
        SourceResolutionResult result = SourceResolutionResults.failed(
                Collections.singletonList(error));

        assertEquals(SourceResolutionStatus.FAILED, result.status());
        assertTrue(result.sources().isEmpty());
        assertEquals(Collections.singletonList(error), result.diagnostics());
        assertThrows(
                IllegalArgumentException.class,
                () -> SourceResolutionResults.failed(
                        Collections.singletonList(
                                diagnostic(DiagnosticSeverity.WARNING))));
    }

    @Test
    void resolvedFileSetRejectsEmptyOrNullSourceCandidates() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SourceResolutionResults.resolvedFileSet(
                        Collections.<DocumentSource>emptyList(),
                        Collections.<Diagnostic>emptyList()));
        assertThrows(
                NullPointerException.class,
                () -> SourceResolutionResults.resolvedFileSet(
                        Collections.<DocumentSource>singletonList(null),
                        Collections.<Diagnostic>emptyList()));
    }

    /**
     * 创建位于同一允许根中的完整文档来源。
     */
    private static DocumentSource source(String sourceId, String fileName) {
        AllowedRoot allowedRoot = new AllowedRoot(
                URI.create("file:///workspace/config/"));
        return new DocumentSource(
                sourceId,
                URI.create("file:///workspace/config/" + fileName),
                DocumentFormat.XML,
                allowedRoot,
                new byte[] {1},
                "sha256:" + sourceId);
    }

    /**
     * 创建用于结果不变量验证的 Diagnostic。
     */
    private static Diagnostic diagnostic(DiagnosticSeverity severity) {
        return new Diagnostic(
                DiagnosticCode.MIX_SOURCE_POLICY,
                severity,
                "source.test",
                null,
                new SourceRef("source:test", 1, 1, "/source"),
                Collections.<SourceRef>emptyList(),
                "Review source resolution policy",
                "SourceResolutionPass");
    }
}
