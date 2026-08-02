package dec.core.compiler.canonical.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendStatus;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.DocumentSource;
import dec.core.context.model.DiagnosticCode;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T05 / I002 的原始编码、typed tag 和 nodePath 来源事实 Oracle。
 */
class YamlFrontendSourceFactsReworkTest {

    /**
     * 非法 continuation、截断、overlong 和 surrogate UTF-8 都必须在 parser 前失败。
     */
    @Test
    void rejectsMalformedUtf8FromRawBytes() {
        List<byte[]> malformedDocuments = Arrays.asList(
                bytes("root: ", 0x80, "\n"),
                bytes("root: ", 0xE2, 0x82),
                bytes("root: ", 0xC0, 0xAF, "\n"),
                bytes("root: ", 0xED, 0xA0, 0x80, "\n"));

        for (byte[] malformed : malformedDocuments) {
            assertUnsafe(source(malformed));
        }
    }

    /**
     * 普通节点不得接受词法与标准 typed tag 不一致的 scalar。
     */
    @Test
    void rejectsInvalidTypedLexemesOnOrdinaryScalar() {
        for (String value : invalidTypedValues()) {
            assertUnsafe("root: " + value + "\n");
        }
    }

    /**
     * `#text` 不得通过非法 typed 词法静默删除或伪造来源事实。
     */
    @Test
    void rejectsInvalidTypedLexemesOnTextScalar() {
        for (String value : invalidTypedValues()) {
            assertUnsafe("root:\n  \"#text\": " + value + "\n");
        }
    }

    /**
     * `@attributes` value 也必须执行相同 typed 词法门禁。
     */
    @Test
    void rejectsInvalidTypedLexemesOnAttributeScalar() {
        for (String value : invalidTypedValues()) {
            assertUnsafe(
                    "root:\n"
                            + "  \"@attributes\":\n"
                            + "    value: " + value + "\n");
        }
    }

    /**
     * Sequence item 形成普通子节点时不能绕过 typed 词法门禁。
     */
    @Test
    void rejectsInvalidTypedLexemesOnSequenceItem() {
        for (String value : invalidTypedValues()) {
            assertUnsafe(
                    "root:\n"
                            + "  item:\n"
                            + "    - " + value + "\n");
        }
    }

    /**
     * 显式字符串 tag 必须保留原始词法且不触发对象构造。
     */
    @Test
    void acceptsExplicitStringTagWithoutObjectConstruction() {
        FrontendResult result = parse("root: !!str attacker-data\n");

        assertEquals(FrontendStatus.PARSED, result.status());
        assertEquals("attacker-data", result.canonicalRoot().get().scalar().get());
    }

    /**
     * 合法显式标准 typed tag 必须通过词法校验并继续保留原始文本。
     */
    @Test
    void acceptsValidExplicitStandardTypedLexemes() {
        FrontendResult result = parse(
                "root:\n"
                        + "  boolValue: !!bool true\n"
                        + "  intValue: !!int 42\n"
                        + "  floatValue: !!float 3.5\n"
                        + "  timestampValue: !!timestamp 2026-08-02\n"
                        + "  nullValue: !!null null\n");

        assertEquals(FrontendStatus.PARSED, result.status());
        List<CanonicalDocumentNode> children = result.canonicalRoot().get().children();
        assertEquals("true", children.get(0).scalar().get());
        assertEquals("42", children.get(1).scalar().get());
        assertEquals("3.5", children.get(2).scalar().get());
        assertEquals("2026-08-02", children.get(3).scalar().get());
        assertFalse(children.get(4).scalar().isPresent());
    }

    /**
     * 路径分隔符、换行、冒号和非法首字符不得成为 Canonical 节点名称。
     */
    @Test
    void rejectsAmbiguousOrNonPortableCanonicalNames() {
        List<String> unsafeNames = Arrays.asList(
                "a/b",
                "line\nbreak",
                "a:b",
                "1name");
        for (String name : unsafeNames) {
            String escaped = name
                    .replace("\\", "\\\\")
                    .replace("\n", "\\n")
                    .replace("\"", "\\\"");
            assertUnsafe(
                    "root:\n"
                            + "  \"" + escaped + "\": value\n");
        }
    }

