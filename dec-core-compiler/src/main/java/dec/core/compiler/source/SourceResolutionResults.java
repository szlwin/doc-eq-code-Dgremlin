package dec.core.compiler.source;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 统一创建并验证满足 Source 解析成功或 Diagnostic 失败不变量的结果。
 */
public final class SourceResolutionResults {
    private SourceResolutionResults() {
        throw new AssertionError("No instances");
    }

    /**
     * 创建恰好携带一个文档来源的解析成功结果。
     *
     * @param source 唯一的完整 DocumentSource
     * @param diagnostics 不含 ERROR 的 Diagnostic 输入
     * @return 不可变的单 Source RESOLVED 结果
     */
    public static SourceResolutionResult resolvedSingle(
            DocumentSource source,
            List<Diagnostic> diagnostics) {
        List<DocumentSource> sources = Collections.singletonList(
                Objects.requireNonNull(source, "source"));
        return resolvedResult(
                sources,
                diagnostics,
                true);
    }

    /**
     * 创建至少携带一个文档来源的文件集解析成功结果。
     *
     * @param sources 至少包含一个完整 DocumentSource 的来源集合
     * @param diagnostics 不含 ERROR 的 Diagnostic 输入
     * @return 按 sourceId 排序且不可变的文件集 RESOLVED 结果
     */
    public static SourceResolutionResult resolvedFileSet(
            List<DocumentSource> sources,
            List<Diagnostic> diagnostics) {
        return resolvedResult(
                sources,
                diagnostics,
                false);
    }

    /**
     * 验证第三方 Provider 的单 Source 解析结果。
     *
     * <p>Provider 合同违规不会向 T03 抛出预期异常，而是转换为不携带
     * 部分 Source 的 {@code MIX-SOURCE-POLICY} 失败结果。</p>
     *
     * @param reference 本次解析引用，用于违规 Diagnostic 定位
     * @param result Provider 返回的待验证结果
     * @return 规范化不可变结果或 Source 策略失败结果
     */
    public static SourceResolutionResult validateSingle(
            SourceReference reference,
            SourceResolutionResult result) {
        return validateProviderResult(
                reference,
                result,
                true);
    }

    /**
     * 验证第三方 Provider 的文件集解析结果。
     *
     * <p>合法结果也会重新复制和排序，避免第三方结果在返回后继续修改
     * Source 或 Diagnostic 集合。</p>
     *
     * @param reference 本次解析引用，用于违规 Diagnostic 定位
     * @param result Provider 返回的待验证结果
     * @return 规范化不可变结果或 Source 策略失败结果
     */
    public static SourceResolutionResult validateFileSet(
            SourceReference reference,
            SourceResolutionResult result) {
        return validateProviderResult(
                reference,
                result,
                false);
    }

    /**
     * 创建解析失败结果；失败不得携带部分 Source 候选。
     *
     * @param diagnostics 至少包含一个 ERROR 的 Diagnostic 输入
     * @return sources 为空的 FAILED 结果
     */
    public static SourceResolutionResult failed(List<Diagnostic> diagnostics) {
        return new ImmutableSourceResolutionResult(
                SourceResolutionStatus.FAILED,
                Collections.<DocumentSource>emptyList(),
                errorDiagnostics(diagnostics));
    }

    /**
     * 创建并校验单 Source 或文件集成功结果。
     */
    private static SourceResolutionResult resolvedResult(
            List<DocumentSource> sources,
            List<Diagnostic> diagnostics,
            boolean single) {
        List<DocumentSource> sourceCopy = immutableUniqueSources(sources);
        if (single && sourceCopy.size() != 1) {
            throw new IllegalArgumentException(
                    "resolved single result must contain exactly one source");
        }
        if (!single && sourceCopy.isEmpty()) {
            throw new IllegalArgumentException(
                    "resolved file set sources must not be empty");
        }
        return new ImmutableSourceResolutionResult(
                SourceResolutionStatus.RESOLVED,
                sourceCopy,
                nonErrorDiagnostics(diagnostics));
    }

    /**
     * 防御性验证第三方 Provider 返回值并转换合同违规。
     */
    private static SourceResolutionResult validateProviderResult(
            SourceReference reference,
            SourceResolutionResult result,
            boolean single) {
        SourceReference checkedReference = Objects.requireNonNull(
                reference,
                "reference");
        try {
            SourceResolutionResult checkedResult = Objects.requireNonNull(
                    result,
                    "result");
            SourceResolutionStatus status = Objects.requireNonNull(
                    checkedResult.status(),
                    "result.status()");
            List<DocumentSource> sources = checkedResult.sources();
            List<Diagnostic> diagnostics = checkedResult.diagnostics();

            if (status == SourceResolutionStatus.RESOLVED) {
                return resolvedResult(sources, diagnostics, single);
            }
            if (status == SourceResolutionStatus.FAILED) {
                List<DocumentSource> sourceCopy = immutableUniqueSources(sources);
                if (!sourceCopy.isEmpty()) {
                    throw new IllegalArgumentException(
                            "failed result must not contain partial sources");
                }
                return failed(diagnostics);
            }
            throw new IllegalArgumentException(
                    "unsupported source resolution status: " + status);
        } catch (RuntimeException contractViolation) {
            return policyFailure(checkedReference);
        }
    }

