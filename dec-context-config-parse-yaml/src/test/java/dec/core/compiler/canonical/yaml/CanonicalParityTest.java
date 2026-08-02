package dec.core.compiler.canonical.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendOptions;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendStatus;
import dec.core.compiler.canonical.xml.SecureXmlDocumentFrontend;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * T05 同语义 XML/YAML Canonical 树与 YAML SourceRef Oracle。
 */
class CanonicalParityTest {

    /**
     * 属性、直接文本和重复子节点必须与等价 XML 形成相同语义树。
     */
    @Test
    void producesSemanticTreeEquivalentToXml() {
        String xml = "<root id=\"7\"><child kind=\"x\">hello</child>"
                + "<item>one</item><item>two</item></root>";
        String yaml = "root:\n"
                + "  \"@attributes\":\n"
                + "    id: \"7\"\n"
                + "  child:\n"
                + "    \"@attributes\":\n"
                + "      kind: x\n"
                + "    \"#text\": hello\n"
                + "  item:\n"
                + "    - one\n"
                + "    - two\n";

        CanonicalDocumentNode xmlRoot = parseXml(xml);
        CanonicalDocumentNode yamlRoot = parseYaml(yaml);

        assertSemanticTree(xmlRoot, yamlRoot);
        assertFormatRecursively(xmlRoot, DocumentFormat.XML);
        assertFormatRecursively(yamlRoot, DocumentFormat.YAML);
    }

    /**
     * 注释、属性输入顺序和保留 key 顺序变化不得改变 YAML Canonical 语义。
     */
    @Test
    void ignoresCommentsAndCanonicalizesAttributeOrder() {
        String first = "root:\n"
                + "  \"@attributes\":\n"
                + "    z: last\n"
                + "    a: first\n"
                + "  \"#text\": value\n";
        String second = "# document comment\n"
                + "root:\n"
                + "  \"#text\": value\n"
                + "  \"@attributes\": {a: first, z: last}\n";

        CanonicalDocumentNode firstRoot = parseYaml(first);
        CanonicalDocumentNode secondRoot = parseYaml(second);

        assertSemanticTree(firstRoot, secondRoot);
        assertEquals("first", firstRoot.attributes().get("a"));
        assertEquals("last", firstRoot.attributes().get("z"));
    }

    /**
     * YAML key/item 位置必须转换为一基 SourceRef，并保留完整 nodePath。
     */
    @Test
    void preservesYamlKeyAndSequenceItemSourceRefs() {
        String yaml = "root:\n"
                + "  \"@attributes\":\n"
                + "    id: \"7\"\n"
                + "  child:\n"
                + "    \"#text\": hello\n"
                + "  item:\n"
                + "    - one\n"
                + "    - two\n";

        CanonicalDocumentNode root = parseYaml(yaml);
        List<CanonicalDocumentNode> children = root.children();

        assertEquals(1, root.sourceRef().line());
        assertEquals(1, root.sourceRef().column());
        assertEquals("/root", root.sourceRef().nodePath());
        assertEquals(4, children.get(0).sourceRef().line());
        assertEquals(3, children.get(0).sourceRef().column());
        assertEquals("/root/child", children.get(0).sourceRef().nodePath());
        assertEquals(7, children.get(1).sourceRef().line());
        assertEquals(7, children.get(1).sourceRef().column());
        assertEquals("/root/item", children.get(1).sourceRef().nodePath());
        assertEquals(8, children.get(2).sourceRef().line());
        assertEquals(7, children.get(2).sourceRef().column());
        assertEquals("/root/item", children.get(2).sourceRef().nodePath());
    }

    /**
     * 使用 T04 安全 XML Frontend 解析等价 fixture。
     */
    private static CanonicalDocumentNode parseXml(String xml) {
        DocumentFrontend frontend = new SecureXmlDocumentFrontend();
        FrontendResult result = frontend.parse(
                YamlFrontendTestSupport.source(
                        xml,
                        DocumentFormat.XML,
                        "root.xml"),
                new FrontendOptions("1.0"));
        assertEquals(FrontendStatus.PARSED, result.status());
        assertTrue(result.canonicalRoot().isPresent());
        return result.canonicalRoot().get();
    }

    /**
     * 使用 T05 目标 Frontend 解析 YAML fixture。
     */
    private static CanonicalDocumentNode parseYaml(String yaml) {
        DocumentFrontend frontend = YamlFrontendTestSupport.frontend();
        FrontendResult result = YamlFrontendTestSupport.parse(
                frontend,
                YamlFrontendTestSupport.yamlSource(yaml));
        assertEquals(FrontendStatus.PARSED, result.status());
        assertTrue(result.canonicalRoot().isPresent());
        return result.canonicalRoot().get();
    }

    /**
     * 比较格式中立语义；格式和物理位置由单独 Oracle 验证。
     */
    private static void assertSemanticTree(
            CanonicalDocumentNode expected,
            CanonicalDocumentNode actual) {
        assertEquals(expected.name(), actual.name());
        assertEquals(expected.attributes(), actual.attributes());
        assertEquals(expected.scalar(), actual.scalar());
        assertEquals(expected.schemaVersion(), actual.schemaVersion());
        assertEquals(expected.sourceRef().nodePath(), actual.sourceRef().nodePath());
        assertEquals(expected.children().size(), actual.children().size());
        for (int index = 0; index < expected.children().size(); index++) {
            assertSemanticTree(
                    expected.children().get(index),
                    actual.children().get(index));
        }
    }

    /**
     * 确认整个树由单一 Frontend 格式产生。
     */
    private static void assertFormatRecursively(
            CanonicalDocumentNode node,
            DocumentFormat format) {
        assertEquals(format, node.format());
        for (CanonicalDocumentNode child : node.children()) {
            assertFormatRecursively(child, format);
        }
    }
}
