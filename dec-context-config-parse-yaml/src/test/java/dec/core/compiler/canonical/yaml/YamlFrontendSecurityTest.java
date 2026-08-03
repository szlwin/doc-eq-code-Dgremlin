package dec.core.compiler.canonical.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendOptions;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendStatus;
import dec.core.compiler.source.DocumentSource;
import dec.core.context.model.DiagnosticCode;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * T05 YAML tag、对象构造、别名、递归和文档结构安全 Oracle。
 */
class YamlFrontendSecurityTest {

    @BeforeEach
    void resetObjectConstructionProbe() {
        ExploitProbe.reset();
    }

    /**
     * 安全最小 Mapping 必须被解析为 YAML Canonical 根。
     */
    @Test
    void parsesSafeYamlWithoutObjectConstruction() {
        DocumentFrontend frontend = YamlFrontendTestSupport.frontend();

        FrontendResult result = YamlFrontendTestSupport.parse(
                frontend,
                YamlFrontendTestSupport.yamlSource(
                        "root:\n  child: value\n"));

        assertSafe(result);
        assertEquals(DocumentFormat.YAML, result.canonicalRoot().get().format());
        assertEquals(0, ExploitProbe.constructions());
    }

    /**
     * 任意 Java/object tag 必须在用户类型构造前失败。
     */
    @Test
    void rejectsJavaObjectTagWithoutInstantiatingType() {
        String typeName = ExploitProbe.class.getName();
        assertYamlFailure("root: !!" + typeName + " {}\n");
        assertEquals(0, ExploitProbe.constructions());
    }

    /**
     * local/custom tag 不属于冻结 Scalar/Mapping/Sequence tag 集合。
     */
    @Test
    void rejectsLocalCustomTag() {
        assertYamlFailure("root: !custom value\n");
    }

    /**
     * anchor 和 alias 会形成共享图，Canonical 树必须 fail closed。
     */
    @Test
    void rejectsAnchorAndAliasGraph() {
        assertYamlFailure(
                "root:\n"
                        + "  first: &shared\n"
                        + "    value: x\n"
                        + "  second: *shared\n");
    }

    /**
     * 递归 alias 不得进入 Canonical 遍历。
     */
    @Test
    void rejectsRecursiveAlias() {
        assertYamlFailure("root: &loop [*loop]\n");
    }

    /**
     * duplicate key 不得通过最后写入覆盖改变 Canonical 事实。
     */
    @Test
    void rejectsDuplicateKeys() {
        assertYamlFailure(
                "root:\n"
                        + "  child: first\n"
                        + "  child: second\n");
    }

    /**
     * YAML merge key 会隐式复制结构，当前 Canonical 合同明确拒绝。
     */
    @Test
    void rejectsMergeKeys() {
        assertYamlFailure(
                "root:\n"
                        + "  base: &base\n"
                        + "    value: x\n"
                        + "  merged:\n"
                        + "    <<: *base\n");
    }

    /**
     * malformed YAML 必须受控失败且不发布部分根。
     */
    @Test
    void rejectsMalformedYamlWithoutPartialRoot() {
        assertYamlFailure("root: [one, two\n");
    }

    /**
     * null source、null options 和错误格式必须使用同一稳定失败边界。
     */
    @Test
    void rejectsInvalidFrontendArguments() {
        DocumentFrontend frontend = YamlFrontendTestSupport.frontend();

        assertSafe(YamlFrontendTestSupport.parse(
                frontend,
                YamlFrontendTestSupport.yamlSource("root: value\n")));
        assertFailure(frontend.parse(null, new FrontendOptions("1.0")));
        assertFailure(frontend.parse(
                YamlFrontendTestSupport.yamlSource("root: value\n"),
                null));
        DocumentSource xmlSource = YamlFrontendTestSupport.source(
                "<root/>",
                DocumentFormat.XML,
                "root.xml");
        assertFailure(frontend.parse(xmlSource, new FrontendOptions("1.0")));
    }

    /**
     * 空、多 document、非 Mapping root、多 root key 和复杂 key 均违反冻结文档合同。
     */
    @Test
    void rejectsInvalidDocumentShapes() {
        assertYamlFailure("");
        assertYamlFailure("- one\n- two\n");
        assertYamlFailure("first: one\nsecond: two\n");
        assertYamlFailure("root: one\n---\nother: two\n");
        assertYamlFailure("? [root, other]\n: value\n");
    }

    /**
     * 先证明同一 Frontend 能解析安全控制样本，再断言目标输入失败。
     * 该控制样本防止“拒绝所有 YAML”的伪安全实现通过负向 Oracle。
     */
    private static void assertYamlFailure(String yaml) {
        DocumentFrontend frontend = YamlFrontendTestSupport.frontend();
        assertSafe(YamlFrontendTestSupport.parse(
                frontend,
                YamlFrontendTestSupport.yamlSource("root: value\n")));
        FrontendResult result = YamlFrontendTestSupport.parse(
                frontend,
                YamlFrontendTestSupport.yamlSource(yaml));
        assertFailure(result);
    }

    /**
     * 安全控制样本必须发布唯一 Canonical 根且不携带错误。
     */
    private static void assertSafe(FrontendResult result) {
        assertEquals(FrontendStatus.PARSED, result.status());
        assertTrue(result.canonicalRoot().isPresent());
        assertTrue(result.diagnostics().stream()
                .noneMatch(diagnostic -> diagnostic.code()
                        == DiagnosticCode.MIX_FRONTEND_YAML_UNSAFE));
    }

    /**
     * 所有 YAML 安全失败都不得携带部分 Canonical root。
     */
    private static void assertFailure(FrontendResult result) {
        assertEquals(FrontendStatus.FAILED, result.status());
        assertFalse(result.canonicalRoot().isPresent());
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.code()
                        == DiagnosticCode.MIX_FRONTEND_YAML_UNSAFE));
    }

    /**
     * 若通用 YAML 对象构造器被调用，该探针会留下可观测副作用。
     */
    public static final class ExploitProbe {
        private static final AtomicInteger CONSTRUCTIONS = new AtomicInteger();

        /**
         * 用户类型构造器绝不应被安全 Frontend 调用。
         */
        public ExploitProbe() {
            CONSTRUCTIONS.incrementAndGet();
        }

        static void reset() {
            CONSTRUCTIONS.set(0);
        }

        static int constructions() {
            return CONSTRUCTIONS.get();
        }
    }
}
