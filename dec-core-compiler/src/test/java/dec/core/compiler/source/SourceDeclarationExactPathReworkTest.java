package dec.core.compiler.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import org.junit.jupiter.api.Test;

/**
 * I005 独立 Review 驱动的完整 XML 声明路径与忽略子树 Oracle。
 */
class SourceDeclarationExactPathReworkTest {

    /**
     * 错误 root 即使包含四类同名声明，也不得生成真实 Source 边。
     */
    @Test
    void rejectsWrongRootBeforeAccessingDeclaredTargets() {
        SourceTestFixture.InMemoryProvider provider = providerWithRoot(
                wrongRootXml());

        SourceGraphResolutionResult result = resolve(provider);

        assertPolicyFailure(result);
        assertEquals(1, provider.accessCount());
    }

    /**
     * 正确 root 下错误嵌套的四类声明必须全部忽略，并由完整性门禁阻断。
     */
    @Test
    void rejectsDeclarationsNestedUnderIgnoredRootSubtree() {
        SourceTestFixture.InMemoryProvider provider = providerWithRoot(
                wrongRootNestingXml());

        SourceGraphResolutionResult result = resolve(provider);

        assertPolicyFailure(result);
        assertEquals(1, provider.accessCount());
    }

    /**
     * 合法直接声明旁的同名 ignored subtree 不得制造重复边或改变解析结果。
     */
    @Test
    void ignoresSameNameDeclarationsOutsideFrozenRootPaths() {
        SourceTestFixture.InMemoryProvider provider = providerWithRoot(
                validRootWithIgnoredDuplicatesXml());

        SourceGraphResolutionResult result = resolve(provider);

        assertEquals(SourceGraphResolutionStatus.RESOLVED, result.status());
        assertTrue(result.graph().isPresent());
        assertEquals(10, result.graph().get().manifest().sources().size());
        assertEquals(7, result.graph().get().edges().size());
        assertEquals(8, provider.accessCount());
    }

    /**
     * 错误 systems root 中的 rule-file 不得进入 Provider、Edge 或 cycle identity。
     */
    @Test
    void rejectsWrongSystemsRootBeforeAccessingRulesOrBusiness() {
        SourceTestFixture.InMemoryProvider provider = providerWithSystems(
                wrongSystemsRootXml());

        SourceGraphResolutionResult result = resolve(provider);

        assertPolicyFailure(result);
        assertEquals(4, provider.accessCount());
    }

    /**
     * systems 根下非 system 路径中的 rule-file 必须忽略，并以不完整结构失败。
     */
    @Test
    void rejectsRuleFilesOutsideFrozenSystemPath() {
        SourceTestFixture.InMemoryProvider provider = providerWithSystems(
                wrongSystemPathXml());

        SourceGraphResolutionResult result = resolve(provider);

        assertPolicyFailure(result);
        assertEquals(4, provider.accessCount());
    }

    /**
     * 使用固定合法 Provider 替换根 Source 内容。
     */
    private static SourceTestFixture.InMemoryProvider providerWithRoot(
            String content) {
        SourceTestFixture.InMemoryProvider provider = SourceTestFixture.provider(
                SourceTestFixture.FileSetOrder.FORWARD);
        provider.putSingle(SourceTestFixture.source(SourceTestFixture.ROOT, content));
        return provider;
    }

    /**
     * 使用固定合法 Provider 替换 systems Source 内容。
     */
    private static SourceTestFixture.InMemoryProvider providerWithSystems(
            String content) {
        SourceTestFixture.InMemoryProvider provider = SourceTestFixture.provider(
                SourceTestFixture.FileSetOrder.FORWARD);
        provider.putSingle(SourceTestFixture.source(
                SourceTestFixture.SYSTEMS,
                content));
        return provider;
    }

    /**
     * 调用固定 root resolver。
     */
    private static SourceGraphResolutionResult resolve(
            DocumentSourceProvider provider) {
        return new MixSourceResolver().resolve(
                new SourceReference(SourceTestFixture.ROOT),
                provider,
                SourceTestFixture.policy());
    }

    /**
     * 断言结构错误映射为稳定策略失败，且不发布部分图。
     */
    private static void assertPolicyFailure(SourceGraphResolutionResult result) {
        assertEquals(SourceGraphResolutionStatus.FAILED, result.status());
        assertFalse(result.graph().isPresent());
        assertTrue(result.diagnostics().stream()
                .map(Diagnostic::code)
                .anyMatch(code -> code == DiagnosticCode.MIX_SOURCE_POLICY));
    }

    /**
     * 返回错误 root 下包含四类后缀匹配声明的文档。
     */
    private static String wrongRootXml() {
        return "<wrong-root><ignored>"
                + rootDeclarations()
                + "</ignored></wrong-root>";
    }

    /**
     * 返回正确 root 下声明全部位于 ignored 子树的文档。
     */
    private static String wrongRootNestingXml() {
        return "<orm-config><ignored>"
                + rootDeclarations()
                + "</ignored></orm-config>";
    }

    /**
     * 返回合法直接声明以及 ignored 子树中同名重复声明并存的文档。
     */
    private static String validRootWithIgnoredDuplicatesXml() {
        return "<orm-config>"
                + rootDeclarations()
                + "<ignored>" + rootDeclarations() + "</ignored>"
                + "</orm-config>";
    }

    /**
     * 返回固定四类 root 声明片段。
     */
    private static String rootDeclarations() {
        return "<orm-data-file-info><orm-file path=\""
                + SourceTestFixture.DATA_ROOT
                + "\"/></orm-data-file-info>"
                + "<orm-view-file-info><orm-file path=\""
                + SourceTestFixture.VIEW_ROOT
                + "\"/></orm-view-file-info>"
                + "<system-file-info><system-file path=\""
                + SourceTestFixture.SYSTEMS
                + "\"/></system-file-info>"
                + "<business-file-info><business-file path=\""
                + SourceTestFixture.BUSINESS
                + "\"/></business-file-info>";
    }

    /**
     * 返回错误 systems root 下的三个规则声明。
     */
    private static String wrongSystemsRootXml() {
        return "<wrong-systems-root><metadata>"
                + ruleDeclarations()
                + "</metadata></wrong-systems-root>";
    }

    /**
     * 返回正确 systems root 下错误 metadata 路径中的规则声明。
     */
    private static String wrongSystemPathXml() {
        return "<systems><metadata>"
                + ruleDeclarations()
                + "</metadata></systems>";
    }

    /**
     * 返回三个固定 rule-file-info 声明片段。
     */
    private static String ruleDeclarations() {
        return "<rule-file-info><rule-file path=\""
                + SourceTestFixture.USER_RULE
                + "\"/></rule-file-info>"
                + "<rule-file-info><rule-file path=\""
                + SourceTestFixture.ORDER_RULE
                + "\"/></rule-file-info>"
                + "<rule-file-info><rule-file path=\""
                + SourceTestFixture.PAYMENT_RULE
                + "\"/></rule-file-info>";
    }
}
