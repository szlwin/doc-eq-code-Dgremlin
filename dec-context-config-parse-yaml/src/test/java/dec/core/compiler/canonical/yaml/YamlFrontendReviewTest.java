package dec.core.compiler.canonical.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendStatus;
import dec.core.context.model.DiagnosticCode;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Development GREEN 后由独立 Review 补充的 YAML 安全与边界 Oracle。
 */
class YamlFrontendReviewTest {

    /**
     * 即使没有 alias，单独声明 anchor 也会把树变成潜在共享图，必须拒绝。
     */
    @Test
    void rejectsStandaloneAnchor() {
        assertUnsafe("root:\n  child: &standalone value\n");
    }

    /**
     * merge key 必须独立于 alias 门禁被拒绝，避免测试只命中更早的 alias 失败。
     */
    @Test
    void rejectsMergeKeyWithoutAlias() {
        assertUnsafe("root:\n  child:\n    <<: value\n");
    }

    /**
     * binary、set、omap 和 pairs 均不属于 Design R20 冻结 tag 集合。
     */
    @Test
    void rejectsUnsupportedStandardTags() {
        List<String> unsafeDocuments = Arrays.asList(
                "root: !!binary SGVsbG8=\n",
                "root: !!set {a: null}\n",
                "root: !!omap [{a: one}]\n",
                "root: !!pairs [{a: one}]\n");
        for (String yaml : unsafeDocuments) {
            assertUnsafe(yaml);
        }
    }

    /**
     * 保留 key 的 value 类型必须严格匹配 Mapping 或 Scalar/null 合同。
     */
    @Test
    void rejectsInvalidReservedKeyShapes() {
        assertUnsafe("root:\n  \"@attributes\": value\n");
        assertUnsafe("root:\n  \"#text\":\n    child: value\n");
        assertUnsafe(
                "root:\n"
                        + "  \"@attributes\":\n"
                        + "    id:\n"
                        + "      nested: value\n");
        assertUnsafe("\"@attributes\": value\n");
    }

    /**
     * Sequence item 不能再次是 Sequence，保证重复子节点仍是普通树节点。
     */
    @Test
    void rejectsNestedSequenceItems() {
        assertUnsafe(
                "root:\n"
                        + "  item:\n"
                        + "    - [one, two]\n");
    }

    /**
     * 所有资源上限都必须显式有效，零值不能被误解为关闭检查。
     */
    @Test
    void rejectsInvalidLimitConfiguration() {
        assertInvalidLimits(0, 1, 1, 1, 1L, 1, 1, 1, 1L, 0);
        assertInvalidLimits(1, 0, 1, 1, 1L, 1, 1, 1, 1L, 0);
        assertInvalidLimits(1, 1, 0, 1, 1L, 1, 1, 1, 1L, 0);
        assertInvalidLimits(1, 1, 1, 0, 1L, 1, 1, 1, 1L, 0);
        assertInvalidLimits(1, 1, 1, 1, 0L, 1, 1, 1, 1L, 0);
        assertInvalidLimits(1, 1, 1, 1, 1L, 0, 1, 1, 1L, 0);
        assertInvalidLimits(1, 1, 1, 1, 1L, 1, 0, 1, 1L, 0);
        assertInvalidLimits(1, 1, 1, 1, 1L, 1, 1, 0, 1L, 0);
        assertInvalidLimits(1, 1, 1, 1, 1L, 1, 1, 1, 0L, 0);
        assertInvalidLimits(1, 1, 1, 1, 1L, 1, 1, 1, 1L, -1);
    }

    /**
     * 生产默认 Frontend 必须在第 129 个 Canonical 层级前受控失败。
     */
    @Test
    void productionFrontendRejectsDepthBeyondDesignLimit() {
        StringBuilder yaml = new StringBuilder("root:\n");
        for (int depth = 1; depth <= 128; depth++) {
            appendIndent(yaml, depth);
            yaml.append('n').append(depth).append(':');
            if (depth == 128) {
                yaml.append(" value\n");
            } else {
                yaml.append('\n');
            }
        }

        assertUnsafe(yaml.toString());
    }

    /**
     * 标准安全 scalar tag 保留词法值，YAML null 映射为空 Optional。
     */
    @Test
    void preservesAllowedScalarLexemesAndNull() {
        FrontendResult result = parse(
                "root:\n"
                        + "  boolValue: true\n"
                        + "  intValue: 42\n"
                        + "  floatValue: 3.5\n"
                        + "  timestampValue: 2026-08-02\n"
                        + "  nullValue: null\n");

        assertEquals(FrontendStatus.PARSED, result.status());
        CanonicalDocumentNode root = result.canonicalRoot().get();
        assertEquals("true", root.children().get(0).scalar().get());
        assertEquals("42", root.children().get(1).scalar().get());
        assertEquals("3.5", root.children().get(2).scalar().get());
        assertEquals("2026-08-02", root.children().get(3).scalar().get());
        assertFalse(root.children().get(4).scalar().isPresent());
        for (CanonicalDocumentNode child : root.children()) {
            assertEquals("1.0", child.schemaVersion());
            assertTrue(child.sourceRef().sourceId().endsWith("/root.yaml"));
        }
    }

    /**
     * 使用生产 Frontend 解析固定 YAML。
     */
    private static FrontendResult parse(String yaml) {
        DocumentFrontend frontend = YamlFrontendTestSupport.frontend();
        return YamlFrontendTestSupport.parse(
                frontend,
                YamlFrontendTestSupport.yamlSource(yaml));
    }

    /**
     * 先验证安全控制样本可解析，再验证目标输入稳定失败且无部分 root。
     */
    private static void assertUnsafe(String yaml) {
        FrontendResult control = parse("root: value\n");
        assertEquals(FrontendStatus.PARSED, control.status());
        assertTrue(control.canonicalRoot().isPresent());

        FrontendResult result = parse(yaml);
        assertEquals(FrontendStatus.FAILED, result.status());
        assertFalse(result.canonicalRoot().isPresent());
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.code()
                        == DiagnosticCode.MIX_FRONTEND_YAML_UNSAFE));
    }

    /**
     * 断言一组非法资源预算在 Frontend 创建前即被拒绝。
     */
    private static void assertInvalidLimits(
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
        assertThrows(IllegalArgumentException.class, () ->
                new YamlFrontendLimits(
                        maxDocumentBytes,
                        maxCodePoints,
                        maxNestingDepth,
                        maxNodeCount,
                        maxCumulativeNodePathChars,
                        maxMappingEntriesPerNode,
                        maxSequenceItemsPerNode,
                        maxScalarCharsPerNode,
                        maxCumulativeScalarChars,
                        maxAliasesForCollections));
    }

    /**
     * 追加 YAML 两空格缩进。
     */
    private static void appendIndent(StringBuilder yaml, int depth) {
        for (int index = 0; index < depth; index++) {
            yaml.append("  ");
        }
    }
}
