package dec.core.compiler.canonical.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.FrontendOptions;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendStatus;
import dec.core.context.model.DiagnosticCode;
import org.junit.jupiter.api.Test;

/**
 * T04 XML Frontend 的 DTD、实体、外部 schema 和失败隔离 Oracle。
 */
class XmlFrontendSecurityTest {

    /**
     * 内部 DOCTYPE 必须在任何外部访问前被拒绝。
     */
    @Test
    void rejectsDoctypeWithoutExternalAccess() {
        assertUnsafe("<!DOCTYPE root [<!ENTITY local \"boom\">]>"
                + "<root>&local;</root>");
    }

    /**
     * 网络外部实体必须失败，且 XMLResolver 访问探针保持 0。
     */
    @Test
    void rejectsNetworkExternalEntityWithoutAccess() {
        assertUnsafe("<!DOCTYPE root [<!ENTITY xxe SYSTEM "
                + "\"https://example.invalid/xxe\">]>"
                + "<root>&xxe;</root>");
    }

    /**
     * 文件外部实体必须失败，且不得尝试读取根目录外文件。
     */
    @Test
    void rejectsFileExternalEntityWithoutAccess() {
        assertUnsafe("<!DOCTYPE root [<!ENTITY xxe SYSTEM "
                + "\"file:///etc/passwd\">]>"
                + "<root>&xxe;</root>");
    }

    /**
     * 外部参数实体必须在 DTD 展开前失败，且不得触发网络访问。
     */
    @Test
    void rejectsExternalParameterEntityWithoutAccess() {
        assertUnsafe("<!DOCTYPE root [<!ENTITY % ext SYSTEM "
                + "\"https://example.invalid/external.dtd\"> %ext;]>"
                + "<root/>");
    }

    /**
     * 外部网络 schema 位置必须直接拒绝，不能触发 SchemaFactory 或网络访问。
     */
    @Test
    void rejectsNetworkSchemaLocationWithoutAccess() {
        assertUnsafe("<root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:noNamespaceSchemaLocation="
                + "\"https://example.invalid/schema.xsd\"/>");
    }

    /**
     * 外部文件 schema 位置必须直接拒绝，不能读取本地文件。
     */
    @Test
    void rejectsFileSchemaLocationWithoutAccess() {
        assertUnsafe("<root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:schemaLocation=\"urn:test file:///etc/passwd\"/>");
    }

    /**
     * Malformed XML 必须返回无部分 Canonical 的稳定失败结果。
     */
    @Test
    void rejectsMalformedXmlWithoutPartialCanonical() {
        assertUnsafe("<root><child></root>");
    }

    /**
     * 非 XML Source 必须在解析字节前受控失败。
     */
    @Test
    void rejectsWrongDocumentFormat() {
        XmlFrontendTestSupport.FrontendHarness harness =
                XmlFrontendTestSupport.frontend();
        FrontendResult result = XmlFrontendTestSupport.parse(
                harness,
                XmlFrontendTestSupport.source("<root/>", DocumentFormat.YAML));

        assertFailure(result);
        assertEquals(0, harness.externalAccessAttempts());
    }

    /**
     * null Source 必须返回稳定失败，不能抛出参数异常或访问外部资源。
     */
    @Test
    void rejectsNullSourceWithStableFailure() {
        XmlFrontendTestSupport.FrontendHarness harness =
                XmlFrontendTestSupport.frontend();
        FrontendResult result = XmlFrontendTestSupport.parse(
                harness,
                null,
                new FrontendOptions("1.0"));

        assertFailure(result);
        assertEquals(0, harness.externalAccessAttempts());
    }

    /**
     * null FrontendOptions 必须返回稳定失败且不发布部分 root。
     */
    @Test
    void rejectsNullOptionsWithStableFailure() {
        XmlFrontendTestSupport.FrontendHarness harness =
                XmlFrontendTestSupport.frontend();
        FrontendResult result = XmlFrontendTestSupport.parse(
                harness,
                XmlFrontendTestSupport.xmlSource("<root/>"),
                null);

        assertFailure(result);
        assertEquals(0, harness.externalAccessAttempts());
    }

    /**
     * 执行不安全输入并断言零外部访问和无部分 root。
     */
    private static void assertUnsafe(String xml) {
        XmlFrontendTestSupport.FrontendHarness harness =
                XmlFrontendTestSupport.frontend();
        FrontendResult result = XmlFrontendTestSupport.parse(
                harness,
                XmlFrontendTestSupport.xmlSource(xml));

        assertFailure(result);
        assertEquals(0, harness.externalAccessAttempts());
    }

    /**
     * 断言稳定 XML 安全失败合同。
     */
    private static void assertFailure(FrontendResult result) {
        assertEquals(FrontendStatus.FAILED, result.status());
        assertFalse(result.canonicalRoot().isPresent());
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic ->
                        diagnostic.code() == DiagnosticCode.MIX_FRONTEND_XML_UNSAFE));
    }
}
