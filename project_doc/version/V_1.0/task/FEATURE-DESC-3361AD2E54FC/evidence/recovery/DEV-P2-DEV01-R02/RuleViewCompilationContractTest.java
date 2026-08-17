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
import dec.core.compiler.symbol.SymbolBuildResult;
import dec.core.compiler.symbol.SymbolBuildStatus;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.compiler.symbol.SymbolTableBuilder;
import dec.core.context.model.RuleViewKey;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RuleViewCompilationContractTest {
    @Test @DisplayName("CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001")
    void missingSystemOwnerFailsWithoutPartialPublication() {
        SymbolBuildResult result = build(Collections.singletonList(rule(0, "rule.xml", "missing", "submit")));
        assertEquals(SymbolBuildStatus.FAILED, result.status());
        assertFalse(result.symbolTable().isPresent());
        assertEquals("symbol.owner.system.missing", result.diagnostics().get(0).messageKey());
    }

    @Test @DisplayName("CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001")
    void duplicateRuleViewDiagnosticIsSourceOrderIndependent() {
        SymbolBuildResult first = build(Arrays.asList(system(0, "system.xml", "order"), rule(1, "a.xml", "order", "shared"), rule(2, "b.xml", "order", "shared")));
        SymbolBuildResult second = build(Arrays.asList(system(0, "system.xml", "order"), rule(1, "b.xml", "order", "shared"), rule(2, "a.xml", "order", "shared")));
        assertEquals(SymbolBuildStatus.FAILED, first.status());
        assertEquals(SymbolBuildStatus.FAILED, second.status());
        assertEquals(first.diagnostics(), second.diagnostics(), "P2 RED [CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001]: duplicate diagnostic must not depend on scan order");
    }

    @Test @DisplayName("CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001")
    void sameLocalNameIsIsolatedByOwningSystem() {
        SymbolTable table = table(build(Arrays.asList(system(0, "order.xml", "order"), system(1, "payment.xml", "payment"), rule(2, "order-rule.xml", "order", "shared"), rule(3, "payment-rule.xml", "payment", "shared"))));
        assertTrue(table.find(new RuleViewKey(new SystemKey("order"), "shared")).isPresent());
        assertTrue(table.find(new RuleViewKey(new SystemKey("payment"), "shared")).isPresent());
    }

    @Test @DisplayName("DEV01-CHAR-EXPLICIT-SYSTEM-OWNER-NO-RECENCY-FALLBACK-001")
    void explicitOwnerNeverFallsBackToMostRecentSystem() {
        SymbolTable table = table(build(Arrays.asList(system(0, "order.xml", "order"), system(1, "payment.xml", "payment"), rule(2, "rule.xml", "order", "submit"))));
        assertTrue(table.find(new RuleViewKey(new SystemKey("order"), "submit")).isPresent());
        assertFalse(table.find(new RuleViewKey(new SystemKey("payment"), "submit")).isPresent());
    }

    @Test @DisplayName("DEV01-CHAR-RULEVIEWKEY-CASE-SENSITIVE-001")
    void compositeRuleViewIdentityRemainsCaseSensitive() {
        assertNotEquals(new RuleViewKey(new SystemKey("order"), "Submit"), new RuleViewKey(new SystemKey("order"), "submit"));
    }

    @Test @DisplayName("DEV01-CHAR-RULEVIEWKEY-NO-BARE-CONSTRUCTOR-001")
    void noBareStringRuleViewKeyConstructorExists() {
        for (Constructor<?> constructor : RuleViewKey.class.getConstructors()) {
            assertFalse(Arrays.equals(new Class<?>[] {String.class}, constructor.getParameterTypes()));
        }
    }

    @Test @DisplayName("DEV01-CHAR-LEXICAL-OWNER-NORMALIZATION-001")
    void explicitLexicalOwnerAndNameMapToSharedCompositeKey() {
        SymbolTable table = table(build(Arrays.asList(system(0, "system.xml", "order"), rule(1, "rule.xml", " order ", " submit "))));
        assertTrue(table.find(new RuleViewKey(new SystemKey("order"), "submit")).isPresent());
    }

    @Test @DisplayName("DEV01-CHAR-CROSS-SYSTEM-NEGATIVE-LOOKUP-001")
    void localNameNeverAuthorizesCrossSystemLookup() {
        SymbolTable table = table(build(Arrays.asList(system(0, "system.xml", "order"), rule(1, "rule.xml", "order", "submit"))));
        assertTrue(table.find(new RuleViewKey(new SystemKey("order"), "submit")).isPresent());
        assertFalse(table.find(new RuleViewKey(new SystemKey("other"), "submit")).isPresent());
    }

    private static RawDefinition system(long ordinal, String source, String name) { return definition(RawDefinitionKind.SYSTEM, ordinal, source, null, name); }
    private static RawDefinition rule(long ordinal, String source, String owner, String name) { return definition(RawDefinitionKind.RULE_VIEW, ordinal, source, owner, name); }
    private static RawDefinition definition(RawDefinitionKind kind, long ordinal, String source, String owner, String name) {
        SourceRef ref = new SourceRef(source, 1, 1, "/definition");
        return new RawDefinition(kind, ordinal, ref, owner == null ? Optional.<String>empty() : Optional.of(owner), Optional.of(name), Collections.<String,String>emptyMap(), Collections.emptyList(), new RawNodeBody(kind.name().toLowerCase(), Collections.<String,String>emptyMap(), Optional.<String>empty(), Collections.<RawNodeBody>emptyList(), ref), DocumentFormat.XML, "1.0");
    }
    private static SymbolBuildResult build(List<RawDefinition> definitions) { return new SymbolTableBuilder().build(new RawDefinitionSet(definitions)); }
    private static SymbolTable table(SymbolBuildResult result) { assertEquals(SymbolBuildStatus.BUILT, result.status()); assertTrue(result.symbolTable().isPresent()); return result.symbolTable().get(); }
}
