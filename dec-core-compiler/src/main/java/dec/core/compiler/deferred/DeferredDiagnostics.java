package dec.core.compiler.deferred;

import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;

/**
 * T11 Deferred 分类的稳定 Diagnostic 工厂。
 */
final class DeferredDiagnostics {
    private static final SourceRef FALLBACK_SOURCE =
            new SourceRef("<deferred>", 0, 0, "/classification");

    private DeferredDiagnostics() {
    }

    /** 为缺失或不完整字段创建统一阻断 Diagnostic。 */
    static Diagnostic incomplete(
            DeferredClassificationInput input,
            String field) {
        DefinitionKey key = input == null || !input.ownerKey().isPresent()
                ? null
                : input.ownerKey().get();
        SourceRef source = input == null || !input.sourceRef().isPresent()
                ? FALLBACK_SOURCE
                : input.sourceRef().get();
        return new Diagnostic(
                DiagnosticCode.MIX_DEFERRED_INCOMPLETE,
                DiagnosticSeverity.ERROR,
                "deferred.incomplete." + field,
                key,
                source,
                Collections.<SourceRef>emptyList(),
                "补齐 Deferred 分类字段并确保全部引用已类型化",
                "DeferredClassificationPass");
    }
}
