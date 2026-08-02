package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 冻结 TASK-P1-T02 I005 的 URI 安全根与 SourceResolution 负向合同。
 */
class CompilerSourcePolicyR05Test {
    @Test
    void rejectsTraversalBeforeRootNormalizationCanExpandTheBoundary() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AllowedRoot(
                        URI.create("file:///workspace/config/..")));
        assertThrows(
                IllegalArgumentException.class,
                () -> new AllowedRoot(
                        URI.create("file:///workspace/config/../secret")));
        assertThrows(
                IllegalArgumentException.class,
                () -> new AllowedRoot(
                        URI.create("file:///workspace/config/%2e%2e/secret")));
    }

    @Test
    void rejectsOpaqueQueryInRootAndCandidate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AllowedRoot(
                        URI.create("classpath:config/?raw=true")));

        AllowedRoot root = new AllowedRoot(URI.create("classpath:config"));
        assertFalse(root.contains(
                URI.create("classpath:config/root.xml?raw=true")));
        assertTrue(root.contains(
                URI.create("classpath:config/root.xml")));
    }

    @Test
    void exposesExplicitResolutionFactoriesAndRemovesAmbiguousFactory()
            throws Exception {
        assertEquals(
                SourceResolutionResult.class,
                SourceResolutionResults.class.getMethod(
                        "resolvedSingle",
                        DocumentSource.class,
                        List.class).getReturnType());
        assertEquals(
                SourceResolutionResult.class,
                SourceResolutionResults.class.getMethod(
                        "resolvedFileSet",
                        List.class,
                        List.class).getReturnType());
        assertEquals(
                SourceResolutionResult.class,
                SourceResolutionResults.class.getMethod(
                        "validateSingle",
                        SourceReference.class,
                        SourceResolutionResult.class).getReturnType());
        assertEquals(
                SourceResolutionResult.class,
                SourceResolutionResults.class.getMethod(
                        "validateFileSet",
                        SourceReference.class,
                        SourceResolutionResult.class).getReturnType());
        assertThrows(
                NoSuchMethodException.class,
                () -> SourceResolutionResults.class.getMethod(
                        "resolved",
                        List.class,
                        List.class));
    }

    @Test
    void resolvedSingleAlwaysContainsExactlyOneSource() {
        DocumentSource source = source(
                "source:single",
                "single.xml",
                "sha256:single");

        SourceResolutionResult result = invokeResult(
                "resolvedSingle",
                new Class<?>[] {DocumentSource.class, List.class},
                source,
                Collections.<Diagnostic>emptyList());

        assertEquals(SourceResolutionStatus.RESOLVED, result.status());
        assertEquals(Collections.singletonList(source), result.sources());
        assertTrue(result.diagnostics().isEmpty());
    }

    @Test
    void resolvedFileSetSortsUniqueIdsAndRejectsEveryDuplicate() {
        DocumentSource later = source(
                "source:z",
                "z.xml",
                "sha256:z");
        DocumentSource earlier = source(
                "source:a",
                "a.xml",
                "sha256:a");

        SourceResolutionResult result = invokeResult(
                "resolvedFileSet",
                new Class<?>[] {List.class, List.class},
                Arrays.asList(later, earlier),
                Collections.<Diagnostic>emptyList());

        assertEquals(Arrays.asList(earlier, later), result.sources());
        assertThrows(
                IllegalArgumentException.class,
                () -> invokeResult(
                        "resolvedFileSet",
                        new Class<?>[] {List.class, List.class},
                        Arrays.asList(earlier, earlier),
                        Collections.<Diagnostic>emptyList()));
        assertThrows(
                IllegalArgumentException.class,
                () -> invokeResult(
                        "resolvedFileSet",
                        new Class<?>[] {List.class, List.class},
                        Arrays.asList(
                                earlier,
                                source(
                                        "source:a",
                                        "conflict.xml",
                                        "sha256:conflict")),
                        Collections.<Diagnostic>emptyList()));
    }

    @Test
    void validatorsConvertSingleCardinalityViolationsToPolicyFailure() {
        final DocumentSource first = source(
                "source:first",
                "first.xml",
                "sha256:first");
        final DocumentSource second = source(
                "source:second",
                "second.xml",
                "sha256:second");
        SourceResolutionResult invalid = customResult(
                SourceResolutionStatus.RESOLVED,
                Arrays.asList(first, second),
                Collections.<Diagnostic>emptyList());

        SourceResolutionResult validated = invokeResult(
                "validateSingle",
                new Class<?>[] {
                    SourceReference.class,
                    SourceResolutionResult.class
                },
                new SourceReference("file:/workspace/config/root.xml"),
                invalid);

        assertPolicyFailure(validated);
    }

    @Test
    void validatorsConvertDuplicateFileSetAndPartialFailureToPolicyFailure() {
        final DocumentSource first = source(
                "source:duplicate",
                "first.xml",
                "sha256:first");
        final DocumentSource conflict = source(
                "source:duplicate",
                "second.xml",
                "sha256:second");
        SourceReference reference = new SourceReference(
                "file:/workspace/config/");

        SourceResolutionResult duplicate = customResult(
                SourceResolutionStatus.RESOLVED,
                Arrays.asList(first, conflict),
                Collections.<Diagnostic>emptyList());
        SourceResolutionResult duplicateValidated = invokeResult(
                "validateFileSet",
                new Class<?>[] {
                    SourceReference.class,
                    SourceResolutionResult.class
                },
                reference,
                duplicate);
        assertPolicyFailure(duplicateValidated);

        SourceResolutionResult partialFailure = customResult(
                SourceResolutionStatus.FAILED,
                Collections.singletonList(first),
                Collections.singletonList(
                        diagnostic(DiagnosticSeverity.ERROR)));
        SourceResolutionResult failureValidated = invokeResult(
                "validateFileSet",
                new Class<?>[] {
                    SourceReference.class,
                    SourceResolutionResult.class
                },
                reference,
                partialFailure);
        assertPolicyFailure(failureValidated);
    }

    @Test
    void successfulFactoriesRejectErrorDiagnostics() {
        final DocumentSource source = source(
                "source:error",
                "error.xml",
                "sha256:error");
        assertThrows(
                IllegalArgumentException.class,
                () -> invokeResult(
                        "resolvedSingle",
                        new Class<?>[] {DocumentSource.class, List.class},
                        source,
                        Collections.singletonList(
                                diagnostic(DiagnosticSeverity.ERROR))));
    }

    /**
     * 调用待冻结的公共工厂，并把反射目标异常恢复为原始运行时异常。
     */
    private static SourceResolutionResult invokeResult(
            String methodName,
            Class<?>[] parameterTypes,
            Object... arguments) {
        try {
            Method method = SourceResolutionResults.class.getMethod(
                    methodName,
                    parameterTypes);
            return (SourceResolutionResult) method.invoke(null, arguments);
        } catch (NoSuchMethodException exception) {
            throw new AssertionError(
                    "Missing frozen method: " + methodName,
                    exception);
        } catch (IllegalAccessException exception) {
            throw new AssertionError(
                    "Frozen method is not accessible: " + methodName,
                    exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new AssertionError(
                    "Unexpected checked exception from: " + methodName,
                    cause);
        }
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
     * 创建具有稳定来源身份的完整文档来源。
     */
    private static DocumentSource source(
            String sourceId,
            String fileName,
            String digest) {
        AllowedRoot allowedRoot = new AllowedRoot(
                URI.create("file:///workspace/config/"));
        return new DocumentSource(
                sourceId,
                URI.create("file:///workspace/config/" + fileName),
                DocumentFormat.XML,
                allowedRoot,
                new byte[] {1},
                digest);
    }

    /**
     * 创建用于模拟第三方 Provider 的自定义解析结果。
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
     * 创建 Source 策略测试使用的 Diagnostic。
     */
    private static Diagnostic diagnostic(DiagnosticSeverity severity) {
        return new Diagnostic(
                DiagnosticCode.MIX_SOURCE_POLICY,
                severity,
                "source.policy.test",
                null,
                new SourceRef("source:test", 1, 1, "/source"),
                Collections.<SourceRef>emptyList(),
                "修复 DocumentSourceProvider 返回合同",
                "SourceResolutionContractPass");
    }
}
