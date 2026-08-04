package dec.core.compiler.information;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.raw.RawReference;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * T09 I002 对 canonical common、完整输入快照和 parser 深度边界的返工 Oracle。
 */
class InformationInputIdentityReworkTest {

    /** padded common 仍应获得 canonical common 的跨 System 引用权限。 */
    @Test
    void allowsPaddedCanonicalCommonCrossSystemReference() {
        RawDefinitionSet definitions = paddedCommonDefinitions();
        InformationCompilationResult result = new InformationCompiler().compile(
                definitions,
                InformationTestFixture.symbols(definitions));

        assertEquals(InformationCompilationStatus.COMPILED, result.status());
        assertTrue(result.compilation().isPresent());
        assertEquals(2, result.compilation().get().size());
        assertEquals(" common ", definitions.definitions().get(6).name().get());
        assertEquals(" common ",
                definitions.definitions().get(7).ownerToken().get());
    }

    /** padded common Information 的禁止成员必须按 canonical 身份拒绝。 */
    @Test
    void rejectsForbiddenMemberForPaddedCanonicalCommon() {
        assertCommonMemberFailure(paddedCommonInformationWithMember());
    }

    /** padded common System 的非空 data section 必须被拒绝。 */
    @Test
    void rejectsSystemSectionForPaddedCanonicalCommon() {
        assertCommonMemberFailure(paddedCommonSystemWithDataMember());
    }

    /** padded common ModelAccess 必须被 canonical common 规则拒绝。 */
    @Test
    void rejectsModelAccessForPaddedCanonicalCommon() {
        assertCommonMemberFailure(paddedCommonWithModelAccess());
    }

    /** padded common Information 缺失 expression 不得按普通 Information 静默跳过。 */
    @Test
    void rejectsMissingExpressionForPaddedCanonicalCommon() {
        assertCommonMemberFailure(paddedCommonWithoutExpression());
    }

    /** 删除旧 Information 后不得从上一 revision SymbolTable 发布陈旧依赖。 */
    @Test
    void rejectsSnapshotAfterInformationDeletion() {
        RawDefinitionSet previous = InformationTestFixture.ordinaryDefinitions(
                "order.paySuccessStatus");
        assertSnapshotMismatch(removeAndReordinal(previous, 1),
                InformationTestFixture.symbols(previous));
    }

    /** 同 ordinal 将旧 Information 替换为 ModelAccess 时必须入口失败。 */
    @Test
    void rejectsSnapshotAfterInformationReplacement() {
        RawDefinitionSet previous = InformationTestFixture.ordinaryDefinitions(
                "order.paySuccessStatus");
        List<RawDefinition> changed = new ArrayList<RawDefinition>(
                previous.definitions());
        RawDefinition target = changed.get(1);
        changed.set(1, new RawDefinition(
                RawDefinitionKind.MODEL_ACCESS,
                target.sourceOrdinal(),
                target.sourceRef(),
                target.ownerToken(),
                target.name(),
                Collections.<String, String>emptyMap(),
                Collections.<RawReference>emptyList(),
                new RawNodeBody(
                        "model-access",
                        Collections.<String, String>emptyMap(),
                        Optional.<String>empty(),
                        Collections.<RawNodeBody>emptyList(),
                        target.sourceRef()),
                target.format(),
                target.schemaVersion()));
        assertSnapshotMismatch(new RawDefinitionSet(changed),
                InformationTestFixture.symbols(previous));
    }

    /** 当前批次新增 Information 时旧 SymbolTable 不得继续使用。 */
    @Test
    void rejectsSnapshotAfterInformationAddition() {
        RawDefinitionSet previous = InformationTestFixture.ordinaryDefinitions(
                "order.paySuccessStatus");
        List<RawDefinition> changed = new ArrayList<RawDefinition>(
                previous.definitions());
        RawDefinition template = changed.get(1);
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        attributes.put("name", "newStatus");
        changed.add(new RawDefinition(
                RawDefinitionKind.INFORMATION,
                changed.size(),
                new SourceRef("systems.xml", 88, 1,
                        "/systems/system/information-info/information[newStatus]"),
                Optional.of("order"),
                Optional.of("newStatus"),
                attributes,
                Collections.<RawReference>emptyList(),
                new RawNodeBody(
                        "information",
                        attributes,
                        Optional.<String>empty(),
                        Collections.<RawNodeBody>emptyList(),
                        new SourceRef("systems.xml", 88, 1,
                                "/systems/system/information-info/information[newStatus]")),
                template.format(),
                template.schemaVersion()));
        assertSnapshotMismatch(new RawDefinitionSet(changed),
                InformationTestFixture.symbols(previous));
    }

