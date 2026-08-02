package dec.core.compiler.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.DiagnosticCode;
import java.net.URI;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * 验证 SourcePolicy 在 Provider 访问前拒绝不安全引用和资源预算超限。
 */
class SourcePolicySecurityTest {
    @Test
    void rejectsTraversalAndUnknownSchemeBeforeProviderAccess() {
        SourceTestFixture.InMemoryProvider traversalProvider =
                SourceTestFixture.provider(SourceTestFixture.FileSetOrder.FORWARD);
        SourceGraphResolutionResult traversal = new MixSourceResolver().resolve(
                new SourceReference("classpath:mix/../secret.xml"),
                traversalProvider,
                SourceTestFixture.policy());

        assertFailedWith(traversal, DiagnosticCode.MIX_SOURCE_PATH_ESCAPE);
        assertEquals(0, traversalProvider.accessCount());

        SourceTestFixture.InMemoryProvider networkProvider =
                SourceTestFixture.provider(SourceTestFixture.FileSetOrder.FORWARD);
        SourceGraphResolutionResult network = new MixSourceResolver().resolve(
                new SourceReference("https://example.invalid/mix.xml"),
                networkProvider,
                SourceTestFixture.policy());

        assertFailedWith(network, DiagnosticCode.MIX_SOURCE_PATH_ESCAPE);
        assertEquals(0, networkProvider.accessCount());
    }

    @Test
    void enforcesDepthBeforeResolvingRuleSources() {
        SourcePolicy shallow = new SourcePolicy(
                Collections.singleton("classpath"),
                new AllowedRoot(URI.create("classpath:mix/")),
                1,
                20,
                1024L * 1024L);
        SourceTestFixture.InMemoryProvider provider = SourceTestFixture.provider(
                SourceTestFixture.FileSetOrder.FORWARD);

        SourceGraphResolutionResult result = new MixSourceResolver().resolve(
                new SourceReference(SourceTestFixture.ROOT),
                provider,
                shallow);

        assertFailedWith(result, DiagnosticCode.MIX_SOURCE_POLICY);
        assertTrue(provider.accessCount() < 10);
    }

    @Test
    void enforcesSourceCountAndTotalByteBudgetsWithoutPartialGraph() {
        SourcePolicy countLimited = new SourcePolicy(
                Collections.singleton("classpath"),
                new AllowedRoot(URI.create("classpath:mix/")),
                3,
                5,
                1024L * 1024L);
        SourceGraphResolutionResult countResult = new MixSourceResolver().resolve(
                new SourceReference(SourceTestFixture.ROOT),
                SourceTestFixture.provider(SourceTestFixture.FileSetOrder.FORWARD),
                countLimited);
        assertFailedWith(countResult, DiagnosticCode.MIX_SOURCE_POLICY);

        SourcePolicy byteLimited = new SourcePolicy(
                Collections.singleton("classpath"),
                new AllowedRoot(URI.create("classpath:mix/")),
                3,
                20,
                16L);
        SourceGraphResolutionResult byteResult = new MixSourceResolver().resolve(
                new SourceReference(SourceTestFixture.ROOT),
                SourceTestFixture.provider(SourceTestFixture.FileSetOrder.FORWARD),
                byteLimited);
        assertFailedWith(byteResult, DiagnosticCode.MIX_SOURCE_POLICY);
    }

    /**
     * 断言失败结果包含指定 ERROR，且不暴露部分 SourceGraph。
     */
    private static void assertFailedWith(
            SourceGraphResolutionResult result,
            DiagnosticCode code) {
        assertEquals(SourceGraphResolutionStatus.FAILED, result.status());
        assertFalse(result.graph().isPresent());
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.code() == code));
    }
}
