package dec.core.compiler.canonical.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendStatus;
import dec.core.context.model.DiagnosticCode;
import org.junit.jupiter.api.Test;

/**
 * T04 I002 的 XML 深度、节点、路径和文本资源预算 Oracle。
 *
 * <p>全部测试使用小型或受预算约束的输入，不通过真实 OOM 证明安全。</p>
 */
class XmlFrontendResourceLimitTest {

    /**
     * 生产预算必须与 Design R19 冻结值完全一致。
     */
    @Test
    void freezesProductionLimitsFromDesignR19() {
        XmlFrontendLimits limits = XmlFrontendLimits.production();

        assertEquals(1_048_576, limits.maxDocumentBytes());
        assertEquals(256, limits.maxElementDepth());
        assertEquals(65_536, limits.maxNodeCount());
        assertEquals(4_194_304L, limits.maxCumulativeNodePathChars());
        assertEquals(256, limits.maxAttributesPerElement());
        assertEquals(262_144, limits.maxDirectTextCharsPerElement());
        assertEquals(1_048_576L, limits.maxCumulativeDirectTextChars());
    }

    /**
     * 任一非正预算都必须在创建策略时立即失败。
     */
    @Test
    void rejectsNonPositiveLimitConfiguration() {
        assertInvalidLimits(0, 1, 1, 1L, 1, 1, 1L);
        assertInvalidLimits(1, 0, 1, 1L, 1, 1, 1L);
        assertInvalidLimits(1, 1, 0, 1L, 1, 1, 1L);
        assertInvalidLimits(1, 1, 1, 0L, 1, 1, 1L);
        assertInvalidLimits(1, 1, 1, 1L, 0, 1, 1L);
        assertInvalidLimits(1, 1, 1, 1L, 1, 0, 1L);
        assertInvalidLimits(1, 1, 1, 1L, 1, 1, 0L);
    }

    /**
     * 深度、节点数和累计路径字符恰好位于边界时必须成功。
     */
    @Test
    void acceptsDocumentExactlyWithinStructuralLimits() {
        XmlFrontendTestSupport.FrontendHarness harness = frontend(
                64,
                3,
                3,
                12,
                1,
                16,
                16);
        FrontendResult result = XmlFrontendTestSupport.parse(
                harness,
                XmlFrontendTestSupport.xmlSource("<a><b><c/></b></a>"));

        assertEquals(FrontendStatus.PARSED, result.status());
        assertTrue(result.canonicalRoot().isPresent());
        assertEquals(0, harness.externalAccessAttempts());
    }

    /**
     * 生产 Frontend 必须直接拒绝超过冻结深度 256 的小型输入。
     */
    @Test
    void productionFrontendRejectsDepthBeyondFrozenLimit() {
        assertLimitFailure(
                nestedXml(257, "a"),
                XmlFrontendTestSupport.frontend());
    }

    /**
     * 在深度仍合法时，生产 Frontend 必须拒绝累计路径字符超限。
     */
    @Test
    void productionFrontendRejectsCumulativePathBeyondFrozenLimit() {
        assertLimitFailure(
                nestedXml(256, repeatedName(128)),
                XmlFrontendTestSupport.frontend());
    }

    /**
     * reader 创建前必须拒绝超过文档字节预算的输入。
     */
    @Test
    void rejectsDocumentBytesOverLimit() {
        assertLimitFailure("<root/>", frontend(6, 8, 8, 128, 8, 32, 64));
    }

    /**
     * 创建深层节点前必须拒绝超过最大元素深度的输入。
     */
    @Test
    void rejectsElementDepthOverLimit() {
        assertLimitFailure("<a><b><c/></b></a>",
                frontend(64, 2, 8, 128, 8, 32, 64));
    }

    /**
     * 创建新节点前必须拒绝超过最大节点数的输入。
     */
    @Test
    void rejectsNodeCountOverLimit() {
        assertLimitFailure("<a><b/><c/></a>",
                frontend(64, 8, 2, 128, 8, 32, 64));
    }

