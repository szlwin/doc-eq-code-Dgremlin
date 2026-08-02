package dec.core.compiler.canonical;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticSeverity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 统一创建满足 Canonical 成功或 Diagnostic 失败不变量的 Frontend 结果。
 */
public final class FrontendResults {
    private FrontendResults() {
        throw new AssertionError("No instances");
    }

    /**
     * 创建成功解析结果。
     *
     * <p>Architecture Skeleton 阶段先冻结签名和输入校验，实际结果构造
     * 在 Development 阶段完成。</p>
     *
     * @param canonicalRoot 唯一 Canonical 根节点
     * @param diagnostics 不含 ERROR 的稳定 Diagnostic 输入
     * @return 成功 Frontend 结果
     */
    public static FrontendResult parsed(
            CanonicalDocumentNode canonicalRoot,
            List<Diagnostic> diagnostics) {
        Objects.requireNonNull(canonicalRoot, "canonicalRoot");
        parsedDiagnostics(diagnostics);
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    /**
     * 创建失败解析结果；失败不得携带 Canonical 候选。
     *
     * @param diagnostics 至少包含一个 ERROR 的 Diagnostic 输入
     * @return 失败 Frontend 结果
     */
    public static FrontendResult failed(List<Diagnostic> diagnostics) {
        return new ImmutableFrontendResult(
                FrontendStatus.FAILED,
                Optional.<CanonicalDocumentNode>empty(),
                failedDiagnostics(diagnostics));
    }

    /**
     * 校验成功 Diagnostic 不得包含 ERROR，并返回稳定不可变副本。
     */
    private static List<Diagnostic> parsedDiagnostics(
            List<Diagnostic> diagnostics) {
        List<Diagnostic> copy = immutableDiagnostics(diagnostics);
        for (Diagnostic diagnostic : copy) {
            if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
                throw new IllegalArgumentException(
                        "parsed diagnostics must not contain ERROR entries");
            }
        }
        return copy;
    }

    /**
     * 校验失败 Diagnostic 至少包含一个 ERROR。
     */
    private static List<Diagnostic> failedDiagnostics(
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
     * 防御性复制并按 Diagnostic 公共顺序稳定排序。
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
     * FrontendResult 的私有不可变实现，避免暴露额外候选状态。
     */
    private static final class ImmutableFrontendResult implements FrontendResult {
        private final FrontendStatus status;
        private final Optional<CanonicalDocumentNode> canonicalRoot;
        private final List<Diagnostic> diagnostics;

        private ImmutableFrontendResult(
                FrontendStatus status,
                Optional<CanonicalDocumentNode> canonicalRoot,
                List<Diagnostic> diagnostics) {
            this.status = Objects.requireNonNull(status, "status");
            this.canonicalRoot = Objects.requireNonNull(
                    canonicalRoot,
                    "canonicalRoot");
            this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
        }

        @Override
        public FrontendStatus status() {
            return status;
        }

        @Override
        public Optional<CanonicalDocumentNode> canonicalRoot() {
            return canonicalRoot;
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
            if (!(other instanceof ImmutableFrontendResult)) {
                return false;
            }
            ImmutableFrontendResult that = (ImmutableFrontendResult) other;
            return status == that.status
                    && canonicalRoot.equals(that.canonicalRoot)
                    && diagnostics.equals(that.diagnostics);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, canonicalRoot, diagnostics);
        }

        @Override
        public String toString() {
            return "FrontendResult{"
                    + "status=" + status
                    + ", canonicalRoot=" + canonicalRoot
                    + ", diagnostics=" + diagnostics
                    + '}';
        }
    }
}
