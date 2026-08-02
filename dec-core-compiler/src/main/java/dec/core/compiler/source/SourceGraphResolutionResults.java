package dec.core.compiler.source;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticSeverity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 统一创建满足候选隔离不变量的 SourceGraph 结果。
 */
public final class SourceGraphResolutionResults {
    private SourceGraphResolutionResults() {
        throw new AssertionError("No instances");
    }

    /**
     * 创建包含一个完整图且不含 ERROR 的成功结果。
     */
    public static SourceGraphResolutionResult resolved(
            MixSourceGraph graph,
            List<Diagnostic> diagnostics) {
        List<Diagnostic> copy = immutableDiagnostics(diagnostics);
        for (Diagnostic diagnostic : copy) {
            if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
                throw new IllegalArgumentException(
                        "resolved diagnostics must not contain ERROR entries");
            }
        }
        return new ImmutableSourceGraphResolutionResult(
                SourceGraphResolutionStatus.RESOLVED,
                Optional.of(Objects.requireNonNull(graph, "graph")),
                copy);
    }

    /**
     * 创建不暴露部分图且至少包含一个 ERROR 的失败结果。
     */
    public static SourceGraphResolutionResult failed(
            List<Diagnostic> diagnostics) {
        List<Diagnostic> copy = immutableDiagnostics(diagnostics);
        boolean hasError = false;
        for (Diagnostic diagnostic : copy) {
            if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
                hasError = true;
                break;
            }
        }
        if (!hasError) {
            throw new IllegalArgumentException(
                    "failed diagnostics must contain at least one ERROR entry");
        }
        return new ImmutableSourceGraphResolutionResult(
                SourceGraphResolutionStatus.FAILED,
                Optional.<MixSourceGraph>empty(),
                copy);
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
     * SourceGraphResolutionResult 的私有不可变实现。
     */
    private static final class ImmutableSourceGraphResolutionResult
            implements SourceGraphResolutionResult {
        private final SourceGraphResolutionStatus status;
        private final Optional<MixSourceGraph> graph;
        private final List<Diagnostic> diagnostics;

        private ImmutableSourceGraphResolutionResult(
                SourceGraphResolutionStatus status,
                Optional<MixSourceGraph> graph,
                List<Diagnostic> diagnostics) {
            this.status = status;
            this.graph = graph;
            this.diagnostics = diagnostics;
        }

        @Override
        public SourceGraphResolutionStatus status() {
            return status;
        }

        @Override
        public Optional<MixSourceGraph> graph() {
            return graph;
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
            if (!(other instanceof ImmutableSourceGraphResolutionResult)) {
                return false;
            }
            ImmutableSourceGraphResolutionResult that =
                    (ImmutableSourceGraphResolutionResult) other;
            return status == that.status
                    && graph.equals(that.graph)
                    && diagnostics.equals(that.diagnostics);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, graph, diagnostics);
        }

        @Override
        public String toString() {
            return "SourceGraphResolutionResult{"
                    + "status=" + status
                    + ", graph=" + graph
                    + ", diagnostics=" + diagnostics
                    + '}';
        }
    }
}