    /** 相同 TypedKey 的 body、expression 或 SourceRef 变化也必须判定快照不一致。 */
    @Test
    void rejectsSnapshotAfterSameKeyBodyChange() {
        RawDefinitionSet previous = InformationTestFixture.ordinaryDefinitions(
                "order.paySuccessStatus");
        List<RawDefinition> changed = new ArrayList<RawDefinition>(
                previous.definitions());
        RawDefinition target = changed.get(1);
        SourceRef changedRef = new SourceRef(
                "systems-v2.xml",
                101,
                1,
                "/systems/system/information-info/information[changed]");
        Map<String, String> attributes = new LinkedHashMap<String, String>(
                target.attributes());
        attributes.put("expression", "order.paySuccessStatus");
        changed.set(1, new RawDefinition(
                target.kind(),
                target.sourceOrdinal(),
                changedRef,
                target.ownerToken(),
                target.name(),
                attributes,
                target.references(),
                new RawNodeBody(
                        target.body().name(),
                        attributes,
                        Optional.<String>empty(),
                        target.body().children(),
                        changedRef),
                target.format(),
                target.schemaVersion()));
        assertSnapshotMismatch(new RawDefinitionSet(changed),
                InformationTestFixture.symbols(previous));
    }

    /** 定义顺序或 ordinal 变化必须阻断上一 revision SymbolTable。 */
    @Test
    void rejectsSnapshotAfterOrdinalChange() {
        RawDefinitionSet previous = InformationTestFixture.ordinaryDefinitions(
                "order.paySuccessStatus");
        List<RawDefinition> changed = new ArrayList<RawDefinition>(
                previous.definitions());
        RawDefinition first = changed.get(0);
        RawDefinition second = changed.get(1);
        changed.set(0, copyWithOrdinal(second, 0));
        changed.set(1, copyWithOrdinal(first, 1));
        assertSnapshotMismatch(new RawDefinitionSet(changed),
                InformationTestFixture.symbols(previous));
    }

    /** 128 层括号属于冻结预算，必须成功。 */
    @Test
    void acceptsExactly128ParenthesisLevels() {
        String expression = nestedExpression(128);
        RawDefinitionSet definitions =
                InformationTestFixture.ordinaryDefinitions(expression);
        InformationCompilationResult result = new InformationCompiler().compile(
                definitions,
                InformationTestFixture.symbols(definitions));

        assertEquals(InformationCompilationStatus.COMPILED, result.status());
        assertTrue(result.compilation().isPresent());
    }

