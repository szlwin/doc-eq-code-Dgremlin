package dec.core.compiler.symbol;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticSeverity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * SymbolTable 的完整成功结果或稳定失败 Diagnostic 边界。
 */
public final class SymbolBuildResult {
    private static final String PASS = "symbol-registration";

    private final SymbolBuildStatus status;
    private final Optional<SymbolTable> symbolTable;
    private final List<Diagnostic> diagnostics;

    private SymbolBuildResult(
            SymbolBuildStatus status,
            Optional<SymbolTable> symbolTable,
            List<Diagnostic> diagnostics) {
        this.status = Objects.requireNonNull(status, "status");
        this.symbolTable = Objects.requireNonNull(symbolTable, "symbolTable");
        this.diagnostics = immutableDiagnostics(diagnostics);
        requireContract();
    }

    /**
     * 创建携带完整表且无 Diagnostic 的成功结果。
     */
    public static SymbolBuildResult built(SymbolTable table) {
        return new SymbolBuildResult(
                SymbolBuildStatus.BUILT,
                Optional.of(Objects.requireNonNull(table, "table")),
                Collections.<Diagnostic>emptyList());
    }

    /**
     * 创建不携带部分表的失败结果。
     */
    public static SymbolBuildResult failed(List<Diagnostic> diagnostics) {
        return new SymbolBuildResult(
                SymbolBuildStatus.FAILED,
                Optional.<SymbolTable>empty(),
                diagnostics);
    }

    /**
     * 返回构建终态。
     */
    public SymbolBuildStatus status() {
        return status;
    }

    /**
     * 成功时返回完整表，失败时固定为空。
     */
    public Optional<SymbolTable> symbolTable() {
        return symbolTable;
    }

    /**
     * 返回稳定排序且不可变的诊断列表。
     */
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /**
     * 强制 BUILT/FAILED 的互斥发布合同。
     */
    private void requireContract() {
        if (status == SymbolBuildStatus.BUILT
                && (!symbolTable.isPresent() || !diagnostics.isEmpty())) {
            throw new IllegalArgumentException("BUILT result contract violated");
        }
        if (status == SymbolBuildStatus.FAILED
                && (symbolTable.isPresent() || diagnostics.isEmpty())) {
            throw new IllegalArgumentException("FAILED result contract violated");
        }
        if (status == SymbolBuildStatus.FAILED) {
            for (Diagnostic diagnostic : diagnostics) {
                if (diagnostic.severity() != DiagnosticSeverity.ERROR) {
                    throw new IllegalArgumentException(
                            "FAILED diagnostic severity must be ERROR");
                }
                if (!PASS.equals(diagnostic.pass())) {
                    throw new IllegalArgumentException(
                            "FAILED diagnostic pass must be " + PASS);
                }
            }
        }
    }

    /**
     * 逐项拒绝 null，按 Context 稳定顺序排序并冻结。
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
        if (!(other instanceof SymbolBuildResult)) {
            return false;
        }
        SymbolBuildResult that = (SymbolBuildResult) other;
        return status == that.status
                && symbolTable.equals(that.symbolTable)
                && diagnostics.equals(that.diagnostics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, symbolTable, diagnostics);
    }

    @Override
    public String toString() {
        return "SymbolBuildResult{" + status + ", table=" + symbolTable
                + ", diagnostics=" + diagnostics + '}';
    }
}
