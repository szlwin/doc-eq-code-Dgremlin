package dec.core.compiler.source;

import dec.core.context.model.Diagnostic;
import java.util.List;

/**
 * Source Provider 的不可变解析结果视图。
 */
public interface SourceResolutionResult {
    /**
     * 返回解析成功或失败状态。
     */
    SourceResolutionStatus status();

    /**
     * 返回稳定排序且不可变的 Source 列表。
     */
    List<DocumentSource> sources();

    /**
     * 返回稳定排序且不可变的 Diagnostic 列表。
     */
    List<Diagnostic> diagnostics();
}
