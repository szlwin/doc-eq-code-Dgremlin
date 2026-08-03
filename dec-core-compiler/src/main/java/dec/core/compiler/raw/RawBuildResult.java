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
        this.diagnostics = Collections.unmodifiableList(
                new ArrayList<Diagnostic>(
                        Objects.requireNonNull(diagnostics, "diagnostics")));
        if (status == RawBuildStatus.BUILT
                && (!rawDefinitionSet.isPresent() || !diagnostics.isEmpty())) {
            throw new IllegalArgumentException("BUILT result contract violated");
        }
        if (status == RawBuildStatus.FAILED
                && (rawDefinitionSet.isPresent() || diagnostics.isEmpty())) {
            throw new IllegalArgumentException("FAILED result contract violated");
        }
    }

    /** 创建成功结果。 */
    public static RawBuildResult built(RawDefinitionSet set) {
        return new RawBuildResult(
                RawBuildStatus.BUILT,
                Optional.of(Objects.requireNonNull(set, "set")),
                Collections.<Diagnostic>emptyList());
    }

    /** 创建不携带部分集合的失败结果。 */
    public static RawBuildResult failed(List<Diagnostic> diagnostics) {
        return new RawBuildResult(
                RawBuildStatus.FAILED,
                Optional.<RawDefinitionSet>empty(),
                diagnostics);
    }

    public RawBuildStatus status() {
        return status;
    }

    public Optional<RawDefinitionSet> rawDefinitionSet() {
        return rawDefinitionSet;
    }

    public List<Diagnostic> diagnostics() {
        return diagnostics;
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
