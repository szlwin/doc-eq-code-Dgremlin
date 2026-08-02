package dec.core.compiler.canonical;

import dec.core.context.model.Diagnostic;
import java.util.List;
import java.util.Optional;

/**
 * Frontend 解析后的稳定 Canonical 成功或 Diagnostic 失败边界。
 */
public interface FrontendResult {
    /**
     * 返回 PARSED 或 FAILED 终态。
     */
    FrontendStatus status();

    /**
     * 返回成功解析产生的唯一 Canonical 根；失败时必须为空。
     */
    Optional<CanonicalDocumentNode> canonicalRoot();

    /**
     * 返回稳定排序且不可变的 Diagnostic。
     */
    List<Diagnostic> diagnostics();
}
