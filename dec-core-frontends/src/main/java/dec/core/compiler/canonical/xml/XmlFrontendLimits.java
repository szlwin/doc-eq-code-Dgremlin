package dec.core.compiler.canonical.xml;

/**
 * 安全 XML Frontend 使用的不可变资源预算。
 *
 * <p>生产默认值由 Design R19 冻结；同包测试可注入更小预算，
 * 以小型输入确定性验证边界而不制造真实内存耗尽。</p>
 */
final class XmlFrontendLimits {
    private static final int PRODUCTION_MAX_DOCUMENT_BYTES = 1_048_576;
    private static final int PRODUCTION_MAX_ELEMENT_DEPTH = 256;
    private static final int PRODUCTION_MAX_NODE_COUNT = 65_536;
    private static final long PRODUCTION_MAX_CUMULATIVE_NODE_PATH_CHARS = 4_194_304L;
    private static final int PRODUCTION_MAX_ATTRIBUTES_PER_ELEMENT = 256;
    private static final int PRODUCTION_MAX_DIRECT_TEXT_CHARS_PER_ELEMENT = 262_144;
    private static final long PRODUCTION_MAX_CUMULATIVE_DIRECT_TEXT_CHARS = 1_048_576L;

    private final int maxDocumentBytes;
    private final int maxElementDepth;
    private final int maxNodeCount;
    private final long maxCumulativeNodePathChars;
    private final int maxAttributesPerElement;
    private final int maxDirectTextCharsPerElement;
    private final long maxCumulativeDirectTextChars;

    /**
     * 创建显式资源预算，所有值必须为正数。
     */
    XmlFrontendLimits(
            int maxDocumentBytes,
            int maxElementDepth,
            int maxNodeCount,
            long maxCumulativeNodePathChars,
            int maxAttributesPerElement,
            int maxDirectTextCharsPerElement,
            long maxCumulativeDirectTextChars) {
        this.maxDocumentBytes = positive(maxDocumentBytes, "maxDocumentBytes");
        this.maxElementDepth = positive(maxElementDepth, "maxElementDepth");
        this.maxNodeCount = positive(maxNodeCount, "maxNodeCount");
        this.maxCumulativeNodePathChars = positive(
                maxCumulativeNodePathChars,
                "maxCumulativeNodePathChars");
        this.maxAttributesPerElement = positive(
                maxAttributesPerElement,
                "maxAttributesPerElement");
        this.maxDirectTextCharsPerElement = positive(
                maxDirectTextCharsPerElement,
                "maxDirectTextCharsPerElement");
        this.maxCumulativeDirectTextChars = positive(
                maxCumulativeDirectTextChars,
                "maxCumulativeDirectTextChars");
    }

    /**
     * 返回 Design R19 冻结的生产预算。
     */
    static XmlFrontendLimits production() {
        return new XmlFrontendLimits(
                PRODUCTION_MAX_DOCUMENT_BYTES,
                PRODUCTION_MAX_ELEMENT_DEPTH,
                PRODUCTION_MAX_NODE_COUNT,
                PRODUCTION_MAX_CUMULATIVE_NODE_PATH_CHARS,
                PRODUCTION_MAX_ATTRIBUTES_PER_ELEMENT,
                PRODUCTION_MAX_DIRECT_TEXT_CHARS_PER_ELEMENT,
                PRODUCTION_MAX_CUMULATIVE_DIRECT_TEXT_CHARS);
    }

    int maxDocumentBytes() {
        return maxDocumentBytes;
    }

    int maxElementDepth() {
        return maxElementDepth;
    }

    int maxNodeCount() {
        return maxNodeCount;
    }

    long maxCumulativeNodePathChars() {
        return maxCumulativeNodePathChars;
    }

    int maxAttributesPerElement() {
        return maxAttributesPerElement;
    }

    int maxDirectTextCharsPerElement() {
        return maxDirectTextCharsPerElement;
    }

    long maxCumulativeDirectTextChars() {
        return maxCumulativeDirectTextChars;
    }

    /**
     * 校验 int 预算并返回原值。
     */
    private static int positive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    /**
     * 校验 long 预算并返回原值。
     */
    private static long positive(long value, String name) {
        if (value <= 0L) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
