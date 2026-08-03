package dec.core.compiler.raw;

/**
 * RawDefinitionBuilder 使用的不可变 Canonical 输入资源预算。
 *
 * <p>生产默认值由 Design R24 冻结；同包测试可注入更小预算，
 * 以小型 Canonical 树验证边界而不制造真实栈溢出。</p>
 */
final class RawBuilderLimits {
    private static final int PRODUCTION_MAX_CANONICAL_DEPTH = 256;
    private static final int PRODUCTION_MAX_CANONICAL_NODE_COUNT = 65_536;

    private final int maxCanonicalDepth;
    private final int maxCanonicalNodeCount;

    /**
     * 创建显式预算，所有限制必须为正数。
     */
    RawBuilderLimits(int maxCanonicalDepth, int maxCanonicalNodeCount) {
        this.maxCanonicalDepth = positive(
                maxCanonicalDepth,
                "maxCanonicalDepth");
        this.maxCanonicalNodeCount = positive(
                maxCanonicalNodeCount,
                "maxCanonicalNodeCount");
    }

    /**
     * 返回 Design R24 冻结的生产预算。
     */
    static RawBuilderLimits production() {
        return new RawBuilderLimits(
                PRODUCTION_MAX_CANONICAL_DEPTH,
                PRODUCTION_MAX_CANONICAL_NODE_COUNT);
    }

    int maxCanonicalDepth() {
        return maxCanonicalDepth;
    }

    int maxCanonicalNodeCount() {
        return maxCanonicalNodeCount;
    }

    /**
     * 校验正整数预算并返回原值。
     */
    private static int positive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
