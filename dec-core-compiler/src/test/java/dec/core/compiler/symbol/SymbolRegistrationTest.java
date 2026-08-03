package dec.core.compiler.symbol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.raw.RawReference;
import dec.core.context.model.ActionKey;
import dec.core.context.model.BusinessScopeKey;
import dec.core.context.model.ConnectionKey;
import dec.core.context.model.DataKey;
import dec.core.context.model.DataSourceKey;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DirectoryKey;
import dec.core.context.model.InformationKey;
import dec.core.context.model.ProduceKey;
import dec.core.context.model.RuleViewKey;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T07 两遍 TypedKey 注册、重复拒绝和确定性 Oracle。
 */
class SymbolRegistrationTest {

    /**
     * 两遍注册必须产出 11 类 Context TypedKey，并保留精确 owner 边界。
     */
    @Test
    void registersAllPublishedTypedKeyKindsWithOwners() {
        SymbolTable table = assertBuilt(new SymbolTableBuilder().build(
                new RawDefinitionSet(completeDefinitions())));

        assertEquals(13, table.size());
        assertTrue(table.find(new DataSourceKey("shared")).isPresent());
        assertTrue(table.find(new ConnectionKey("connection")).isPresent());
        assertTrue(table.find(new DataKey("shared")).isPresent());
        assertTrue(table.find(new ViewKey("shared")).isPresent());

        SystemKey order = new SystemKey("order");
        SystemKey payment = new SystemKey("payment");
        assertTrue(table.find(order).isPresent());
        assertTrue(table.find(payment).isPresent());
        assertTrue(table.find(new RuleViewKey(order, "submit-view")).isPresent());
        assertTrue(table.find(new InformationKey(order, "status")).isPresent());
        assertTrue(table.find(new InformationKey(payment, "status")).isPresent());

        BusinessScopeKey scope = new BusinessScopeKey("scope");
        DirectoryKey directory = new DirectoryKey(scope, "directory");
        ActionKey action = new ActionKey(directory, "action");
        assertTrue(table.find(scope).isPresent());
        assertTrue(table.find(directory).isPresent());
        assertTrue(table.find(action).isPresent());
        assertTrue(table.find(new ProduceKey(action, 12)).isPresent());
    }

    /**
     * 相同 lexical name 的不同 TypedKey 必须共存，禁止全局字符串命名空间。
     */
    @Test
    void keepsSameNameAcrossDifferentTypes() {
        RawDefinitionSet definitions = new RawDefinitionSet(Arrays.asList(
                definition(RawDefinitionKind.DATA, 0, null, "same"),
                definition(RawDefinitionKind.VIEW, 1, null, "same"),
                definition(RawDefinitionKind.SYSTEM, 2, null, "same")));

        SymbolTable table = assertBuilt(new SymbolTableBuilder().build(definitions));
        assertEquals(3, table.size());
        assertTrue(table.find(new DataKey("same")).isPresent());
        assertTrue(table.find(new ViewKey("same")).isPresent());
        assertTrue(table.find(new SystemKey("same")).isPresent());
    }

    /**
     * 同 TypedKey 重复必须失败且不得发布部分 SymbolTable。
     */
    @Test
    void rejectsDuplicateTypedKeyWithoutPublishingPartialTable() {
        RawDefinition first = definition(RawDefinitionKind.DATA, 0, null, "duplicate");
        RawDefinition second = definition(RawDefinitionKind.DATA, 1, null, "duplicate");

        SymbolBuildResult result = new SymbolTableBuilder().build(
                new RawDefinitionSet(Arrays.asList(first, second)));

        assertEquals(SymbolBuildStatus.FAILED, result.status());
        assertFalse(result.symbolTable().isPresent());
        assertEquals(1, result.diagnostics().size());
        Diagnostic diagnostic = result.diagnostics().get(0);
        assertEquals(DiagnosticCode.MIX_SYMBOL_DUPLICATE, diagnostic.code());
        assertEquals(new DataKey("duplicate"), diagnostic.definitionKey().get());
    }

    /**
     * duplicate Diagnostic 必须把后出现定义作为主位置，把首定义作为 relatedRef。
     */
    @Test
    void reportsDuplicateAndFirstDefinitionSourceRefs() {
        RawDefinition first = definition(RawDefinitionKind.VIEW, 0, null, "duplicate");
        RawDefinition second = definition(RawDefinitionKind.VIEW, 1, null, "duplicate");

        Diagnostic diagnostic = new SymbolTableBuilder().build(
                new RawDefinitionSet(Arrays.asList(first, second)))
                .diagnostics().get(0);

        assertEquals(second.sourceRef(), diagnostic.sourceRef());
        assertEquals(Collections.singletonList(first.sourceRef()),
                diagnostic.relatedRefs());
        assertEquals("symbol.duplicate", diagnostic.messageKey());
        assertEquals("symbol-registration", diagnostic.pass());
    }