    /** 129 层括号必须稳定命中 limit Diagnostic。 */
    @Test
    void rejects129ParenthesisLevels() {
        String expression = nestedExpression(129);
        RawDefinitionSet definitions =
                InformationTestFixture.ordinaryDefinitions(expression);
        InformationCompilationResult result = new InformationCompiler().compile(
                definitions,
                InformationTestFixture.symbols(definitions));

        assertEquals(InformationCompilationStatus.FAILED, result.status());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "information.expression.limit.exceeded".equals(
                        diagnostic.messageKey())));
    }

    /** 快照失败必须独占入口且 parser/resolver 均不得执行。 */
    private static void assertSnapshotMismatch(
            RawDefinitionSet current,
            SymbolTable previousSymbols) {
        AtomicInteger parserCalls = new AtomicInteger();
        AtomicInteger resolverCalls = new AtomicInteger();
        InformationExpressionParser parser = (expression, sourceRef) -> {
            parserCalls.incrementAndGet();
            return new DefaultInformationExpressionParser().parse(
                    expression,
                    sourceRef);
        };
        InformationReferenceResolver resolver = (owner, ast, symbols, sourceRef) -> {
            resolverCalls.incrementAndGet();
            return new DefaultInformationReferenceResolver().resolve(
                    owner,
                    ast,
                    symbols,
                    sourceRef);
        };

        InformationCompilationResult result =
                new InformationCompiler(parser, resolver).compile(
                        current,
                        previousSymbols);
        assertEquals(InformationCompilationStatus.FAILED, result.status());
        assertFalse(result.compilation().isPresent());
        assertEquals(1, result.diagnostics().size(), result.diagnostics().toString());
        Diagnostic diagnostic = result.diagnostics().get(0);
        assertEquals("information.input.snapshot-mismatch",
                diagnostic.messageKey());
        assertEquals(0, parserCalls.get());
        assertEquals(0, resolverCalls.get());
    }

    /** 断言 padded common 的结构限制使用 canonical 身份。 */
    private static void assertCommonMemberFailure(RawDefinitionSet definitions) {
        InformationCompilationResult result = new InformationCompiler().compile(
                definitions,
                InformationTestFixture.symbols(definitions));
        assertEquals(InformationCompilationStatus.FAILED, result.status());
        assertFalse(result.compilation().isPresent());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "information.common.member.invalid".equals(
                        diagnostic.messageKey())), result.diagnostics().toString());
    }

    /** 构造 raw lexical 带空格但 canonical owner 为 common 的完整输入。 */
    private static RawDefinitionSet paddedCommonDefinitions() {
        RawDefinitionSet base = InformationTestFixture.commonDefinitions(
                "payment.success and order.paySuccessStatus",
                "payment.error and order.payErrorStatus");
        List<RawDefinition> changed = new ArrayList<RawDefinition>(
                base.definitions());
        RawDefinition commonSystem = changed.get(6);
        Map<String, String> systemAttributes = new LinkedHashMap<String, String>(
                commonSystem.attributes());
        systemAttributes.put("name", " common ");
        changed.set(6, new RawDefinition(
                commonSystem.kind(),
                commonSystem.sourceOrdinal(),
                commonSystem.sourceRef(),
                commonSystem.ownerToken(),
                Optional.of(" common "),
                systemAttributes,
                commonSystem.references(),
                new RawNodeBody(
                        commonSystem.body().name(),
                        systemAttributes,
                        commonSystem.body().scalar(),
                        commonSystem.body().children(),
                        commonSystem.body().sourceRef()),
                commonSystem.format(),
                commonSystem.schemaVersion()));
        changed.set(7, copyWithOwner(changed.get(7), " common "));
        changed.set(8, copyWithOwner(changed.get(8), " common "));
        return new RawDefinitionSet(changed);
    }

    /** 构造 padded common Information 含禁止成员。 */
    private static RawDefinitionSet paddedCommonInformationWithMember() {
        RawDefinitionSet base = paddedCommonDefinitions();
        List<RawDefinition> changed = new ArrayList<RawDefinition>(
                base.definitions());
        RawDefinition information = changed.get(7);
        Map<String, String> attributes = new LinkedHashMap<String, String>(
                information.attributes());
        attributes.put("view-ref", "PaymentView");
        RawNodeBody forbidden = new RawNodeBody(
                "change-data",
                Collections.singletonMap("name", "forbidden"),
                Optional.<String>empty(),
                Collections.<RawNodeBody>emptyList(),
                information.sourceRef());
        changed.set(7, new RawDefinition(
                information.kind(),
                information.sourceOrdinal(),
                information.sourceRef(),
                information.ownerToken(),
                information.name(),
                attributes,
                information.references(),
                new RawNodeBody(
                        information.body().name(),
                        attributes,
                        Optional.<String>empty(),
                        Collections.singletonList(forbidden),
                        information.body().sourceRef()),
                information.format(),
                information.schemaVersion()));
        return new RawDefinitionSet(changed);
    }

    /** 构造 padded common System 含非空 data-info。 */
    private static RawDefinitionSet paddedCommonSystemWithDataMember() {
        RawDefinitionSet base = paddedCommonDefinitions();
        List<RawDefinition> changed = new ArrayList<RawDefinition>(
                base.definitions());
        RawDefinition system = changed.get(6);
        RawNodeBody dataRef = new RawNodeBody(
                "data-ref",
                Collections.singletonMap("ref", "payment"),
                Optional.<String>empty(),
                Collections.<RawNodeBody>emptyList(),
                system.sourceRef());
        RawNodeBody dataInfo = new RawNodeBody(
                "data-info",
                Collections.<String, String>emptyMap(),
                Optional.<String>empty(),
                Collections.singletonList(dataRef),
                system.sourceRef());
        List<RawNodeBody> children = new ArrayList<RawNodeBody>(
                system.body().children());
        children.set(0, dataInfo);
        changed.set(6, new RawDefinition(
                system.kind(),
                system.sourceOrdinal(),
                system.sourceRef(),
                system.ownerToken(),
                system.name(),
                system.attributes(),
                system.references(),
                new RawNodeBody(
                        system.body().name(),
                        system.body().attributes(),
                        system.body().scalar(),
                        children,
                        system.body().sourceRef()),
                system.format(),
                system.schemaVersion()));
        return new RawDefinitionSet(changed);
    }

    /** 构造 padded common 下被禁止的 ModelAccess。 */
    private static RawDefinitionSet paddedCommonWithModelAccess() {
        RawDefinitionSet base = paddedCommonDefinitions();
        List<RawDefinition> changed = new ArrayList<RawDefinition>(
                base.definitions());
        SourceRef sourceRef = new SourceRef(
                "systems.xml",
                90,
                1,
                "/systems/system/model-access[1]");
        changed.add(new RawDefinition(
                RawDefinitionKind.MODEL_ACCESS,
                changed.size(),
                sourceRef,
                Optional.of(" common "),
                Optional.of("model"),
                Collections.<String, String>emptyMap(),
                Collections.<RawReference>emptyList(),
                new RawNodeBody(
                        "model-access",
                        Collections.<String, String>emptyMap(),
                        Optional.<String>empty(),
                        Collections.<RawNodeBody>emptyList(),
                        sourceRef),
                DocumentFormat.XML,
                "1.0"));
        return new RawDefinitionSet(changed);
    }

    /** 构造 padded common Information 缺失 expression。 */
    private static RawDefinitionSet paddedCommonWithoutExpression() {
        RawDefinitionSet base = paddedCommonDefinitions();
        List<RawDefinition> changed = new ArrayList<RawDefinition>(
                base.definitions());
        RawDefinition information = changed.get(7);
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        attributes.put("name", information.name().get());
        changed.set(7, new RawDefinition(
                information.kind(),
                information.sourceOrdinal(),
                information.sourceRef(),
                information.ownerToken(),
                information.name(),
                attributes,
                information.references(),
                new RawNodeBody(
                        information.body().name(),
                        attributes,
                        Optional.<String>empty(),
                        Collections.<RawNodeBody>emptyList(),
                        information.body().sourceRef()),
                information.format(),
                information.schemaVersion()));
        return new RawDefinitionSet(changed);
    }

    /** 删除定义后重新建立连续 ordinal，用于模拟当前 revision 删除事实。 */
    private static RawDefinitionSet removeAndReordinal(
            RawDefinitionSet definitions,
            int removedIndex) {
        List<RawDefinition> changed = new ArrayList<RawDefinition>();
        for (RawDefinition definition : definitions.definitions()) {
            if (definition.sourceOrdinal() != removedIndex) {
                changed.add(copyWithOrdinal(definition, changed.size()));
            }
        }
        return new RawDefinitionSet(changed);
    }

    /** 复制 RawDefinition 并替换 owner lexical。 */
    private static RawDefinition copyWithOwner(
            RawDefinition definition,
            String owner) {
        return new RawDefinition(
                definition.kind(),
                definition.sourceOrdinal(),
                definition.sourceRef(),
                Optional.of(owner),
                definition.name(),
                definition.attributes(),
                definition.references(),
                definition.body(),
                definition.format(),
                definition.schemaVersion());
    }

    /** 复制 RawDefinition 并替换 sourceOrdinal。 */
    private static RawDefinition copyWithOrdinal(
            RawDefinition definition,
            long ordinal) {
        return new RawDefinition(
                definition.kind(),
                ordinal,
                definition.sourceRef(),
                definition.ownerToken(),
                definition.name(),
                definition.attributes(),
                definition.references(),
                definition.body(),
                definition.format(),
                definition.schemaVersion());
    }

    /** 构造指定括号层数的合法表达式。 */
    private static String nestedExpression(int levels) {
        StringBuilder value = new StringBuilder();
        for (int index = 0; index < levels; index++) {
            value.append('(');
        }
        value.append("order.paySuccessStatus");
        for (int index = 0; index < levels; index++) {
            value.append(')');
        }
        return value.toString();
    }
}
