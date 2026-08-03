package dec.core.compiler.symbol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.raw.RawReference;
import dec.core.context.model.ActionKey;
import dec.core.context.model.BusinessScopeKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DirectoryKey;
import dec.core.context.model.InformationKey;
import dec.core.context.model.ProduceKey;
import dec.core.context.model.RuleViewKey;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T07 I002：Raw lexical owner 与 RuleView 显式 owner 的独立 Review Oracle。
 */
class SymbolOwnerIdentityReworkTest {

    /**
     * System/Information 的结构 owner 必须比较原始 lexical，同时 TypedKey 独立规范化。
     */
    @Test
    void preservesPaddedSystemLexicalWhileCanonicalizingTypedKeys() {
        RawDefinition system = definition(
                RawDefinitionKind.SYSTEM, 0, null, "  order  ");
        RawDefinition information = definition(
                RawDefinitionKind.INFORMATION,
                1,
                "  order  ",
                "  status  ");

        SymbolTable table = assertBuilt(new SymbolTableBuilder().build(
                new RawDefinitionSet(Arrays.asList(system, information))));

        SystemKey systemKey = new SystemKey("order");
        InformationKey informationKey = new InformationKey(
                systemKey,
                "status");
        assertTrue(table.find(systemKey).isPresent());
        RawDefinition registered = table.require(informationKey);
        assertEquals("  status  ", registered.name().get());
        assertEquals("  order  ", registered.ownerToken().get());
        assertEquals("order", systemKey.name());
        assertEquals("status", informationKey.name());
    }

    /**
     * Business owner 链和 Produce composite owner 必须全部使用原始 lexical 事实校验。
     */
    @Test
    void preservesPaddedBusinessOwnerChainAndProduceIdentity() {
        RawDefinitionSet definitions = new RawDefinitionSet(Arrays.asList(
                definition(RawDefinitionKind.BUSINESS_SCOPE,
                        0, null, "  scope  "),
                definition(RawDefinitionKind.DIRECTORY,
                        1, "  scope  ", "  directory  "),
                definition(RawDefinitionKind.ACTION,
                        2, "  directory  ", "  action  "),
                definition(RawDefinitionKind.PRODUCE,
                        3, "  directory  /  action  ", null)));

        SymbolTable table = assertBuilt(new SymbolTableBuilder().build(definitions));
        BusinessScopeKey scope = new BusinessScopeKey("scope");
        DirectoryKey directory = new DirectoryKey(scope, "directory");
        ActionKey action = new ActionKey(directory, "action");
        ProduceKey produce = new ProduceKey(action, 3);

        assertTrue(table.find(scope).isPresent());
        assertTrue(table.find(directory).isPresent());
        assertTrue(table.find(action).isPresent());
        RawDefinition registered = table.require(produce);
        assertEquals("  directory  /  action  ",
                registered.ownerToken().get());
        assertEquals("directory", directory.name());
        assertEquals("action", action.actionName());
    }

    /**
     * RuleView 位于 System 之前时，必须依据自身 ownerToken 在完整 System 集合中登记。
     */
    @Test
    void registersRuleViewBeforeItsSystem() {
        RawDefinitionSet definitions = new RawDefinitionSet(Arrays.asList(
                definition(RawDefinitionKind.RULE_VIEW,
                        0, "  order  ", "  submit  "),
                definition(RawDefinitionKind.SYSTEM,
                        1, null, "order")));

        SymbolTable table = assertBuilt(new SymbolTableBuilder().build(definitions));
        RuleViewKey key = new RuleViewKey(
                new SystemKey("order"),
                "submit");
        RawDefinition registered = table.require(key);
        assertEquals("  order  ", registered.ownerToken().get());
        assertEquals("  submit  ", registered.name().get());
    }

    /**
     * RuleView 必须能指向多个 System 中非最近扫描到的目标。
     */
    @Test
    void bindsRuleViewToExplicitNonRecentSystem() {
        RawDefinitionSet definitions = new RawDefinitionSet(Arrays.asList(
                definition(RawDefinitionKind.SYSTEM,
                        0, null, "order"),
                definition(RawDefinitionKind.SYSTEM,
                        1, null, "payment"),
                definition(RawDefinitionKind.RULE_VIEW,
                        2, "order", "submit")));

        SymbolTable table = assertBuilt(new SymbolTableBuilder().build(definitions));
        assertTrue(table.find(new RuleViewKey(
                new SystemKey("order"), "submit")).isPresent());
        assertFalse(table.find(new RuleViewKey(
                new SystemKey("payment"), "submit")).isPresent());
    }