    /**
     * 无名 Produce 必须使用 Raw sourceOrdinal 形成稳定且互不覆盖的身份。
     */
    @Test
    void registersUnnamedProduceBySourceOrdinal() {
        RawDefinitionSet definitions = new RawDefinitionSet(Arrays.asList(
                definition(RawDefinitionKind.BUSINESS_SCOPE, 0, null, "scope"),
                definition(RawDefinitionKind.DIRECTORY, 1, "scope", "directory"),
                definition(RawDefinitionKind.ACTION, 2, "directory", "action"),
                definition(RawDefinitionKind.PRODUCE, 3, "directory/action", null),
                definition(RawDefinitionKind.PRODUCE, 4, "directory/action", null)));

        SymbolTable table = assertBuilt(new SymbolTableBuilder().build(definitions));
        ActionKey action = new ActionKey(
                new DirectoryKey(new BusinessScopeKey("scope"), "directory"),
                "action");
        assertTrue(table.find(new ProduceKey(action, 3)).isPresent());
        assertTrue(table.find(new ProduceKey(action, 4)).isPresent());
    }

    /**
     * owner token 与当前结构上下文不一致时必须 fail closed，不能猜测同名 owner。
     */
    @Test
    void rejectsMismatchedOwnerContext() {
        RawDefinitionSet definitions = new RawDefinitionSet(Arrays.asList(
                definition(RawDefinitionKind.SYSTEM, 0, null, "order"),
                definition(RawDefinitionKind.INFORMATION, 1, "payment", "status")));

        SymbolBuildResult result = new SymbolTableBuilder().build(definitions);
        assertEquals(SymbolBuildStatus.FAILED, result.status());
        assertFalse(result.symbolTable().isPresent());
        assertEquals(DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                result.diagnostics().get(0).code());
        assertEquals("symbol.owner.context.invalid",
                result.diagnostics().get(0).messageKey());
    }

    /**
     * 不同 BusinessScope 下相同 Directory 与 Action 名称必须按 owner 链隔离。
     */
    @Test
    void isolatesBusinessSymbolsByOwnerChain() {
        RawDefinitionSet definitions = new RawDefinitionSet(Arrays.asList(
                definition(RawDefinitionKind.BUSINESS_SCOPE, 0, null, "first"),
                definition(RawDefinitionKind.DIRECTORY, 1, "first", "directory"),
                definition(RawDefinitionKind.ACTION, 2, "directory", "action"),
                definition(RawDefinitionKind.BUSINESS_SCOPE, 3, null, "second"),
                definition(RawDefinitionKind.DIRECTORY, 4, "second", "directory"),
                definition(RawDefinitionKind.ACTION, 5, "directory", "action")));

        SymbolTable table = assertBuilt(new SymbolTableBuilder().build(definitions));
        DirectoryKey firstDirectory = new DirectoryKey(
                new BusinessScopeKey("first"), "directory");
        DirectoryKey secondDirectory = new DirectoryKey(
                new BusinessScopeKey("second"), "directory");
        assertTrue(table.find(firstDirectory).isPresent());
        assertTrue(table.find(secondDirectory).isPresent());
        assertTrue(table.find(new ActionKey(firstDirectory, "action")).isPresent());
        assertTrue(table.find(new ActionKey(secondDirectory, "action")).isPresent());
    }

    /**
     * keys 和 definitions 必须稳定排序、一一对应并保持全部 SourceRef。
     */
    @Test
    void publishesStableOrderedImmutableSnapshot() {
        SymbolTable table = assertBuilt(new SymbolTableBuilder().build(
                new RawDefinitionSet(completeDefinitions())));

        List<DefinitionKey> keys = table.keys();
        for (int index = 1; index < keys.size(); index++) {
            assertTrue(keys.get(index - 1).compareTo(keys.get(index)) < 0);
        }
        assertEquals(keys.size(), table.definitions().size());
        for (int index = 0; index < keys.size(); index++) {
            assertEquals(table.require(keys.get(index)), table.definitions().get(index));
            assertEquals(table.require(keys.get(index)).sourceRef(),
                    table.definitions().get(index).sourceRef());
        }
        assertThrows(UnsupportedOperationException.class,
                () -> table.keys().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> table.definitions().clear());
    }

    /**
     * 同一输入重复构建必须产生完全相同的有序表。
     */
    @Test
    void repeatsDeterministically() {
        RawDefinitionSet definitions = new RawDefinitionSet(completeDefinitions());
        SymbolBuildResult first = new SymbolTableBuilder().build(definitions);
        SymbolBuildResult second = new SymbolTableBuilder().build(definitions);

        assertEquals(first, second);
        assertEquals(first.symbolTable().get().keys(), second.symbolTable().get().keys());
        assertEquals(first.symbolTable().get().definitions(),
                second.symbolTable().get().definitions());
    }

