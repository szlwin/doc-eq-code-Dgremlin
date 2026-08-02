package dec.core.compiler.canonical.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendStatus;
import dec.core.context.model.DiagnosticCode;
import org.junit.jupiter.api.Test;

/**
 * T05 YAML 文档、深度、节点、路径、集合和标量资源预算 Oracle。
 *
 * <p>全部测试使用小型输入和注入预算，不构造真实 OOM。</p>
 */
class YamlFrontendResourceLimitTest {

    /**
     * Design R20 冻结的生产预算必须保持精确不变。
     */
    @Test
    void productionLimitsMatchDesignR20() {
        assertEquals(1_048_576L,
                YamlFrontendTestSupport.productionLimit("MAX_DOCUMENT_BYTES"));
        assertEquals(1_048_576L,
                YamlFrontendTestSupport.productionLimit("MAX_CODE_POINTS"));
        assertEquals(128L,
                YamlFrontendTestSupport.productionLimit("MAX_NESTING_DEPTH"));
        assertEquals(65_536L,
                YamlFrontendTestSupport.productionLimit("MAX_NODE_COUNT"));
        assertEquals(4_194_304L,
                YamlFrontendTestSupport.productionLimit(
                        "MAX_CUMULATIVE_NODE_PATH_CHARS"));
        assertEquals(256L,
                YamlFrontendTestSupport.productionLimit(
                        "MAX_MAPPING_ENTRIES_PER_NODE"));
        assertEquals(4_096L,
                YamlFrontendTestSupport.productionLimit(
                        "MAX_SEQUENCE_ITEMS_PER_NODE"));
        assertEquals(262_144L,
                YamlFrontendTestSupport.productionLimit(
                        "MAX_SCALAR_CHARS_PER_NODE"));
        assertEquals(1_048_576L,
                YamlFrontendTestSupport.productionLimit(
                        "MAX_CUMULATIVE_SCALAR_CHARS"));
        assertEquals(0L,
                YamlFrontendTestSupport.productionLimit(
                        "MAX_ALIASES_FOR_COLLECTIONS"));
    }

    /**
     * 小型文档在全部预算内时必须成功。
     */
    @Test
    void acceptsDocumentWithinAllLimits() {
        DocumentFrontend frontend = frontend(
                128, 128, 16, 2, 16, 1, 2, 3, 3, 0);
        FrontendResult result = parse(
                frontend,
                "root:\n"
                        + "  child:\n"
                        + "    \"#text\": abc\n");

        assertEquals(FrontendStatus.PARSED, result.status());
        assertTrue(result.canonicalRoot().isPresent());
    }

    /**
     * parser 创建前必须拒绝超过文档字节预算的输入。
     */
    @Test
    void rejectsDocumentBytesOverLimit() {
        assertLimitFailure("root: value\n",
                frontend(5, 128, 16, 16, 512, 16, 16, 64, 512, 0));
    }

    /**
     * SnakeYAML compose 阶段必须拒绝超过 code point 预算的输入。
     */
    @Test
    void rejectsCodePointsOverLimit() {
        assertLimitFailure("root: abcdef\n",
                frontend(128, 5, 16, 16, 512, 16, 16, 64, 512, 0));
    }

    /**
     * 超深 YAML 必须在 Canonical 节点构造前失败。
     */
    @Test
    void rejectsNestingDepthOverLimit() {
        assertLimitFailure(
                "root:\n  a:\n    b:\n      c:\n        d: value\n",
                frontend(256, 256, 3, 32, 1024, 16, 16, 64, 1024, 0));
    }

    /**
     * 创建超过最大 Canonical 节点数的新节点前必须失败。
     */
    @Test
    void rejectsNodeCountOverLimit() {
        assertLimitFailure(
                "root:\n  first: one\n  second: two\n",
                frontend(128, 128, 16, 2, 512, 16, 16, 64, 512, 0));
    }

    /**
     * 构造当前 nodePath 前必须拒绝累计路径字符超限。
     */
    @Test
    void rejectsCumulativeNodePathCharsOverLimit() {
        assertLimitFailure(
                "root:\n  child: value\n",
                frontend(128, 128, 16, 8, 15, 16, 16, 64, 512, 0));
    }

    /**
     * 遍历过大的单节点 Mapping 前必须失败。
     */
    @Test
    void rejectsMappingEntriesPerNodeOverLimit() {
        assertLimitFailure(
                "root:\n  first: one\n  second: two\n",
                frontend(128, 128, 16, 8, 512, 1, 16, 64, 512, 0));
    }

    /**
     * 遍历过大的重复子节点 Sequence 前必须失败。
     */
    @Test
    void rejectsSequenceItemsPerNodeOverLimit() {
        assertLimitFailure(
                "root:\n  item:\n    - one\n    - two\n",
                frontend(128, 128, 16, 8, 512, 16, 1, 64, 512, 0));
    }

    /**
     * 保存单个过长 scalar 前必须失败。
     */
    @Test
    void rejectsScalarCharsPerNodeOverLimit() {
        assertLimitFailure(
                "root: 1234\n",
                frontend(128, 128, 16, 8, 512, 16, 16, 3, 512, 0));
    }

    /**
     * 多个节点的 scalar 累计超过预算时必须失败。
     */
    @Test
    void rejectsCumulativeScalarCharsOverLimit() {
        assertLimitFailure(
                "root:\n  first: 123\n  second: 456\n",
                frontend(128, 128, 16, 8, 512, 16, 16, 3, 5, 0));
    }

    /**
     * 使用指定小型预算创建 Frontend。
     */
    private static DocumentFrontend frontend(
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
        return YamlFrontendTestSupport.frontendWithLimits(
                maxDocumentBytes,
                maxCodePoints,
                maxNestingDepth,
                maxNodeCount,
                maxCumulativeNodePathChars,
                maxMappingEntriesPerNode,
                maxSequenceItemsPerNode,
                maxScalarCharsPerNode,
                maxCumulativeScalarChars,
                maxAliasesForCollections);
    }

    /**
     * 使用固定 Source 执行 YAML 解析。
     */
    private static FrontendResult parse(
            DocumentFrontend frontend,
            String yaml) {
        return YamlFrontendTestSupport.parse(
                frontend,
                YamlFrontendTestSupport.yamlSource(yaml));
    }

    /**
     * 断言预算失败不发布部分 root，并返回统一 YAML 安全 Diagnostic。
     */
    private static void assertLimitFailure(
            String yaml,
            DocumentFrontend frontend) {
        FrontendResult result = parse(frontend, yaml);

        assertEquals(FrontendStatus.FAILED, result.status());
        assertFalse(result.canonicalRoot().isPresent());
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.code()
                        == DiagnosticCode.MIX_FRONTEND_YAML_UNSAFE));
    }
}
