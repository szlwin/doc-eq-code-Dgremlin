package dec.core.compiler.source;

import dec.core.context.model.Diagnostic;
import java.util.List;
import java.util.Optional;

/**
 * Source discovery 成功图或稳定失败 Diagnostic 的不可变结果合同。
 */
public interface SourceGraphResolutionResult {
    SourceGraphResolutionStatus status();

    Optional<MixSourceGraph> graph();

    List<Diagnostic> diagnostics();
}