    /**
     * 多个 RuleView 可分别绑定不同 System，且相同名称按 owner 类型身份隔离。
     */
    @Test
    void isolatesSameRuleViewNameAcrossExplicitSystems() {
        RawDefinitionSet definitions = new RawDefinitionSet(Arrays.asList(
                definition(RawDefinitionKind.RULE_VIEW,
                        0, "order", "shared"),
                definition(RawDefinitionKind.RULE_VIEW,
                        1, "payment", "shared"),
                definition(RawDefinitionKind.SYSTEM,
                        2, null, "payment"),
                definition(RawDefinitionKind.SYSTEM,
                        3, null, "order")));

        SymbolTable table = assertBuilt(new SymbolTableBuilder().build(definitions));
        assertTrue(table.find(new RuleViewKey(
                new SystemKey("order"), "shared")).isPresent());
        assertTrue(table.find(new RuleViewKey(
                new SystemKey("payment"), "shared")).isPresent());
    }

    /**
     * RuleView 显式 owner 不存在时必须稳定失败，且不得发布部分 SymbolTable。
     */
    @Test
    void rejectsRuleViewWhoseExplicitSystemIsMissing() {
        SymbolBuildResult result = new SymbolTableBuilder().build(
                new RawDefinitionSet(Collections.singletonList(
                        definition(RawDefinitionKind.RULE_VIEW,
                                0, "missing", "submit"))));

        assertEquals(SymbolBuildStatus.FAILED, result.status());
        assertFalse(result.symbolTable().isPresent());
        assertEquals(1, result.diagnostics().size());
        Diagnostic diagnostic = result.diagnostics().get(0);
        assertEquals("symbol.owner.system.missing", diagnostic.messageKey());
        assertTrue(diagnostic.definitionKey().isPresent());
        assertEquals(new SystemKey("missing"),
                diagnostic.definitionKey().get());
    }

    /**
     * RuleView 与 System 的文档排列变化不得改变最终 Symbol 身份集合。
     */
    @Test
    void keepsSymbolIdentitiesStableAcrossRuleDocumentOrder() {
        SymbolTable ruleFirst = assertBuilt(new SymbolTableBuilder().build(
                new RawDefinitionSet(Arrays.asList(
                        definition(RawDefinitionKind.RULE_VIEW,
                                0, "order", "submit"),
                        definition(RawDefinitionKind.SYSTEM,
                                1, null, "order")))));
        SymbolTable systemFirst = assertBuilt(new SymbolTableBuilder().build(
                new RawDefinitionSet(Arrays.asList(
                        definition(RawDefinitionKind.SYSTEM,
                                0, null, "order"),
                        definition(RawDefinitionKind.RULE_VIEW,
                                1, "order", "submit")))));

        assertEquals(ruleFirst.keys(), systemFirst.keys());
    }

    /**
     * 断言构建成功并返回完整 SymbolTable。
     */
    private static SymbolTable assertBuilt(SymbolBuildResult result) {
        assertEquals(SymbolBuildStatus.BUILT, result.status());
        assertTrue(result.symbolTable().isPresent());
        assertTrue(result.diagnostics().isEmpty());
        return result.symbolTable().get();
    }

    /**
     * 创建无引用 RawDefinition，保留传入 lexical owner/name。
     */
    private static RawDefinition definition(
            RawDefinitionKind kind,
            long ordinal,
            String owner,
            String name) {
        SourceRef sourceRef = ref(ordinal);
        return new RawDefinition(
                kind,
                ordinal,
                sourceRef,
                owner == null
                        ? Optional.<String>empty()
                        : Optional.of(owner),
                name == null
                        ? Optional.<String>empty()
                        : Optional.of(name),
                Collections.<String, String>emptyMap(),
                Collections.<RawReference>emptyList(),
                new RawNodeBody(
                        kind.name().toLowerCase(),
                        Collections.<String, String>emptyMap(),
                        Optional.<String>empty(),
                        Collections.<RawNodeBody>emptyList(),
                        sourceRef),
                DocumentFormat.XML,
                "1.0");
    }

    /**
     * 为每个 ordinal 创建稳定、可区分的 SourceRef。
     */
    private static SourceRef ref(long ordinal) {
        return new SourceRef(
                "owner-rework-" + ordinal + ".xml",
                (int) ordinal + 1,
                1,
                "/definition[" + ordinal + "]");
    }
}
