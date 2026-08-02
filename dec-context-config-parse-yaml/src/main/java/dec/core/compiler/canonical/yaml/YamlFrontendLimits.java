package dec.core.compiler.canonical.yaml;

/**
 * YAML Frontend 的不可变资源预算。
 *
 * <p>生产默认值由 Design R20 冻结；package-private 构造器只用于同包测试注入小型预算。</p>
 */
final class YamlFrontendLimits {
    static final int MAX_DOCUMENT_BYTES = 1_048_576;
    static final int MAX_CODE_POINTS = 1_048_576;
    static final int MAX_NESTING_DEPTH = 128;
    static final int MAX_NODE_COUNT = 65_536;
    static final long MAX_CUMULATIVE_NODE_PATH_CHARS = 4_194_304L;
    static final int MAX_MAPPING_ENTRIES_PER_NODE = 256;
    static final int MAX_SEQUENCE_ITEMS_PER_NODE = 4_096;
    static final int MAX_SCALAR_CHARS_PER_NODE = 262_144;
    static final long MAX_CUMULATIVE_SCALAR_CHARS = 1_048_576L;
    static final int MAX_ALIASES_FOR_COLLECTIONS = 0;

    private final int maxDocumentBytes;
    private final int maxCodePoints;
    private final int maxNestingDepth;
    private final int maxNodeCount;
    private final long maxCumulativeNodePathChars;
    private final int maxMappingEntriesPerNode;
    private final int maxSequenceItemsPerNode;
    private final int maxScalarCharsPerNode;
    private final long maxCumulativeScalarChars;
    private final int maxAliasesForCollections;

    /**
     * 创建一组显式资源预算。
     *
     * @param maxDocumentBytes 最大 UTF-8 文档字节数
     * @param maxCodePoints 最大 Unicode code point 数
     * @param maxNestingDepth 最大 YAML/Canonical 嵌套深度
     * @param maxNodeCount 最大 Canonical 节点数
     * @param maxCumulativeNodePathChars 最大累计 nodePath 字符数
     * @param maxMappingEntriesPerNode 单 Mapping 最大 entry 数
     * @param maxSequenceItemsPerNode 单 Sequence 最大 item 数
     * @param maxScalarCharsPerNode 单 scalar 最大字符数
     * @param maxCumulativeScalarChars 最大累计 scalar 字符数
     * @param maxAliasesForCollections 最大集合 alias 数，生产值固定为零
     */
    YamlFrontendLimits(
            int maxDocumentBytes,
            int maxCodePoints,
            int maxNestingDepth,
            int maxNodeCount,
            long maxCumulativeNodePathChars,
            int maxMappingEntriesPerNode,
            int maxSequenceItemsPerNode,
            int maxScalarCharsPerNode,
            long maxCumulativeScalarChars,
            int maxAliasesForCollections) {
        this.maxDocumentBytes = requirePositive(
                maxDocumentBytes,
                "maxDocumentBytes");
        this.maxCodePoints = requirePositive(maxCodePoints, "maxCodePoints");
        this.maxNestingDepth = requirePositive(
                maxNestingDepth,
                "maxNestingDepth");
        this.maxNodeCount = requirePositive(maxNodeCount, "maxNodeCount");
        this.maxCumulativeNodePathChars = requirePositive(
                maxCumulativeNodePathChars,
                "maxCumulativeNodePathChars");
        this.maxMappingEntriesPerNode = requirePositive(
                maxMappingEntriesPerNode,
                "maxMappingEntriesPerNode");
        this.maxSequenceItemsPerNode = requirePositive(
                maxSequenceItemsPerNode,
                "maxSequenceItemsPerNode");
        this.maxScalarCharsPerNode = requirePositive(
                maxScalarCharsPerNode,
                "maxScalarCharsPerNode");
        this.maxCumulativeScalarChars = requirePositive(
                maxCumulativeScalarChars,
                "maxCumulativeScalarChars");
        if (maxAliasesForCollections < 0) {
            throw new IllegalArgumentException(
                    "maxAliasesForCollections must be >= 0");
        }
        this.maxAliasesForCollections = maxAliasesForCollections;
    }

    /**
     * 返回 Design R20 冻结的生产预算。
     */
    static YamlFrontendLimits production() {
        return new YamlFrontendLimits(
                MAX_DOCUMENT_BYTES,
                MAX_CODE_POINTS,
                MAX_NESTING_DEPTH,
                MAX_NODE_COUNT,
                MAX_CUMULATIVE_NODE_PATH_CHARS,
                MAX_MAPPING_ENTRIES_PER_NODE,
                MAX_SEQUENCE_ITEMS_PER_NODE,
                MAX_SCALAR_CHARS_PER_NODE,
                MAX_CUMULATIVE_SCALAR_CHARS,
                MAX_ALIASES_FOR_COLLECTIONS);
    }

    int maxDocumentBytes() {
        return maxDocumentBytes;
    }

    int maxCodePoints() {
        return maxCodePoints;
    }

    int maxNestingDepth() {
        return maxNestingDepth;
    }

    int maxNodeCount() {
        return maxNodeCount;
    }

    long maxCumulativeNodePathChars() {
        return maxCumulativeNodePathChars;
    }

    int maxMappingEntriesPerNode() {
        return maxMappingEntriesPerNode;
    }

    int maxSequenceItemsPerNode() {
        return maxSequenceItemsPerNode;
    }

    int maxScalarCharsPerNode() {
        return maxScalarCharsPerNode;
    }

    long maxCumulativeScalarChars() {
        return maxCumulativeScalarChars;
    }

    int maxAliasesForCollections() {
        return maxAliasesForCollections;
    }

    /**
     * 资源上限必须为正数，避免零预算被误用为“禁用检查”。
     */
    private static int requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
        return value;
    }

    /**
     * long 类型累计资源上限必须为正数。
     */
    private static long requirePositive(long value, String name) {
        if (value <= 0L) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
        return value;
    }
}
