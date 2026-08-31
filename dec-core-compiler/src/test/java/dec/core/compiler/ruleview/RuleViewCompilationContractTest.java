package dec.core.compiler.ruleview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.raw.RawReference;
import dec.core.compiler.symbol.ReferenceResolutionResult;
import dec.core.compiler.symbol.ReferenceResolutionStatus;
import dec.core.compiler.symbol.ReferenceResolver;
import dec.core.compiler.symbol.ResolvedReference;
import dec.core.compiler.symbol.ResolvedReferenceSet;
import dec.core.compiler.symbol.SymbolBuildResult;
import dec.core.compiler.symbol.SymbolBuildStatus;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.compiler.symbol.SymbolTableBuilder;
import dec.core.context.model.RuleKey;
import dec.core.context.model.RuleViewKey;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * P2 System / RuleView 编译与完整引用合同。
 */
class RuleViewCompilationContractTest {

    @Test
    @DisplayName("CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001")
    void missingSystemOwnerFailsWithoutPartialPublication() {
        SymbolBuildResult result = build(Collections.singletonList(
                rule(0, "rule.xml", "missing", "submit")));
        assertEquals(SymbolBuildStatus.FAILED, result.status());
        assertFalse(result.symbolTable().isPresent());
        assertEquals("symbol.owner.system.missing", result.diagnostics().get(0).messageKey());
    }

    @Test
    @DisplayName("CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001")
    void duplicateRuleViewDiagnosticIsSourceOrderIndependent() {
        SymbolBuildResult first = build(Arrays.asList(
                system(0, "system.xml", "order"),
                rule(1, "a.xml", "order", "shared"),
                rule(2, "b.xml", "order", "shared")));
        SymbolBuildResult second = build(Arrays.asList(
                system(0, "system.xml", "order"),
                rule(1, "b.xml", "order", "shared"),
                rule(2, "a.xml", "order", "shared")));
        assertEquals(SymbolBuildStatus.FAILED, first.status());
        assertEquals(SymbolBuildStatus.FAILED, second.status());
        assertEquals(
                first.diagnostics(),
                second.diagnostics(),
                "duplicate diagnostic must not depend on scan order");
    }

    @Test
    @DisplayName("CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001")
    void sameLocalNameIsIsolatedByOwningSystem() {
        SymbolTable table = table(build(Arrays.asList(
                system(0, "order.xml", "order"),
                system(1, "payment.xml", "payment"),
                rule(2, "order-rule.xml", "order", "shared"),
                rule(3, "payment-rule.xml", "payment", "shared"))));
        assertTrue(table.find(new RuleViewKey(new SystemKey("order"), "shared")).isPresent());
        assertTrue(table.find(new RuleViewKey(new SystemKey("payment"), "shared")).isPresent());
    }

    @Test
    @DisplayName("CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001")
    void ruleViewResolvesOnlyViewDeclaredByItsOwningSystem() {
        RawDefinitionSet legal = new RawDefinitionSet(Arrays.asList(
                view(0, "view.xml", "OrderView"),
                systemWithView(1, "system.xml", "order", "OrderView"),
                ruleWithView(2, "rule.xml", "order", "submit", "OrderView")));
        ReferenceResolutionResult resolved = resolve(legal);
        assertEquals(ReferenceResolutionStatus.RESOLVED, resolved.status());
        List<ResolvedReference> references = resolved.resolvedReferences().get().referencesFrom(
                new RuleViewKey(new SystemKey("order"), "submit"));
        assertEquals(2, references.size());
        assertTrue(references.stream().anyMatch(reference ->
                new SystemKey("order").equals(reference.targetKey())));
        assertTrue(references.stream().anyMatch(reference ->
                new ViewKey("OrderView").equals(reference.targetKey())));

        RawDefinitionSet mismatch = new RawDefinitionSet(Arrays.asList(
                view(0, "view-a.xml", "OrderView"),
                view(1, "view-b.xml", "PaymentView"),
                systemWithView(2, "system.xml", "order", "OrderView"),
                ruleWithView(3, "rule.xml", "order", "submit", "PaymentView")));
        ReferenceResolutionResult failed = resolve(mismatch);
        assertEquals(ReferenceResolutionStatus.FAILED, failed.status());
        assertFalse(failed.resolvedReferences().isPresent());
        assertTrue(failed.diagnostics().stream().anyMatch(diagnostic ->
                "reference.rule-system.mismatch".equals(diagnostic.messageKey())
                        && "rule.xml".equals(diagnostic.sourceRef().sourceId())));
    }

