package dec.core.compiler.source;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.DiagnosticCode;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * I004 独立 Review 驱动的相对 URI 类别保持与 Resolver 根边界 Oracle。
 */
class SourceReferenceRelativeCategoryReworkTest {

    /**
     * 验证删除字面量或编码当前目录段后，引用仍保持原始相对 URI 类别。
     */
    @Test
    void preservesRelativeUriCategoryAfterLeadingCurrentDirectoryCanonicalization() {
        for (String input : promotableRelativeReferences()) {
            URI original = URI.create(input);
            SourceReference canonical = new SourceReference(input);
            URI canonicalUri = URI.create(canonical.value());

            assertFalse(original.isAbsolute(), input);
            assertFalse(
                    canonicalUri.isAbsolute(),
                    input + " -> " + canonical.value());
        }
    }

    /**
     * 验证可被错误提升的相对根在 Provider 调用前统一映射为路径越界失败。
     */
    @Test
    void rejectsPromotableRelativeRootsBeforeProviderAccess() {
        for (String input : promotableRelativeReferences()) {
            CountingProvider provider = new CountingProvider();
            SourceGraphResolutionResult result = assertDoesNotThrow(
                    () -> new MixSourceResolver().resolve(
                            new SourceReference(input),
                            provider,
                            policyFor(input)));

            assertEquals(SourceGraphResolutionStatus.FAILED, result.status());
            assertFalse(result.graph().isPresent());
            assertTrue(result.diagnostics().stream()
                    .anyMatch(diagnostic -> diagnostic.code()
                            == DiagnosticCode.MIX_SOURCE_PATH_ESCAPE));
            assertEquals(0, provider.accessCount());
        }
    }

    /**
     * 验证既有绝对 URI 点段规范化仍保持绝对类别和统一 canonical key。
     */
    @Test
    void keepsAbsoluteUriCategoryForExistingCanonicalReferences() {
        assertAbsoluteCanonical(
                "classpath:mix/%2e/orm-config.xml",
                "classpath:mix/orm-config.xml");
        assertAbsoluteCanonical(
                "file:/mix/./root.xml",
                "file:/mix/root.xml");
    }

    /**
     * 验证空 Provider 继续通过 Resolver 稳定失败结果表达，而不是抛出异常。
     */
    @Test
    void mapsNullProviderToStableFailedResult() {
        SourceGraphResolutionResult result = assertDoesNotThrow(
                () -> new MixSourceResolver().resolve(
                        new SourceReference("classpath:mix/orm-config.xml"),
                        null,
                        classpathPolicy()));

        assertEquals(SourceGraphResolutionStatus.FAILED, result.status());
        assertFalse(result.graph().isPresent());
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.code()
                        == DiagnosticCode.MIX_SOURCE_POLICY));
    }

    /**
     * 断言绝对引用规范化前后均为绝对 URI，并产生预期 canonical key。
     */
    private static void assertAbsoluteCanonical(String input, String expected) {
        SourceReference canonical = new SourceReference(input);
        assertTrue(URI.create(input).isAbsolute(), input);
        assertTrue(URI.create(canonical.value()).isAbsolute(), canonical.value());
        assertEquals(expected, canonical.value());
    }

    /**
     * 返回会因删除前导当前目录段而被错误提升的固定相对引用。
     */
    private static List<String> promotableRelativeReferences() {
        return Arrays.asList(
                "./classpath:mix/orm-config.xml",
                "%2e/classpath:mix/orm-config.xml",
                "./file:/mix/root.xml",
                "%2E/file:/mix/root.xml");
    }

    /**
     * 根据目标文本选择允许该绝对形式的策略，确保测试只验证相对类别门禁。
     */
    private static SourcePolicy policyFor(String input) {
        return input.contains("classpath:")
                ? classpathPolicy()
                : filePolicy();
    }

    /**
     * 返回 classpath mix 根策略。
     */
    private static SourcePolicy classpathPolicy() {
        return new SourcePolicy(
                Collections.singleton("classpath"),
                new AllowedRoot(URI.create("classpath:mix/")),
                3,
                20,
                1024L * 1024L);
    }

    /**
     * 返回 file mix 根策略。
     */
    private static SourcePolicy filePolicy() {
        return new SourcePolicy(
                Collections.singleton("file"),
                new AllowedRoot(URI.create("file:/mix/")),
                3,
                20,
                1024L * 1024L);
    }

    /**
     * 记录所有 Provider 访问；测试期返回 null 以暴露任何错误的提前访问。
     */
    private static final class CountingProvider implements DocumentSourceProvider {
        private int accessCount;

        @Override
        public SourceResolutionResult resolve(
                SourceReference reference,
                SourceResolutionContext context) {
            accessCount++;
            return null;
        }

        @Override
        public SourceResolutionResult resolveFileSet(
                SourceReference reference,
                SourceResolutionContext context) {
            accessCount++;
            return null;
        }

        /**
         * 返回 Provider 被调用的总次数。
         */
        private int accessCount() {
            return accessCount;
        }
    }
}
