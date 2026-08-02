package dec.core.compiler.canonical.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendStatus;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * T04 安全 XML Frontend 的稳定 Canonical 与 SourceRef Oracle。
 */
class XmlCanonicalContractTest {

    /**
     * 合法 XML 必须产生属性稳定、子节点有序且来源精确的 Canonical 树。
     */
    @Test
    void parsesStableCanonicalTreeAndSourceRefs() {
        String xml = "<cfg z=\"2\" a=\"1\">\n"
                + "  <first code=\"x\"> alpha </first>\n"
                + "  <second>\n"
                + "    <leaf/>\n"
                + "  </second>\n"
                + "</cfg>";
        XmlFrontendTestSupport.FrontendHarness harness =
                XmlFrontendTestSupport.frontend();

        FrontendResult result = XmlFrontendTestSupport.parse(
                harness,
                XmlFrontendTestSupport.xmlSource(xml));

        assertEquals(FrontendStatus.PARSED, result.status());
        assertTrue(result.canonicalRoot().isPresent());
        assertTrue(result.diagnostics().isEmpty());
        assertEquals(0, harness.externalAccessAttempts());

        CanonicalDocumentNode root = result.canonicalRoot().get();
        assertEquals("cfg", root.name());
        assertEquals(Arrays.asList("a", "z"),
                new ArrayList<String>(root.attributes().keySet()));
        assertEquals("1", root.attributes().get("a"));
        assertEquals("2", root.attributes().get("z"));
        assertFalse(root.scalar().isPresent());
        assertEquals(DocumentFormat.XML, root.format());
        assertEquals("1.0", root.schemaVersion());
        assertSource(root, 1, 1, "/cfg");

        CanonicalDocumentNode first = root.children().get(0);
        assertEquals("first", first.name());
        assertEquals("alpha", first.scalar().get());
        assertEquals("x", first.attributes().get("code"));
        assertEquals("1.0", first.schemaVersion());
        assertSource(first, 2, 3, "/cfg/first");

        CanonicalDocumentNode second = root.children().get(1);
        assertEquals("second", second.name());
        assertFalse(second.scalar().isPresent());
        assertEquals("1.0", second.schemaVersion());
        assertSource(second, 3, 3, "/cfg/second");
        CanonicalDocumentNode leaf = second.children().get(0);
        assertEquals("leaf", leaf.name());
        assertEquals("1.0", leaf.schemaVersion());
        assertSource(leaf, 4, 5, "/cfg/second/leaf");
    }

    /**
     * 普通文本、CDATA 和后续普通文本必须按文档顺序拼接后统一 trim。
     */
    @Test
    void concatenatesTextAndCdataInDocumentOrder() {
        String xml = "<root> before <![CDATA[<middle>]]> after </root>";
        FrontendResult result = XmlFrontendTestSupport.parse(
                XmlFrontendTestSupport.frontend(),
                XmlFrontendTestSupport.xmlSource(xml));

        assertEquals(FrontendStatus.PARSED, result.status());
        assertEquals("before <middle> after",
                result.canonicalRoot().get().scalar().get());
    }

    /**
     * 命名空间前缀不得进入 Canonical 名称或属性 key。
     */
    @Test
    void canonicalizesQualifiedNamesToLocalNames() {
        String xml = "<x:root xmlns:x=\"urn:test\">"
                + "<x:child x:code=\"1\"/>"
                + "</x:root>";
        FrontendResult result = XmlFrontendTestSupport.parse(
                XmlFrontendTestSupport.frontend(),
                XmlFrontendTestSupport.xmlSource(xml));

        CanonicalDocumentNode root = result.canonicalRoot().get();
        assertEquals("root", root.name());
        assertTrue(root.attributes().isEmpty());
        assertEquals("child", root.children().get(0).name());
        assertEquals("1", root.children().get(0).attributes().get("code"));
        assertEquals("/root/child", root.children().get(0).sourceRef().nodePath());
    }

    /**
     * CRLF 和 CR 文档都必须把 SourceRef 列定位到 start tag 的小于号。
     */
    @Test
    void locatesStartTagsAcrossCrLfAndCr() {
        assertChildPosition("<root>\r\n  <child/>\r\n</root>");
        assertChildPosition("<root>\r  <child/>\r</root>");
    }

    /**
     * 验证固定 child 位置。
     */
    private static void assertChildPosition(String xml) {
        FrontendResult result = XmlFrontendTestSupport.parse(
                XmlFrontendTestSupport.frontend(),
                XmlFrontendTestSupport.xmlSource(xml));
        assertSource(result.canonicalRoot().get().children().get(0),
                2,
                3,
                "/root/child");
    }

    /**
     * 断言节点完整来源事实。
     */
    private static void assertSource(
            CanonicalDocumentNode node,
            int line,
            int column,
            String nodePath) {
        assertEquals("file:/workspace/config/root.xml", node.sourceRef().sourceId());
        assertEquals(line, node.sourceRef().line());
        assertEquals(column, node.sourceRef().column());
        assertEquals(nodePath, node.sourceRef().nodePath());
    }
}