    @Test
    @DisplayName("CASE-P2-TD-RULEKEY-CONTRACT-001")
    void ruleKeyIdentityIsOwningRuleViewPlusCaseSensitiveLocalRule() {
        RuleViewKey order = new RuleViewKey(new SystemKey("order"), "submit");
        RuleViewKey payment = new RuleViewKey(new SystemKey("payment"), "submit");
        assertEquals(RuleKey.of(order, "validate"), RuleKey.of(order, "validate"));
        assertNotEquals(RuleKey.of(order, "validate"), RuleKey.of(order, "Validate"));
        assertNotEquals(RuleKey.of(order, "validate"), RuleKey.of(payment, "validate"));
    }

    @Test
    @DisplayName("CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001")
    void actionRuleReferenceUsesExactSystemQualifiedRuleViewKey() {
        RawDefinitionSet definitions = new RawDefinitionSet(Arrays.asList(
                view(0, "view.xml", "SharedView"),
                systemWithView(1, "order-system.xml", "order", "SharedView"),
                systemWithView(2, "payment-system.xml", "payment", "SharedView"),
                ruleWithView(3, "order-rule.xml", "order", "submit", "SharedView"),
                ruleWithView(4, "payment-rule.xml", "payment", "submit", "SharedView"),
                scope(5, "business.xml", "checkout"),
                directory(6, "business.xml", "checkout", "command"),
                action(7, "action.xml", "command", "save", "order", "submit", true)));

        ReferenceResolutionResult result = resolve(definitions);
        assertEquals(ReferenceResolutionStatus.RESOLVED, result.status());
        ResolvedReference ruleReference = requireReference(
                result.resolvedReferences().get(),
                "@rule-ref");
        assertEquals(new RuleViewKey(new SystemKey("order"), "submit"), ruleReference.targetKey());
        assertEquals("action.xml", ruleReference.sourceRef().sourceId());
        assertFalse(new RuleViewKey(new SystemKey("payment"), "submit")
                .equals(ruleReference.targetKey()));
    }

