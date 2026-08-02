package dec.core.compiler.canonical.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendStatus;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T05 / I003 的 scalar 前置资源门禁与 SnakeYAML 2.2 Resolver Oracle。
 *
 * <p>资源顺序使用小型注入预算和稳定 messageKey 证明，不使用真实 OOM、
 * 大型性能输入或执行时间阈值。</p>
 */
class YamlScalarBudgetResolverReworkTest {
    private static final String SCALAR_LIMIT_KEY =
            "yaml.frontend.limit.scalar-per-node";
    private static final String INVALID_LEXEME_KEY =
            "yaml.frontend.scalar.invalid-lexeme";

    /**
     * 普通节点 scalar 必须先检查原始长度，再执行 typed 词法校验。
     */
    @Test
    void scalarLimitPrecedesLexemeValidationForOrdinaryScalar() {
        assertScalarLimitPrecedence(ScalarLocation.ORDINARY);
    }

    /**
     * `#text` scalar 必须先检查原始长度，再执行 typed 词法校验。
     */
    @Test
    void scalarLimitPrecedesLexemeValidationForTextScalar() {
        assertScalarLimitPrecedence(ScalarLocation.TEXT);
    }

    /**
     * 属性 value 必须先检查原始长度，再执行 typed 词法校验。
     */
    @Test
    void scalarLimitPrecedesLexemeValidationForAttributeScalar() {
        assertScalarLimitPrecedence(ScalarLocation.ATTRIBUTE);
    }

    /**
     * Sequence item 必须先检查原始长度，再执行 typed 词法校验。
     */
    @Test
    void scalarLimitPrecedesLexemeValidationForSequenceItem() {
        assertScalarLimitPrecedence(ScalarLocation.SEQUENCE);
    }

    /**
     * 普通节点必须接受 SnakeYAML 2.2 Resolver 的无符号指数形式。
     */
    @Test
    void acceptsResolverFloatFormsForOrdinaryScalar() {
        assertResolverFloatForms(ScalarLocation.ORDINARY);
    }

    /**
     * `#text` 必须接受 SnakeYAML 2.2 Resolver 的无符号指数形式。
     */
    @Test
    void acceptsResolverFloatFormsForTextScalar() {
        assertResolverFloatForms(ScalarLocation.TEXT);
    }

    /**
     * 属性 value 必须接受 SnakeYAML 2.2 Resolver 的无符号指数形式。
     */
    @Test
    void acceptsResolverFloatFormsForAttributeScalar() {
        assertResolverFloatForms(ScalarLocation.ATTRIBUTE);
    }

    /**
     * Sequence item 必须接受 SnakeYAML 2.2 Resolver 的无符号指数形式。
     */
    @Test
    void acceptsResolverFloatFormsForSequenceItem() {
        assertResolverFloatForms(ScalarLocation.SEQUENCE);
    }

    /**
     * 普通节点必须拒绝没有真实进制数位的显式整数。
     */
    @Test
    void rejectsResolverInvalidIntegersForOrdinaryScalar() {
        assertResolverInvalidIntegers(ScalarLocation.ORDINARY);
    }

    /**
     * `#text` 必须拒绝没有真实进制数位的显式整数。
     */
    @Test
    void rejectsResolverInvalidIntegersForTextScalar() {
        assertResolverInvalidIntegers(ScalarLocation.TEXT);
    }

    /**
     * 属性 value 必须拒绝没有真实进制数位的显式整数。
     */
    @Test
    void rejectsResolverInvalidIntegersForAttributeScalar() {
        assertResolverInvalidIntegers(ScalarLocation.ATTRIBUTE);
    }

    /**
     * Sequence item 必须拒绝没有真实进制数位的显式整数。
     */
    @Test
    void rejectsResolverInvalidIntegersForSequenceItem() {
        assertResolverInvalidIntegers(ScalarLocation.SEQUENCE);
    }

    /**
     * 使用八字符单值预算验证合法与非法超限 typed scalar 都优先命中资源门禁。
     */
    private static void assertScalarLimitPrecedence(ScalarLocation location) {
        DocumentFrontend frontend = frontendWithScalarLimit(8);
        assertParsed(frontend, location.yaml("value"));

        assertFailure(
                frontend,
                location.yaml("!!float 123456789.1"),
                SCALAR_LIMIT_KEY);
        assertFailure(
                frontend,
                location.yaml("!!float not-a-number"),
                SCALAR_LIMIT_KEY);
    }

