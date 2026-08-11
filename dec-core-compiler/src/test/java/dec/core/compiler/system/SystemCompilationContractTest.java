package dec.core.compiler.system;

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
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SystemCompilationContractTest {
    @Test @DisplayName("CASE-P2-TD-SYSTEM-DETERMINISM-001")
    void systemIdentitySetIsSourceOrderIndependent() {
        SymbolTable first = table(build(Arrays.asList(system(0, "a.xml", "order", "1.0"), system(1, "b.xml", "payment", "1.0"))));
        SymbolTable second = table(build(Arrays.asList(system(0, "b.xml", "payment", "1.0"), system(1, "a.xml", "order", "1.0"))));
        assertEquals(canonicalKeys(first), canonicalKeys(second));
    }

    @Test @DisplayName("CASE-P2-TD-SYSTEM-DUPLICATE-001")
    void duplicateSystemDiagnosticIsSourceOrderIndependent() {
        SymbolBuildResult first = build(Arrays.asList(system(0, "a.xml", "order", "1.0"), system(1, "b.xml", "order", "1.0")));
        SymbolBuildResult second = build(Arrays.asList(system(0, "b.xml", "order", "1.0"), system(1, "a.xml", "order", "1.0")));
        assertEquals(SymbolBuildStatus.FAILED, first.status());
        assertEquals(SymbolBuildStatus.FAILED, second.status());
        assertFalse(first.symbolTable().isPresent());
        assertFalse(second.symbolTable().isPresent());
        assertEquals(first.diagnostics(), second.diagnostics(), "P2 RED [CASE-P2-TD-SYSTEM-DUPLICATE-001]: duplicate diagnostic must not depend on scan order");
    }

    @Test @DisplayName("CASE-P2-TD-SYSTEM-FORWARD-REF-001")
    void ruleViewMayPrecedeSystemButMissingOwnerFails() {
        RawDefinition rule = definition(RawDefinitionKind.RULE_VIEW, 0, "rule.xml", "order", "submit", "1.0", Collections.<String,String>emptyMap());
        assertEquals(SymbolBuildStatus.BUILT, build(Arrays.asList(rule, system(1, "system.xml", "order", "1.0"))).status());
        RawDefinition missing = definition(RawDefinitionKind.RULE_VIEW, 0, "rule.xml", "missing", "submit", "1.0", Collections.<String,String>emptyMap());
        assertEquals(SymbolBuildStatus.FAILED, build(Collections.singletonList(missing)).status());
    }

    @Test @DisplayName("CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001")
    void systemDefinitionIsFrozenFromMutableInputAttributes() {
        Map<String,String> attributes = new HashMap<String,String>();
        attributes.put("edition", "v1");
        RawDefinition source = definition(RawDefinitionKind.SYSTEM, 0, "system.xml", null, "order", "1.0", attributes);
        attributes.put("edition", "mutated");
        assertEquals("v1", table(build(Collections.singletonList(source))).require(new SystemKey("order")).attributes().get("edition"));
    }

    @Test @DisplayName("CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001")
    void schemaVersionChangesDefinitionValueButNotSystemIdentity() {
        SymbolTable v1 = table(build(Collections.singletonList(system(0, "system.xml", "order", "1.0"))));
        SymbolTable v2 = table(build(Collections.singletonList(system(0, "system.xml", "order", "2.0"))));
        assertEquals(v1.keys(), v2.keys());
        assertNotEquals(v1.require(new SystemKey("order")).schemaVersion(), v2.require(new SystemKey("order")).schemaVersion());
    }

    @Test @DisplayName("CASE-P2-TD-BM-CANONICAL-PAIR-001")
    void systemKeyUsesCanonicalSharedIdentity() {
        assertEquals(new SystemKey("order"), new SystemKey(" order "));
        assertEquals("system:order", new SystemKey("order").canonical());
    }

    private static RawDefinition system(long ordinal, String source, String name, String version) {
        return definition(RawDefinitionKind.SYSTEM, ordinal, source, null, name, version, Collections.<String,String>emptyMap());
    }
    private static RawDefinition definition(RawDefinitionKind kind, long ordinal, String source, String owner, String name, String version, Map<String,String> attrs) {
        SourceRef ref = new SourceRef(source, 1, 1, "/definition");
        return new RawDefinition(kind, ordinal, ref, owner == null ? Optional.<String>empty() : Optional.of(owner), Optional.of(name), attrs, Collections.emptyList(), new RawNodeBody(kind.name().toLowerCase(), Collections.<String,String>emptyMap(), Optional.<String>empty(), Collections.<RawNodeBody>emptyList(), ref), DocumentFormat.XML, version);
    }
    private static SymbolBuildResult build(List<RawDefinition> definitions) { return new SymbolTableBuilder().build(new RawDefinitionSet(definitions)); }
    private static SymbolTable table(SymbolBuildResult result) { assertEquals(SymbolBuildStatus.BUILT, result.status()); assertTrue(result.symbolTable().isPresent()); return result.symbolTable().get(); }
    private static List<String> canonicalKeys(SymbolTable table) { List<String> keys = new ArrayList<String>(); for (DefinitionKey key : table.keys()) keys.add(key.canonical()); Collections.sort(keys); return keys; }
}
