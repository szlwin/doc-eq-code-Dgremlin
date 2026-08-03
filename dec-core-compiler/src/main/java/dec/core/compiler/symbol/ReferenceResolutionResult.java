package dec.core.compiler.symbol;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticSeverity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 引用解析成功结果或不携带部分集合的稳定失败边界。
 */
public final class ReferenceResolutionResult {
    private static final String PASS = "reference-resolution";

    private final ReferenceResolutionStatus status;
    private final Optional<ResolvedReferenceSet> resolvedReferences;
    private final List<Diagnostic> diagnostics;

    private ReferenceResolutionResult(
            ReferenceResolutionStatus status,
            Optional<ResolvedReferenceSet> resolvedReferences,
            List<Diagnostic> diagnostics) {
        this.status = Objects.requireNonNull(status, "status");
        this.resolvedReferences = Objects.requireNonNull(
                resolvedReferences,
                "resolvedReferences");
        this.diagnostics = immutableDiagnostics(diagnostics);
        requireContract();
    }

    /** 创建携带完整引用集合的成功结果。 */
    public static ReferenceResolutionResult resolved(
            ResolvedReferenceSet references) {
        return new ReferenceResolutionResult(
                ReferenceResolutionStatus.RESOLVED,
                Optional.of(Objects.requireNonNull(references, "references")),
                Collections.<Diagnostic>emptyList());
    }

    /** 创建不携带部分引用集合的失败结果。 */
    public static ReferenceResolutionResult failed(
            List<Diagnostic> diagnostics) {
        return new ReferenceResolutionResult(
                ReferenceResolutionStatus.FAILED,
                Optional.<ResolvedReferenceSet>empty(),
                diagnostics);
    }

    public ReferenceResolutionStatus status() {
        return status;
    }

    public Optional<ResolvedReferenceSet> resolvedReferences() {
        return resolvedReferences;
    }

    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /**
     * 强制成功/失败互斥，并限制失败 Diagnostic 的 pass 与严重级别。
     */
    private void requireContract() {
        if (status == ReferenceResolutionStatus.RESOLVED
                && (!resolvedReferences.isPresent() || !diagnostics.isEmpty())) {
            throw new IllegalArgumentException("RESOLVED result contract violated");
        }
        if (status == ReferenceResolutionStatus.FAILED
                && (resolvedReferences.isPresent() || diagnostics.isEmpty())) {
            throw new IllegalArgumentException("FAILED result contract violated");
        }
        if (status == ReferenceResolutionStatus.FAILED) {
            for (Diagnostic diagnostic : diagnostics) {
                if (diagnostic.severity() != DiagnosticSeverity.ERROR
                        || !PASS.equals(diagnostic.pass())) {
                    throw new IllegalArgumentException(
                            "FAILED diagnostic contract violated");
                }
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ReferenceResolutionResult)) {
            return false;
        }
        ReferenceResolutionResult that = (ReferenceResolutionResult) other;
        return status == that.status
                && resolvedReferences.equals(that.resolvedReferences)
                && diagnostics.equals(that.diagnostics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, resolvedReferences, diagnostics);
    }

    @Override
    public String toString() {
        return "ReferenceResolutionResult{" + status
                + ", references=" + resolvedReferences
                + ", diagnostics=" + diagnostics + '}';
    }

    /** 防御复制、稳定排序并冻结 Diagnostic。 */
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
}
