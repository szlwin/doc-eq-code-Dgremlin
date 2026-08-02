package dec.core.compiler.api;

import dec.core.context.model.Diagnostic;
import java.util.List;

/**
 * 成功发布与失败编译共享的稳定终态结果合同。
 */
public interface CompilationResult {
    /**
     * 返回当前编译的唯一终态。
     */
    CompilationStatus status();

    /**
     * 返回稳定排序且不可变的 Diagnostic。
     */
    List<Diagnostic> diagnostics();
}