    /**
     * 合法可移植名称继续生成无歧义的一基 SourceRef 路径。
     */
    @Test
    void preservesPortableNameAndExactNodePath() {
        FrontendResult result = parse(
                "root:\n"
                        + "  child-name_1: value\n");

        assertEquals(FrontendStatus.PARSED, result.status());
        CanonicalDocumentNode child = result.canonicalRoot().get().children().get(0);
        assertEquals("child-name_1", child.name());
        assertEquals("/root/child-name_1", child.sourceRef().nodePath());
    }

    /**
     * 返回 Review 指定的五类非法显式 typed scalar。
     */
    private static List<String> invalidTypedValues() {
        return Arrays.asList(
                "!!null attacker-data",
                "!!int not-an-int",
                "!!bool not-a-bool",
                "!!float not-a-float",
                "!!timestamp not-a-date");
    }

    /**
     * 使用生产 Frontend 解析合法 UTF-8 字符串。
     */
    private static FrontendResult parse(String yaml) {
        return YamlFrontendTestSupport.parse(
                YamlFrontendTestSupport.frontend(),
                YamlFrontendTestSupport.yamlSource(yaml));
    }

    /**
     * 先验证安全控制样本，再验证字符串输入稳定失败且没有部分 root。
     */
    private static void assertUnsafe(String yaml) {
        assertUnsafe(YamlFrontendTestSupport.yamlSource(yaml));
    }

    /**
     * 先验证安全控制样本，再验证原始 Source 稳定失败且没有部分 root。
     */
    private static void assertUnsafe(DocumentSource source) {
        FrontendResult control = parse("root: value\n");
        assertEquals(FrontendStatus.PARSED, control.status());
        assertTrue(control.canonicalRoot().isPresent());

        FrontendResult result = YamlFrontendTestSupport.parse(
                YamlFrontendTestSupport.frontend(),
                source);
        assertEquals(FrontendStatus.FAILED, result.status());
        assertFalse(result.canonicalRoot().isPresent());
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.code()
                        == DiagnosticCode.MIX_FRONTEND_YAML_UNSAFE));
    }

    /**
     * 创建携带原始 byte[] 的 YAML Source，禁止测试辅助层先进行字符串解码。
     */
    private static DocumentSource source(byte[] content) {
        URI root = URI.create("file:/workspace/config/");
        URI uri = URI.create("file:/workspace/config/raw.yaml");
        return new DocumentSource(
                uri.toString(),
                uri,
                DocumentFormat.YAML,
                new AllowedRoot(root),
                content,
                "raw-digest-" + Integer.toHexString(Arrays.hashCode(content)));
    }

    /**
     * 将合法 ASCII 前后缀与指定原始八位字节拼接为测试输入。
     */
    private static byte[] bytes(String prefix, int value, String suffix) {
        return bytes(prefix, new int[] {value}, suffix);
    }

    /**
     * 将合法 ASCII 前缀与指定原始八位字节拼接为测试输入。
     */
    private static byte[] bytes(String prefix, int first, int second) {
        return bytes(prefix, new int[] {first, second}, "");
    }

    /**
     * 将合法 ASCII 前后缀与两个原始八位字节拼接为测试输入。
     */
    private static byte[] bytes(
            String prefix,
            int first,
            int second,
            String suffix) {
        return bytes(prefix, new int[] {first, second}, suffix);
    }

    /**
     * 将合法 ASCII 前后缀与三个原始八位字节拼接为测试输入。
     */
    private static byte[] bytes(
            String prefix,
            int first,
            int second,
            int third,
            String suffix) {
        return bytes(prefix, new int[] {first, second, third}, suffix);
    }

    /**
     * 执行原始 byte[] 拼接，所有值只保留低八位。
     */
    private static byte[] bytes(String prefix, int[] values, String suffix) {
        byte[] left = prefix.getBytes(StandardCharsets.US_ASCII);
        byte[] right = suffix.getBytes(StandardCharsets.US_ASCII);
        byte[] result = new byte[left.length + values.length + right.length];
        System.arraycopy(left, 0, result, 0, left.length);
        for (int index = 0; index < values.length; index++) {
            result[left.length + index] = (byte) values[index];
        }
        System.arraycopy(
                right,
                0,
                result,
                left.length + values.length,
                right.length);
        return result;
    }
}
