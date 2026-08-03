package dec.core.compiler.symbol;

import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 单次 Symbol 构建使用的 Diagnostic 去重接缝。
 *
 * <p>当前 TDD seam 故意保留线性扫描，用于形成复杂度 Review 的真实 RED；
 * Architecture Skeleton Review 通过后再替换为哈希集合实现。</p>
 */
final class DiagnosticAccumulator {
    private final List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
    private long deduplicationSteps;

    /**
     * 通过显式线性比较加入 Diagnostic；完全相同的事实只保留一次。
     */
    void add(Diagnostic diagnostic) {
        Diagnostic required = Objects.requireNonNull(diagnostic, "diagnostic");
        for (Diagnostic existing : diagnostics) {
            deduplicationSteps++;
            if (existing.equals(required)) {
                return;
            }
        }
        diagnostics.add(required);
    }

    /**
     * 返回当前 Diagnostic 的不可变 defensive copy。
     */
    List<Diagnostic> values() {
        return Collections.unmodifiableList(
                new ArrayList<Diagnostic>(diagnostics));
    }

    /**
     * 返回本 seam 执行的去重比较步骤数，仅供同包确定性测试使用。
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
