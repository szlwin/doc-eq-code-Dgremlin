package dec.core.compiler.source;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticSeverity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 统一创建满足 Source 解析成功或 Diagnostic 失败不变量的结果。
 */
public final class SourceResolutionResults {
    private SourceResolutionResults() {
        throw new AssertionError("No instances");
    }

    /**
     * 创建解析成功结果。
     *
     * @param sources 至少包含一个完整 DocumentSource 的来源集合
     * @param diagnostics 不含 ERROR 的 Diagnostic 输入
     * @return 稳定排序且不可变的 RESOLVED 结果
     */
    public static SourceResolutionResult resolved(
            List<DocumentSource> sources,
            List<Diagnostic> diagnostics) {
        List<DocumentSource> sourceCopy = immutableSources(sources);
        if (sourceCopy.isEmpty()) {
            throw new IllegalArgumentException(
                    "resolved sources must not be empty");
        }
        return new ImmutableSourceResolutionResult(
                SourceResolutionStatus.RESOLVED,
                sourceCopy,
                nonErrorDiagnostics(diagnostics));
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
     * 防御性复制 Source，并按稳定 sourceId 排序。
     */
    private static List<DocumentSource> immutableSources(
            List<DocumentSource> sources) {
        Objects.requireNonNull(sources, "sources");
        List<DocumentSource> copy = new ArrayList<DocumentSource>(sources.size());
        for (DocumentSource source : sources) {
            copy.add(Objects.requireNonNull(source, "sources contains null"));
        }
        Collections.sort(copy, Comparator.comparing(DocumentSource::sourceId));
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