    @Test
    @DisplayName("DEV02-EXACT-BARE-NAME-NEW-PATH-REJECT-001")
    void newActionPathRejectsBareRuleNameWithoutSystemOwner() {
        RawDefinitionSet definitions = new RawDefinitionSet(Arrays.asList(
                view(0, "view.xml", "SharedView"),
                systemWithView(1, "order-system.xml", "order", "SharedView"),
                systemWithView(2, "payment-system.xml", "payment", "SharedView"),
                ruleWithView(3, "order-rule.xml", "order", "submit", "SharedView"),
                ruleWithView(4, "payment-rule.xml", "payment", "submit", "SharedView"),
                scope(5, "business.xml", "checkout"),
                directory(6, "business.xml", "checkout", "command"),
                action(7, "action.xml", "command", "save", null, "submit", false)));

        ReferenceResolutionResult result = resolve(definitions);
        assertEquals(ReferenceResolutionStatus.FAILED, result.status());
        assertFalse(result.resolvedReferences().isPresent());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "reference.rule-system.mismatch".equals(diagnostic.messageKey())
                        && "action.xml".equals(diagnostic.sourceRef().sourceId())));
    }

    @Test
    @DisplayName("DEV02-EXACT-UNKNOWN-AND-OWNER-MISMATCH-DIAGNOSTICS-001")
    void actionReferenceClassifiesUnknownAndWrongOwnerWithSourceLocation() {
        RawDefinitionSet wrongOwner = new RawDefinitionSet(Arrays.asList(
                view(0, "view.xml", "SharedView"),
                systemWithView(1, "order-system.xml", "order", "SharedView"),
                systemWithView(2, "payment-system.xml", "payment", "SharedView"),
                ruleWithView(3, "payment-rule.xml", "payment", "submit", "SharedView"),
                scope(4, "business.xml", "checkout"),
                directory(5, "business.xml", "checkout", "command"),
                action(6, "wrong-owner-action.xml", "command", "save", "order", "submit", true)));
        ReferenceResolutionResult mismatch = resolve(wrongOwner);
        assertEquals(ReferenceResolutionStatus.FAILED, mismatch.status());
        assertTrue(mismatch.diagnostics().stream().anyMatch(diagnostic ->
                "reference.rule-system.mismatch".equals(diagnostic.messageKey())
                        && "wrong-owner-action.xml".equals(diagnostic.sourceRef().sourceId())));

        RawDefinitionSet unknown = new RawDefinitionSet(Arrays.asList(
                view(0, "view.xml", "SharedView"),
                systemWithView(1, "order-system.xml", "order", "SharedView"),
                scope(2, "business.xml", "checkout"),
                directory(3, "business.xml", "checkout", "command"),
                action(4, "unknown-action.xml", "command", "save", "order", "missing", true)));
        ReferenceResolutionResult missing = resolve(unknown);
        assertEquals(ReferenceResolutionStatus.FAILED, missing.status());
        assertTrue(missing.diagnostics().stream().anyMatch(diagnostic ->
                "reference.unknown".equals(diagnostic.messageKey())
                        && "unknown-action.xml".equals(diagnostic.sourceRef().sourceId())));
    }

    @Test
    @DisplayName("DEV02-CHAR-EXPLICIT-SYSTEM-OWNER-NO-RECENCY-FALLBACK-001")
    void explicitOwnerNeverFallsBackToMostRecentSystem() {
        SymbolTable table = table(build(Arrays.asList(
                system(0, "order.xml", "order"),
                system(1, "payment.xml", "payment"),
                rule(2, "rule.xml", "order", "submit"))));
        assertTrue(table.find(new RuleViewKey(new SystemKey("order"), "submit")).isPresent());
        assertFalse(table.find(new RuleViewKey(new SystemKey("payment"), "submit")).isPresent());
    }

    @Test
    @DisplayName("DEV02-CHAR-RULEVIEWKEY-CASE-SENSITIVE-001")
    void compositeRuleViewIdentityRemainsCaseSensitive() {
        assertNotEquals(
                new RuleViewKey(new SystemKey("order"), "Submit"),
                new RuleViewKey(new SystemKey("order"), "submit"));
    }

    @Test
    @DisplayName("DEV02-CHAR-RULEVIEWKEY-NO-BARE-CONSTRUCTOR-001")
    void noBareStringRuleViewKeyConstructorExists() {
        for (Constructor<?> constructor : RuleViewKey.class.getConstructors()) {
            assertFalse(Arrays.equals(
                    new Class<?>[] {String.class},
                    constructor.getParameterTypes()));
        }
    }

    @Test
    @DisplayName("DEV09-DEFERRED-KEY-SOURCE-COMPAT-001")
    void explicitLexicalOwnerAndNameMapToSharedCompositeKey() {
        SymbolTable table = table(build(Arrays.asList(
                system(0, "system.xml", "order"),
                rule(1, "rule.xml", " order ", " submit "))));
        assertTrue(table.find(new RuleViewKey(new SystemKey("order"), "submit")).isPresent());
    }

    @Test
    @DisplayName("DEV09-DEFERRED-BARE-NAME-COMPATIBILITY-BOUNDARY-001")
    void localNameNeverAuthorizesCrossSystemLookup() {
        SymbolTable table = table(build(Arrays.asList(
                system(0, "system.xml", "order"),
                rule(1, "rule.xml", "order", "submit"))));
        assertTrue(table.find(new RuleViewKey(new SystemKey("order"), "submit")).isPresent());
        assertFalse(table.find(new RuleViewKey(new SystemKey("other"), "submit")).isPresent());
    }

    /** 使用真实 Symbol + ReferenceResolver 解析完整快照。 */
    private static ReferenceResolutionResult resolve(RawDefinitionSet definitions) {
        SymbolBuildResult symbols = new SymbolTableBuilder().build(definitions);
        assertEquals(SymbolBuildStatus.BUILT, symbols.status());
        assertTrue(symbols.symbolTable().isPresent());
        return new ReferenceResolver().resolve(definitions, symbols.symbolTable().get());
    }

    /** 从成功结果中取得指定 role 的唯一引用。 */
    private static ResolvedReference requireReference(
            ResolvedReferenceSet references,
            String role) {
        List<ResolvedReference> matches = new ArrayList<ResolvedReference>();
        for (ResolvedReference reference : references.references()) {
            if (role.equals(reference.role())) {
                matches.add(reference);
            }
        }
        assertEquals(1, matches.size(), "expected exactly one reference for " + role);
        return matches.get(0);
    }

    private static RawDefinition system(long ordinal, String source, String name) {
        return definition(
                RawDefinitionKind.SYSTEM,
                ordinal,
                source,
                null,
                name,
                attributes("name", name),
                Collections.<RawReference>emptyList());
    }

    private static RawDefinition systemWithView(
            long ordinal,
            String source,
            String name,
            String viewName) {
        return definition(
                RawDefinitionKind.SYSTEM,
                ordinal,
                source,
                null,
                name,
                attributes("name", name),
                Collections.singletonList(reference(
                        "/view-info/view-ref@ref",
                        viewName,
                        source,
                        20)));
    }

    private static RawDefinition view(long ordinal, String source, String name) {
        return definition(
                RawDefinitionKind.VIEW,
                ordinal,
                source,
                null,
                name,
                attributes("name", name),
                Collections.<RawReference>emptyList());
    }

    private static RawDefinition rule(
            long ordinal,
            String source,
            String owner,
            String name) {
        return definition(
                RawDefinitionKind.RULE_VIEW,
                ordinal,
                source,
                owner,
                name,
                attributes("system", owner, "name", name),
                Collections.<RawReference>emptyList());
    }

    private static RawDefinition ruleWithView(
            long ordinal,
            String source,
            String owner,
            String name,
            String viewName) {
        return definition(
                RawDefinitionKind.RULE_VIEW,
                ordinal,
                source,
                owner,
                name,
                attributes("system", owner, "name", name, "view-ref", viewName),
                Collections.singletonList(reference(
                        "@view-ref",
                        viewName,
                        source,
                        2)));
    }

    private static RawDefinition scope(long ordinal, String source, String name) {
        return definition(
                RawDefinitionKind.BUSINESS_SCOPE,
                ordinal,
                source,
                null,
                name,
                attributes("name", name),
                Collections.<RawReference>emptyList());
    }

    private static RawDefinition directory(
            long ordinal,
            String source,
            String owner,
            String name) {
        return definition(
                RawDefinitionKind.DIRECTORY,
                ordinal,
                source,
                owner,
                name,
                attributes("name", name),
                Collections.<RawReference>emptyList());
    }

    private static RawDefinition action(
            long ordinal,
            String source,
            String owner,
            String name,
            String systemName,
            String ruleName,
            boolean includeSystem) {
        List<RawReference> references = new ArrayList<RawReference>();
        if (includeSystem) {
            references.add(reference("@system-ref", systemName, source, 30));
        }
        references.add(reference("@rule-ref", ruleName, source, 31));
        return definition(
                RawDefinitionKind.ACTION,
                ordinal,
                source,
                owner,
                name,
                attributes("name", name),
                references);
    }

    private static RawDefinition definition(
            RawDefinitionKind kind,
            long ordinal,
            String source,
            String owner,
            String name,
            Map<String, String> attributes,
            List<RawReference> references) {
        SourceRef ref = new SourceRef(source, 1, 1, "/definition");
        return new RawDefinition(
                kind,
                ordinal,
                ref,
                owner == null ? Optional.<String>empty() : Optional.of(owner),
                name == null ? Optional.<String>empty() : Optional.of(name),
                attributes,
                references,
                new RawNodeBody(
                        kind.name().toLowerCase(),
                        attributes,
                        Optional.<String>empty(),
                        Collections.<RawNodeBody>emptyList(),
                        ref),
                DocumentFormat.XML,
                "1.0");
    }

    private static RawReference reference(
            String role,
            String target,
            String source,
            int line) {
        return new RawReference(
                role,
                target,
                new SourceRef(source, line, 1, "/definition/reference"));
    }

    private static Map<String, String> attributes(String... values) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            result.put(values[index], values[index + 1]);
        }
        return result;
    }

    private static SymbolBuildResult build(List<RawDefinition> definitions) {
        return new SymbolTableBuilder().build(new RawDefinitionSet(definitions));
    }

    private static SymbolTable table(SymbolBuildResult result) {
        assertEquals(SymbolBuildStatus.BUILT, result.status());
        assertTrue(result.symbolTable().isPresent());
        return result.symbolTable().get();
    }
}
