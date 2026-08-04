package dec.core.compiler.modelaccess;

import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.util.Collections;

/**
 * Architecture Skeleton 的默认 selector；具体遍历逻辑在 Development 阶段接入。
 */
final class DefaultModelAccessSelectorResolver
        implements ModelAccessSelectorResolver {

    @Override
    public ModelAccessResolution resolve(
            SystemKey owner,
            SharedModelPath sourcePath,
            ViewKey targetView,
            SystemViewSelector selector,
            SymbolTable symbols) {
        return ModelAccessResolution.failed(Collections.singletonList(
                ModelAccessDiagnostics.create(
                        dec.core.context.model.DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                        "modelaccess.selector.not-implemented",
                        targetView,
                        symbols.require(targetView).sourceRef(),
                        "Architecture Skeleton 尚未解析 target-main 或 property path")));
    }
}
