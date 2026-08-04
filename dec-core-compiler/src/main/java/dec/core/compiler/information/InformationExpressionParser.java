package dec.core.compiler.information;

import dec.core.context.model.SourceRef;

/**
 * 可注入的 Information expression parser 边界。
 */
public interface InformationExpressionParser {
    /**
     * 将一个 expression 转换为 AST 或稳定 Diagnostic。
     */
    InformationExpressionParseResult parse(
            String expression,
            SourceRef sourceRef);
}
