package dec.core.compiler.symbol;

import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 单次 Symbol 构建使用的稳定 Diagnostic 去重器。
 *
 * <p>使用 LinkedHashSet 保持首次报告顺序，并把每次去重降为一次哈希集合 add。
 * 最终稳定排序仍由 SymbolBuildResult 按 Diagnostic.compareTo 完成。</p>
 */
final class DiagnosticAccumulator {
    private final Set<Diagnostic> diagnostics =
            new LinkedHashSet<Diagnostic>();
    private long deduplicationSteps;

    /**
     * 每次报告只执行一次哈希集合插入尝试；完全相同的事实只保留一次。
     */
    void add(Diagnostic diagnostic) {
        deduplicationSteps++;
        diagnostics.add(Objects.requireNonNull(diagnostic, "diagnostic"));
    }

    /**
     * 返回保持首次报告顺序的不可变 defensive copy。
     */
    List<Diagnostic> values() {
        return Collections.unmodifiableList(
                new ArrayList<Diagnostic>(diagnostics));
    }

    /**
     * 返回集合 add 的尝试次数，仅供同包确定性测试验证线性步骤。
     */
    long deduplicationSteps() {
        return deduplicationSteps;
    }

    /**
     * 返回当前不同 Diagnostic 数量。
     */
    int size() {
        return diagnostics.size();
    }

    /**
     * 判断是否还没有 Diagnostic。
     */
    boolean isEmpty() {
        return diagnostics.isEmpty();
    }
}
