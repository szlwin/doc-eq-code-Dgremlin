package dec.core.compiler.raw;

import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * RawDefinitionSet 成功结果或稳定失败 Diagnostic 的不可变边界。
 */
public final class RawBuildResult {
    private final RawBuildStatus status;
    private final Optional<RawDefinitionSet> rawDefinitionSet;
    private final List<Diagnostic> diagnostics;

    private RawBuildResult(
            RawBuildStatus status,
            Optional<RawDefinitionSet> rawDefinitionSet,
            List<Diagnostic> diagnostics) {
        this.status = Objects.requireNonNull(status, "status");
        this.rawDefinitionSet = Objects.requireNonNull(
                rawDefinitionSet,
                "rawDefinitionSet");
        this.diagnostics = immutableDiagnostics(diagnostics);
        if (status == RawBuildStatus.BUILT
                && (!rawDefinitionSet.isPresent() || !this.diagnostics.isEmpty())) {
            throw new IllegalArgumentException("BUILT result contract violated");
        }
        if (status == RawBuildStatus.FAILED
                && (rawDefinitionSet.isPresent() || this.diagnostics.isEmpty())) {
            throw new IllegalArgumentException("FAILED result contract violated");
        }
    }

    /**
     * 创建携带完整 RawDefinitionSet 且无 Diagnostic 的成功结果。
     */
    public static RawBuildResult built(RawDefinitionSet set) {
        return new RawBuildResult(
                RawBuildStatus.BUILT,
                Optional.of(Objects.requireNonNull(set, "set")),
                Collections.<Diagnostic>emptyList());
    }

    /**
     * 创建不携带部分集合、Diagnostic 已稳定排序的失败结果。
     */
    public static RawBuildResult failed(List<Diagnostic> diagnostics) {
        return new RawBuildResult(
                RawBuildStatus.FAILED,
                Optional.<RawDefinitionSet>empty(),
                diagnostics);
    }

    /**
     * 返回构建终态。
     */
    public RawBuildStatus status() {
        return status;
    }

    /**
     * 成功时返回完整集合，失败时固定为空。
     */
    public Optional<RawDefinitionSet> rawDefinitionSet() {
        return rawDefinitionSet;
    }

    /**
     * 返回按 Diagnostic 自然顺序冻结的不可变诊断列表。
     */
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /**
     * 逐项拒绝 null，排序后冻结 Diagnostic，避免调用方输入顺序影响结果。
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RawBuildResult)) {
            return false;
        }
        RawBuildResult that = (RawBuildResult) other;
        return status == that.status
                && rawDefinitionSet.equals(that.rawDefinitionSet)
                && diagnostics.equals(that.diagnostics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, rawDefinitionSet, diagnostics);
    }

    @Override
    public String toString() {
        return "RawBuildResult{" + status + ", set=" + rawDefinitionSet
                + ", diagnostics=" + diagnostics + '}';
    }
}