    /**
     * 验证隐式及显式 float 与 SnakeYAML 2.2 Resolver 的指数语法一致。
     */
    private static void assertResolverFloatForms(ScalarLocation location) {
        assertCanonicalValue(location, "1e3", "1e3");
        assertCanonicalValue(location, "1.2e3", "1.2e3");
        assertCanonicalValue(location, "!!float 1e3", "1e3");
    }

    /**
     * 验证二、十六和八进制前缀后必须包含至少一个真实数位。
     */
    private static void assertResolverInvalidIntegers(ScalarLocation location) {
        assertParsed(YamlFrontendTestSupport.frontend(), location.yaml("value"));
        List<String> invalidValues = Arrays.asList(
                "!!int 0b_",
                "!!int 0x_",
                "!!int 0_");
        for (String value : invalidValues) {
            assertFailure(
                    YamlFrontendTestSupport.frontend(),
                    location.yaml(value),
                    INVALID_LEXEME_KEY);
        }
    }

    /**
     * 解析指定位置并断言 Canonical 保留预期原始词法。
     */
    private static void assertCanonicalValue(
            ScalarLocation location,
            String yamlValue,
            String expectedCanonicalValue) {
        FrontendResult result = parse(
                YamlFrontendTestSupport.frontend(),
                location.yaml(yamlValue));

        assertEquals(FrontendStatus.PARSED, result.status());
        assertTrue(result.canonicalRoot().isPresent());
        assertEquals(
                expectedCanonicalValue,
                location.canonicalValue(result.canonicalRoot().get()));
    }

    /**
     * 断言目标输入以指定稳定 messageKey 失败且不发布部分 root。
     */
    private static void assertFailure(
            DocumentFrontend frontend,
            String yaml,
            String expectedMessageKey) {
        FrontendResult result = parse(frontend, yaml);

        assertEquals(FrontendStatus.FAILED, result.status());
        assertFalse(result.canonicalRoot().isPresent());
        assertEquals(1, result.diagnostics().size());
        Diagnostic diagnostic = result.diagnostics().get(0);
        assertEquals(DiagnosticCode.MIX_FRONTEND_YAML_UNSAFE, diagnostic.code());
        assertEquals(expectedMessageKey, diagnostic.messageKey());
    }

    /**
     * 断言安全控制样本可由同一 Frontend 正常发布。
     */
    private static void assertParsed(
            DocumentFrontend frontend,
            String yaml) {
        FrontendResult result = parse(frontend, yaml);
        assertEquals(FrontendStatus.PARSED, result.status());
        assertTrue(result.canonicalRoot().isPresent());
    }

    /**
     * 创建仅收紧单 scalar 上限的测试 Frontend。
     */
    private static DocumentFrontend frontendWithScalarLimit(int scalarLimit) {
        return YamlFrontendTestSupport.frontendWithLimits(
                1_024,
                1_024,
                16,
                32,
                2_048L,
                32,
                32,
                scalarLimit,
                2_048L,
                0);
    }

    /**
     * 使用固定 YAML Source 执行一次解析。
     */
    private static FrontendResult parse(
            DocumentFrontend frontend,
            String yaml) {
        return YamlFrontendTestSupport.parse(
                frontend,
                YamlFrontendTestSupport.yamlSource(yaml));
    }

    /**
     * 四种 Canonical scalar 入口及其固定 YAML fixture。
     */
    private enum ScalarLocation {
        ORDINARY,
        TEXT,
        ATTRIBUTE,
        SEQUENCE;

        /**
         * 将 scalar 词法放入当前入口。
         */
        private String yaml(String value) {
            switch (this) {
                case ORDINARY:
                    return "root: " + value + "\n";
                case TEXT:
                    return "root:\n"
                            + "  \"#text\": " + value + "\n";
                case ATTRIBUTE:
                    return "root:\n"
                            + "  \"@attributes\":\n"
                            + "    value: " + value + "\n";
                case SEQUENCE:
                    return "root:\n"
                            + "  item:\n"
                            + "    - " + value + "\n";
                default:
                    throw new AssertionError("Unknown scalar location: " + this);
            }
        }

        /**
         * 从对应 Canonical 位置读取保存的 scalar 词法。
         */
        private String canonicalValue(CanonicalDocumentNode root) {
            switch (this) {
                case ORDINARY:
                case TEXT:
                    return root.scalar().get();
                case ATTRIBUTE:
                    return root.attributes().get("value");
                case SEQUENCE:
                    return root.children().get(0).scalar().get();
                default:
                    throw new AssertionError("Unknown scalar location: " + this);
            }
        }
    }
}
