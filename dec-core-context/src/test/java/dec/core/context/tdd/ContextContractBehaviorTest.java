package dec.core.context.tdd;

import dec.core.context.CoreConfigProjection;
import dec.core.context.model.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ContextContractBehaviorTest {
    @Test
    void informationKeyUsesSystemQualifiedCanonicalForm() {
        InformationKey key = new InformationKey(new SystemKey("payment"), "success");
        assertEquals("system:payment.success", key.canonical());
        assertEquals(key, new InformationKey(new SystemKey("payment"), "success"));
        assertNotEquals(key, new InformationKey(new SystemKey("order"), "success"));
    }

    @Test
    void registryAndProjectionDefensivelyCopyInputs() {
        Map<DefinitionKey, String> source = new LinkedHashMap<DefinitionKey, String>();
        DataKey first = new DataKey("first");
        source.put(first, "one");
        ImmutableRegistry<DefinitionKey, String> registry = new ImmutableRegistry<DefinitionKey, String>(source);
        source.put(new DataKey("second"), "two");
        assertEquals(1, registry.size());
        assertThrows(UnsupportedOperationException.class, () -> registry.keys().add(first));

        List<String> data = new ArrayList<String>(Collections.singletonList("a"));
        CoreConfigProjection projection = new CoreConfigProjection(data, Collections.emptyList(), Collections.emptyList());
        data.add("b");
        assertEquals(Collections.singletonList("a"), projection.data());
        assertThrows(UnsupportedOperationException.class, () -> ((List<Object>) projection.data()).add("x"));
    }

    @Test
    void deferredDefinitionDefensivelyCopiesAndUsesStringReasonCode() {
        List<DefinitionKey> refs = new ArrayList<DefinitionKey>();
        refs.add(new DataKey("z"));
        refs.add(new DataKey("a"));
        DeferredDefinition definition = new DeferredDefinition(
                new SystemKey("payment"), DeferredKind.INFORMATION, RequiredStage.P3,
                "MIX-INFORMATION-OWNER", new SourceRef("mix", 1, 2, "/information"),
                new NormalizedBody("expression", "payment.success"), refs);
        refs.clear();
        assertEquals(2, definition.resolvedReferences().size());
        assertEquals("data:a", definition.resolvedReferences().get(0).canonical());
        assertEquals("MIX-INFORMATION-OWNER", definition.reasonCode());
        assertThrows(UnsupportedOperationException.class,
                () -> definition.resolvedReferences().add(new DataKey("x")));
    }

    @Test
    void diagnosticsUseStableRequiredOrderingAndCodes() {
        Diagnostic later = diagnostic(2, DiagnosticCode.MIX_REF_UNKNOWN, new DataKey("b"), "ReferenceResolutionPass");
        Diagnostic earlier = diagnostic(1, DiagnosticCode.MIX_SYMBOL_DUPLICATE, new DataKey("z"), "SymbolRegistrationPass");
        List<Diagnostic> values = new ArrayList<Diagnostic>(Arrays.asList(later, earlier));
        Collections.sort(values);
        assertSame(earlier, values.get(0));
        assertEquals("MIX-REF-UNKNOWN", DiagnosticCode.MIX_REF_UNKNOWN.code());
        assertEquals(30, DiagnosticCode.values().length);
    }

    @Test
    void compiledModelSetSnapshotsRegistriesAndDiagnostics() {
        Map<DefinitionKey, CompiledDefinition> definitions = new LinkedHashMap<DefinitionKey, CompiledDefinition>();
        DataKey key = new DataKey("orders");
        definitions.put(key, new CompiledDefinition(key, new SourceRef("mix", 1, 1, "/data"), new NormalizedBody("canonical", "orders")));
        ImmutableRegistry<DefinitionKey, CompiledDefinition> registry = new ImmutableRegistry<DefinitionKey, CompiledDefinition>(definitions);

        Map<DeferredKey, DeferredDefinition> deferredValues = new LinkedHashMap<DeferredKey, DeferredDefinition>();
        DeferredKey deferredKey = new DeferredKey(new SystemKey("payment"), DeferredKind.INFORMATION, 0);
        deferredValues.put(deferredKey, new DeferredDefinition(new SystemKey("payment"), DeferredKind.INFORMATION,
                RequiredStage.P3, "MIX-INFORMATION-OWNER", new SourceRef("mix", 2, 1, "/information"),
                new NormalizedBody("expression", "payment.success"), Collections.<DefinitionKey>emptyList()));
        ImmutableDeferredRegistry deferred = new ImmutableDeferredRegistry(deferredValues);
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        CompiledModelSet model = new CompiledModelSet(registry, deferred, diagnostics,
                new DigestPair("source", "semantic"), "1", "1", "1");
        definitions.clear(); deferredValues.clear(); diagnostics.add(diagnostic(3, DiagnosticCode.MIX_REF_UNKNOWN, key, "pass"));
        assertEquals(1, model.definitions().size());
        assertEquals(1, model.deferred().size());
        assertTrue(model.diagnostics().isEmpty());
    }

    private static Diagnostic diagnostic(int line, DiagnosticCode code, DefinitionKey key, String pass) {
        return new Diagnostic(code, DiagnosticSeverity.ERROR, "message", key,
                new SourceRef("mix", line, 1, "/node"), Collections.<SourceRef>emptyList(), null, pass);
    }
}
