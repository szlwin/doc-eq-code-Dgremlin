package dec.core.compiler.symbol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.context.model.DataKey;
import dec.core.context.model.DataSourceKey;
import dec.core.context.model.DirectoryKey;
import dec.core.context.model.InformationKey;
import dec.core.context.model.RuleViewKey;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T08 强类型引用解析与失败发布边界 Oracle。
 */
class ReferenceResolverContractTest {

    /** 固定 P1 引用必须全部解析为期望 TypedKey。 */
    @Test
    void resolvesAllSupportedP1ReferencesAfterCompleteSymbolRegistration() {
        RawDefinitionSet definitions = ReferenceTestFixture.legalDefinitions();
        ReferenceResolutionResult result = new ReferenceResolver().resolve(
                definitions,
                ReferenceTestFixture.symbols(definitions));

        assertEquals(ReferenceResolutionStatus.RESOLVED, result.status());
        assertTrue(result.diagnostics().isEmpty());
        ResolvedReferenceSet references = result.resolvedReferences().get();
        assertEquals(13, references.size());

        Set<String> targets = new TreeSet<String>();
        for (ResolvedReference reference : references.references()) {
            targets.add(reference.targetKey().canonical());
        }
        assertTrue(targets.contains(new DataSourceKey("data1").canonical()));
        assertTrue(targets.contains(new DataKey("user").canonical()));
        assertTrue(targets.contains(new ViewKey("UserInfo").canonical()));
        assertTrue(targets.contains(new SystemKey("user").canonical()));
        assertTrue(targets.contains(new RuleViewKey(
                new SystemKey("user"), "check").canonical()));
        assertTrue(targets.contains(new InformationKey(
                new SystemKey("user"), "active").canonical()));
        assertTrue(targets.stream().anyMatch(value -> value.contains("directory:")));
    }

    /** RuleView 在 System 前出现时仍必须按显式 owner 精确解析。 */
    @Test
    void resolvesForwardRuleViewWithoutDiscoveryOrderDependency() {
        RawDefinitionSet definitions = ReferenceTestFixture.legalDefinitions();
        ReferenceResolutionResult result = new ReferenceResolver().resolve(
                definitions,
                ReferenceTestFixture.symbols(definitions));
        assertEquals(ReferenceResolutionStatus.RESOLVED, result.status());
        assertTrue(result.resolvedReferences().isPresent());
        ResolvedReferenceSet references = result.resolvedReferences().get();

        List<ResolvedReference> ruleReferences = references.referencesFrom(
                new RuleViewKey(new SystemKey("user"), "check"));
        assertEquals(2, ruleReferences.size());
        assertEquals(new SystemKey("user"), ruleReferences.get(0).targetKey());
        assertEquals(new ViewKey("UserInfo"), ruleReferences.get(1).targetKey());
    }

    /** View property 必须在当前 Data 内区分大小写精确存在。 */
    @Test
    void rejectsUnknownPropertyWithoutSearchingOtherData() {
        RawDefinitionSet definitions = ReferenceTestFixture.invalidDefinitions();
        ReferenceResolutionResult result = new ReferenceResolver().resolve(
                definitions,
                ReferenceTestFixture.symbols(definitions));

        assertEquals(ReferenceResolutionStatus.FAILED, result.status());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "reference.property.unknown".equals(diagnostic.messageKey())));
    }

    /** unknown 与同名错误类型必须产生不同的稳定 messageKey。 */
    @Test
    void distinguishesUnknownFromTypeMismatch() {
        RawDefinitionSet definitions = ReferenceTestFixture.invalidDefinitions();
        ReferenceResolutionResult result = new ReferenceResolver().resolve(
                definitions,
                ReferenceTestFixture.symbols(definitions));

        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "reference.unknown".equals(diagnostic.messageKey())));
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "reference.type.mismatch".equals(diagnostic.messageKey())));
    }

    /** RuleView 与 Action 不能跨 System 使用同名或其他 owner 的目标。 */
    @Test
    void rejectsRuleSystemAndDeclaredViewMismatch() {
        RawDefinitionSet definitions = ReferenceTestFixture.invalidDefinitions();
        ReferenceResolutionResult result = new ReferenceResolver().resolve(
                definitions,
                ReferenceTestFixture.symbols(definitions));

        long mismatches = result.diagnostics().stream()
                .filter(diagnostic -> "reference.rule-system.mismatch"
                        .equals(diagnostic.messageKey()))
                .count();
        assertEquals(2L, mismatches);
    }

    /** Directory 必须使用限定 Information 和同 Scope DirectoryKey。 */
    @Test
    void resolvesQualifiedInformationAndSameScopeDirectory() {
        RawDefinitionSet definitions = ReferenceTestFixture.legalDefinitions();
        ReferenceResolutionResult result = new ReferenceResolver().resolve(
                definitions,
                ReferenceTestFixture.symbols(definitions));
        assertEquals(ReferenceResolutionStatus.RESOLVED, result.status());
        assertTrue(result.resolvedReferences().isPresent());
        ResolvedReferenceSet references = result.resolvedReferences().get();

        assertTrue(references.references().stream().anyMatch(reference ->
                reference.targetKey() instanceof InformationKey));
        assertTrue(references.references().stream().anyMatch(reference ->
                reference.targetKey() instanceof DirectoryKey));
    }

    /** 失败结果不得泄漏任何部分解析集合。 */
    @Test
    void doesNotPublishPartialReferencesOnFailure() {
        RawDefinitionSet definitions = ReferenceTestFixture.invalidDefinitions();
        ReferenceResolutionResult result = new ReferenceResolver().resolve(
                definitions,
                ReferenceTestFixture.symbols(definitions));

        assertEquals(ReferenceResolutionStatus.FAILED, result.status());
        assertFalse(result.resolvedReferences().isPresent());
        assertFalse(result.diagnostics().isEmpty());
    }

    /** 成功集合与来源子列表必须不可变。 */
    @Test
    void publishesImmutableResolvedReferences() {
        RawDefinitionSet definitions = ReferenceTestFixture.legalDefinitions();
        ReferenceResolutionResult result = new ReferenceResolver().resolve(
                definitions,
                ReferenceTestFixture.symbols(definitions));
        assertEquals(ReferenceResolutionStatus.RESOLVED, result.status());
        assertTrue(result.resolvedReferences().isPresent());
        ResolvedReferenceSet references = result.resolvedReferences().get();

        assertThrows(UnsupportedOperationException.class,
                () -> references.references().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> references.referencesFrom(new ViewKey("UserInfo")).clear());
    }
}
