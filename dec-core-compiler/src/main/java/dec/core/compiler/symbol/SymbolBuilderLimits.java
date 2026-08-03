package dec.core.compiler.symbol;

/**
 * SymbolTableBuilder 使用的不可变定义数量预算。
 */
final class SymbolBuilderLimits {
    private static final int PRODUCTION_MAX_DEFINITION_COUNT = 65_536;

    private final int maxDefinitionCount;

    /**
     * 创建显式定义数量预算，测试可注入小型边界。
     */
    SymbolBuilderLimits(int maxDefinitionCount) {
        if (maxDefinitionCount <= 0) {
            throw new IllegalArgumentException(
                    "maxDefinitionCount must be positive");
        }
        this.maxDefinitionCount = maxDefinitionCount;
    }

    /**
     * 返回 T07 冻结的生产预算。
     */
    static SymbolBuilderLimits production() {
        return new SymbolBuilderLimits(PRODUCTION_MAX_DEFINITION_COUNT);
    }

    int maxDefinitionCount() {
        return maxDefinitionCount;
    }
}
