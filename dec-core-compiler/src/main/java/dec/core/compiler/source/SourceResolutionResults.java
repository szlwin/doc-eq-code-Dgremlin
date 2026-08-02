package dec.core.compiler.source;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticSeverity;
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
        return new ImmutableSourceResolutionResult(
                SourceResolutionStatus.RESOLVED,
                immutableUniqueSources(sources),
                nonErrorDiagnostics(diagnostics));
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
        List<DocumentSource> sourceCopy = immutableUniqueSources(sources);
        if (sourceCopy.isEmpty()) {
            throw new IllegalArgumentException(
                    "resolved file set sources must not be empty");
        }
        return new ImmutableSourceResolutionResult(
                SourceResolutionStatus.RESOLVED,
                sourceCopy,
                nonErrorDiagnostics(diagnostics));
    }

    /**
     * 验证第三方 Provider 的单 Source 解析结果。
     *
     * @param reference 本次解析引用，用于违规 Diagnostic 定位
     * @param result Provider 返回的待验证结果
     * @return 规范化结果；Architecture Skeleton 阶段暂不实现
     */
    public static SourceResolutionResult validateSingle(
            SourceReference reference,
            SourceResolutionResult result) {
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(result, "result");
        throw new AssertionError("Architecture skeleton only");
    }

    /**
     * 验证第三方 Provider 的文件集解析结果。
     *
     * @param reference 本次解析引用，用于违规 Diagnostic 定位
     * @param result Provider 返回的待验证结果
     * @return 规范化结果；Architecture Skeleton 阶段暂不实现
     */
    public static SourceResolutionResult validateFileSet(
            SourceReference reference,
            SourceResolutionResult result) {
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(result, "result");
        throw new AssertionError("Architecture skeleton only");
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
