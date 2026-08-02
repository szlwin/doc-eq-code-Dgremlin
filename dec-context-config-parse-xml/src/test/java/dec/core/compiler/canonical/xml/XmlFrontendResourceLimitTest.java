package dec.core.compiler.canonical.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendStatus;
import dec.core.context.model.DiagnosticCode;
import org.junit.jupiter.api.Test;

/**
 * T04 I002 的 XML 深度、节点、路径和文本资源预算 Oracle。
 *
 * <p>全部测试使用小型输入和注入预算，不通过真实 OOM 证明安全。</p>
 */
class XmlFrontendResourceLimitTest {

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
