package dec.core.compiler.information;

import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.InformationKey;
import dec.core.context.model.SourceRef;

/**
 * 可注入的 Information 精确强类型引用解析边界。
 */
public interface InformationReferenceResolver {
    /**
     * 按 owner policy 与 SymbolTable 精确解析 AST 引用。
     */
    InformationReferenceResolutionResult resolve(
            InformationKey owner,
            InformationExpressionAst ast,
            SymbolTable symbols,
            SourceRef sourceRef);
}