    /**
     * 构造当前 SourceRef 前必须拒绝累计 nodePath 字符超限。
     */
    @Test
    void rejectsCumulativeNodePathCharsOverLimit() {
        assertLimitFailure("<a><b><c/></b></a>",
                frontend(64, 8, 8, 11, 8, 32, 64));
    }

    /**
     * 创建属性 Map 前必须拒绝单元素属性数超限。
     */
    @Test
    void rejectsAttributesPerElementOverLimit() {
        assertLimitFailure("<a x=\"1\" y=\"2\"/>",
                frontend(64, 8, 8, 128, 1, 32, 64));
    }

    /**
     * 追加文本前必须拒绝单节点直接文本长度超限。
     */
    @Test
    void rejectsDirectTextPerElementOverLimit() {
        assertLimitFailure("<a>12345</a>",
                frontend(64, 8, 8, 128, 8, 4, 64));
    }

    /**
     * 多个节点的直接文本累计超过预算时必须受控失败。
     */
    @Test
    void rejectsCumulativeDirectTextOverLimit() {
        assertLimitFailure("<a><b>123</b><c>456</c></a>",
                frontend(64, 8, 8, 128, 8, 4, 5));
    }

    /**
     * 创建带指定小型预算的 Frontend。
     */
    private static XmlFrontendTestSupport.FrontendHarness frontend(
            int maxDocumentBytes,
            int maxElementDepth,
            int maxNodeCount,
            long maxCumulativeNodePathChars,
            int maxAttributesPerElement,
            int maxDirectTextCharsPerElement,
            long maxCumulativeDirectTextChars) {
        return XmlFrontendTestSupport.frontendWithLimits(
                maxDocumentBytes,
                maxElementDepth,
                maxNodeCount,
                maxCumulativeNodePathChars,
                maxAttributesPerElement,
                maxDirectTextCharsPerElement,
                maxCumulativeDirectTextChars);
    }

    /**
     * 断言指定预算组合无法构造。
     */
    private static void assertInvalidLimits(
            int maxDocumentBytes,
            int maxElementDepth,
            int maxNodeCount,
            long maxCumulativeNodePathChars,
            int maxAttributesPerElement,
            int maxDirectTextCharsPerElement,
            long maxCumulativeDirectTextChars) {
        assertThrows(IllegalArgumentException.class, () -> new XmlFrontendLimits(
                maxDocumentBytes,
                maxElementDepth,
                maxNodeCount,
                maxCumulativeNodePathChars,
                maxAttributesPerElement,
                maxDirectTextCharsPerElement,
                maxCumulativeDirectTextChars));
    }

    /**
     * 构造指定深度且名称固定的完整 XML。
     */
    private static String nestedXml(int depth, String name) {
        StringBuilder xml = new StringBuilder(depth * (name.length() * 2 + 5));
        for (int index = 0; index < depth; index++) {
            xml.append('<').append(name).append('>');
        }
        for (int index = 0; index < depth; index++) {
            xml.append("</").append(name).append('>');
        }
        return xml.toString();
    }

    /**
     * 构造合法且固定长度的 XML local-name。
     */
    private static String repeatedName(int length) {
        StringBuilder name = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            name.append('n');
        }
        return name.toString();
    }

    /**
     * 断言预算失败不发布部分 root，也不触发任何外部访问。
     */
    private static void assertLimitFailure(
            String xml,
            XmlFrontendTestSupport.FrontendHarness harness) {
        FrontendResult result = XmlFrontendTestSupport.parse(
                harness,
                XmlFrontendTestSupport.xmlSource(xml));

        assertEquals(FrontendStatus.FAILED, result.status());
        assertFalse(result.canonicalRoot().isPresent());
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic ->
                        diagnostic.code() == DiagnosticCode.MIX_FRONTEND_XML_UNSAFE));
        assertEquals(0, harness.externalAccessAttempts());
    }
}
