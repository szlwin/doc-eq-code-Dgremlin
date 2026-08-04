package dec.core.compiler.modelaccess;

import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;

/**
 * 当前 System 已声明 View 内的确定性 selector 解析 seam。
 */
public interface ModelAccessSelectorResolver {
    /**
     * 先精确匹配 target-main，未命中时才逐段解析同一 View property path。
     */
    ModelAccessResolution resolve(
            SystemKey owner,
            SharedModelPath sourcePath,
            ViewKey targetView,
            SystemViewSelector selector,
            SymbolTable symbols);
}