    /**
     * 创建不泄漏部分 Source 的 Provider 合同违规失败结果。
     */
    private static SourceResolutionResult policyFailure(
            SourceReference reference) {
        Diagnostic diagnostic = new Diagnostic(
                DiagnosticCode.MIX_SOURCE_POLICY,
                DiagnosticSeverity.ERROR,
                "source.provider.contract",
                null,
                new SourceRef(reference.value(), 0, 0, "/source"),
                Collections.<SourceRef>emptyList(),
                "修复 DocumentSourceProvider 返回合同",
                "SourceResolutionContractPass");
        return failed(Collections.singletonList(diagnostic));
    }

    /**
     * 防御性复制 Source，按稳定 sourceId 排序并拒绝重复身份。
     */
    private static List<DocumentSource> immutableUniqueSources(
            List<DocumentSource> sources) {
        Objects.requireNonNull(sources, "sources");
        List<DocumentSource> copy = new ArrayList<DocumentSource>(sources.size());
        for (DocumentSource source : sources) {
            copy.add(Objects.requireNonNull(source, "sources contains null"));
        }
        Collections.sort(copy, Comparator.comparing(DocumentSource::sourceId));
        for (int index = 1; index < copy.size(); index++) {
            DocumentSource previous = copy.get(index - 1);
            DocumentSource current = copy.get(index);
            if (previous.sourceId().equals(current.sourceId())) {
                throw new IllegalArgumentException(
                        "sourceId must be unique: " + current.sourceId());
            }
        }
        return Collections.unmodifiableList(copy);
    }

    /**
     * 校验成功 Diagnostic 不得包含 ERROR。
     */
    private static List<Diagnostic> nonErrorDiagnostics(
            List<Diagnostic> diagnostics) {
        List<Diagnostic> copy = immutableDiagnostics(diagnostics);
        for (Diagnostic diagnostic : copy) {
            if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
                throw new IllegalArgumentException(
                        "resolved diagnostics must not contain ERROR entries");
            }
        }
        return copy;
    }

    /**
     * 校验失败 Diagnostic 至少包含一个 ERROR。
     */
    private static List<Diagnostic> errorDiagnostics(
            List<Diagnostic> diagnostics) {
        List<Diagnostic> copy = immutableDiagnostics(diagnostics);
        for (Diagnostic diagnostic : copy) {
            if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
                return copy;
            }
        }
        throw new IllegalArgumentException(
                "failed diagnostics must contain at least one ERROR entry");
    }

    /**
     * 防御性复制并稳定排序 Diagnostic。
     */
    private static List<Diagnostic> immutableDiagnostics(
            List<Diagnostic> diagnostics) {
        Objects.requireNonNull(diagnostics, "diagnostics");
        List<Diagnostic> copy = new ArrayList<Diagnostic>(diagnostics.size());
        for (Diagnostic diagnostic : diagnostics) {
            copy.add(Objects.requireNonNull(
                    diagnostic,
                    "diagnostics contains null"));
        }
        Collections.sort(copy);
        return Collections.unmodifiableList(copy);
    }

    /**
     * SourceResolutionResult 的私有不可变实现。
     */
    private static final class ImmutableSourceResolutionResult
            implements SourceResolutionResult {
        private final SourceResolutionStatus status;
        private final List<DocumentSource> sources;
        private final List<Diagnostic> diagnostics;

        private ImmutableSourceResolutionResult(
                SourceResolutionStatus status,
                List<DocumentSource> sources,
                List<Diagnostic> diagnostics) {
            this.status = Objects.requireNonNull(status, "status");
            this.sources = Objects.requireNonNull(sources, "sources");
            this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
        }

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

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ImmutableSourceResolutionResult)) {
                return false;
            }
            ImmutableSourceResolutionResult that =
                    (ImmutableSourceResolutionResult) other;
            return status == that.status
                    && sources.equals(that.sources)
                    && diagnostics.equals(that.diagnostics);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, sources, diagnostics);
        }

        @Override
        public String toString() {
            return "SourceResolutionResult{"
                    + "status=" + status
                    + ", sources=" + sources
                    + ", diagnostics=" + diagnostics
                    + '}';
        }
    }
}
