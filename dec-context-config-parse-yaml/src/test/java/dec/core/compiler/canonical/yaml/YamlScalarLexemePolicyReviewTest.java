package dec.core.compiler.canonical.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendStatus;
import dec.core.context.model.DiagnosticCode;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * I002 独立 Security/Code Review 增补的标准 typed scalar 近似词法 Oracle。
 */
class YamlScalarLexemePolicyReviewTest {

    /**
     * 看似接近合法格式但无法表达对应类型事实的词法必须 fail closed。
     */
    @Test
    void rejectsTypedScalarNearMisses() {
        List<String> nearMisses = Arrays.asList(
                "!!int 09",
                "!!float .",
                "!!timestamp 2026-02-31",
                "!!timestamp 2026-08-02T24:00:00Z",
                "!!timestamp 2026-08-02T12:00:00+24:00",
                "!!bool truth",
                "!!null Nullish");

        for (String value : nearMisses) {
            FrontendResult control = parse("root: value\n");
            assertEquals(FrontendStatus.PARSED, control.status());
            assertTrue(control.canonicalRoot().isPresent());

            FrontendResult result = parse("root: " + value + "\n");
            assertEquals(FrontendStatus.FAILED, result.status());
            assertFalse(result.canonicalRoot().isPresent());
            assertTrue(result.diagnostics().stream()
                    .anyMatch(diagnostic -> diagnostic.code()
                            == DiagnosticCode.MIX_FRONTEND_YAML_UNSAFE));
        }
    }

    /**
     * 使用生产 Frontend 解析 YAML fixture。
     */
    private static FrontendResult parse(String yaml) {
        return YamlFrontendTestSupport.parse(
                YamlFrontendTestSupport.frontend(),
                YamlFrontendTestSupport.yamlSource(yaml));
    }
}