    /**
     * 没有发布 TypedKey 的 Raw Kind 必须保留为 Raw 事实而不制造伪 Key。
     */
    @Test
    void ignoresRawKindsWithoutPublishedTypedKey() {
        RawDefinitionSet definitions = new RawDefinitionSet(Arrays.asList(
                definition(RawDefinitionKind.ROOT_CONFIG, 0, null, "root"),
                definition(RawDefinitionKind.SYSTEM, 1, null, "system"),
                definition(RawDefinitionKind.RULE_VIEW, 2, "system", "view"),
                definition(RawDefinitionKind.RULE, 3, "system/view", "rule"),
                definition(RawDefinitionKind.MODEL_ACCESS, 4, "system", "model")));

        SymbolTable table = assertBuilt(new SymbolTableBuilder().build(definitions));
        assertEquals(2, table.size());
        assertTrue(table.find(new SystemKey("system")).isPresent());
        assertTrue(table.find(new RuleViewKey(
                new SystemKey("system"), "view")).isPresent());
    }

    /**
     * T07 只登记身份，必须原样保留未知 RawReference，不能提前执行 T08 解析。
     */
    @Test
    void leavesRawReferencesUnresolved() {
        SourceRef sourceRef = ref(0);
        RawReference reference = new RawReference("@ref", "unknown-target", sourceRef);
        RawDefinition data = definition(
                RawDefinitionKind.DATA,
                0,
                null,
                "data",
                Collections.singletonList(reference));

        SymbolTable table = assertBuilt(new SymbolTableBuilder().build(
                new RawDefinitionSet(Collections.singletonList(data))));
        RawDefinition registered = table.require(new DataKey("data"));
        assertEquals(Collections.singletonList(reference), registered.references());
        assertEquals("unknown-target", registered.references().get(0).target());
    }

    private static SymbolTable assertBuilt(SymbolBuildResult result) {
        assertEquals(SymbolBuildStatus.BUILT, result.status());
        assertTrue(result.symbolTable().isPresent());
        assertTrue(result.diagnostics().isEmpty());
        return result.symbolTable().get();
    }

    private static List<RawDefinition> completeDefinitions() {
        List<RawDefinition> values = new ArrayList<RawDefinition>();
        values.add(definition(RawDefinitionKind.ROOT_CONFIG, 0, null, "root"));
        values.add(definition(RawDefinitionKind.DATA_SOURCE, 1, "root", "shared"));
        values.add(definition(RawDefinitionKind.CONNECTION, 2, "root", "connection"));
        values.add(definition(RawDefinitionKind.DATA, 3, null, "shared"));
        values.add(definition(RawDefinitionKind.VIEW, 4, null, "shared"));
        values.add(definition(RawDefinitionKind.SYSTEM, 5, null, "order"));
        values.add(definition(RawDefinitionKind.RULE_VIEW, 6, "order", "submit-view"));
        values.add(definition(RawDefinitionKind.INFORMATION, 7, "order", "status"));
        values.add(definition(RawDefinitionKind.RULE, 8, "order/submit-view", "rule"));
        values.add(definition(RawDefinitionKind.BUSINESS_SCOPE, 9, null, "scope"));
        values.add(definition(RawDefinitionKind.DIRECTORY, 10, "scope", "directory"));
        values.add(definition(RawDefinitionKind.ACTION, 11, "directory", "action"));
        values.add(definition(RawDefinitionKind.PRODUCE, 12, "directory/action", null));
        values.add(definition(RawDefinitionKind.MODEL_ACCESS, 13, "order", "model"));
        values.add(definition(RawDefinitionKind.SYSTEM, 14, null, "payment"));
        values.add(definition(RawDefinitionKind.INFORMATION, 15, "payment", "status"));
        return values;
    }

    private static RawDefinition definition(
            RawDefinitionKind kind,
            long ordinal,
            String owner,
            String name) {
        return definition(kind, ordinal, owner, name,
                Collections.<RawReference>emptyList());
    }

    private static RawDefinition definition(
            RawDefinitionKind kind,
            long ordinal,
            String owner,
            String name,
            List<RawReference> references) {
        SourceRef sourceRef = ref(ordinal);
        return new RawDefinition(
                kind,
                ordinal,
                sourceRef,
                owner == null ? Optional.<String>empty() : Optional.of(owner),
                name == null ? Optional.<String>empty() : Optional.of(name),
                Collections.<String, String>emptyMap(),
                references,
                new RawNodeBody(
                        kind.name().toLowerCase(),
                        Collections.<String, String>emptyMap(),
                        Optional.<String>empty(),
                        Collections.<RawNodeBody>emptyList(),
                        sourceRef),
                DocumentFormat.XML,
                "1.0");
    }

    private static SourceRef ref(long ordinal) {
        return new SourceRef(
                "symbol-" + ordinal + ".xml",
                (int) ordinal + 1,
                1,
                "/definition[" + ordinal + "]");
    }
}
