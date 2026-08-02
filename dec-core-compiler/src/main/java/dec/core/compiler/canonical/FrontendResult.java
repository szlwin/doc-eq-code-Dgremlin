package dec.core.compiler.canonical;

import dec.core.context.model.Diagnostic;
import java.util.List;

/**
 * Frontend 解析后的稳定结果边界。
 */
public interface FrontendResult {
    /**
     * 返回 Frontend 是否成功产生 Canonical 结果。
     */
    boolean isSuccessful();

    /**
     * 返回稳定排序且不可变的 Diagnostic。
     */
    List<Diagnostic> diagnostics();
}
