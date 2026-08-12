package dec.core.context.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.CoreConfigProjection;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DataKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DeferredKind;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.InformationKey;
import dec.core.context.model.NormalizedBody;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.RequiredStage;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 验证 Context 公共值对象的防御性复制、稳定排序和派生行为。
 */
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
        ImmutableRegistry<DefinitionKey, String> registry =
                new ImmutableRegistry<DefinitionKey, String>(source);
        source.put(new DataKey("second"), "two");
        assertEquals(1, registry.size());
        assertThrows(UnsupportedOperationException.class, () -> registry.keys().add(first));

        CompiledModelSet model = emptyModelSet();
        CoreConfigProjection projection = CoreConfigProjection.from(model);
        assertSame(model, projection.sourceModelSet());
        assertThrows(
                UnsupportedOperationException.class,
                () -> projection.data().add(compiledDefinition(new DataKey("x"))));
    }

    @Test
    void deferredDefinitionDefensivelyCopiesAndUsesStringReasonCode() {
        List<DefinitionKey> refs = new ArrayList<DefinitionKey>();
        refs.add(new DataKey("z"));
        refs.add(new DataKey("a"));
        DeferredKey key = new DeferredKey(
                new SystemKey("payment"),
                DeferredKind.INFORMATION,
                0);
        DeferredDefinition definition = new DeferredDefinition(
                key,
                RequiredStage.P3,
                "MIX-INFORMATION-OWNER",
                new SourceRef("mix", 1, 2, "/information"),
                new NormalizedBody("expression", "payment.success"),
                refs);
        refs.clear();

        assertEquals(key, definition.key());
        assertEquals(2, definition.resolvedReferences().size());
        assertEquals("data:a", definition.resolvedReferences().get(0).canonical());
        assertEquals("MIX-INFORMATION-OWNER", definition.reasonCode());
        assertThrows(
                UnsupportedOperationException.class,
                () -> definition.resolvedReferences().add(new DataKey("x")));
    }

    @Test
    void diagnosticsUseStableRequiredOrderingAndCodes() {
        Diagnostic later = diagnostic(
                2,
                DiagnosticCode.MIX_REF_UNKNOWN,
                new DataKey("b"),
                "ReferenceResolutionPass");
        Diagnostic earlier = diagnostic(
                1,
                DiagnosticCode.MIX_SYMBOL_DUPLICATE,
                new DataKey("z"),
                "SymbolRegistrationPass");
        List<Diagnostic> values = new ArrayList<Diagnostic>(Arrays.asList(later, earlier));
        Collections.sort(values);

        assertSame(earlier, values.get(0));
        assertEquals("MIX-REF-UNKNOWN", DiagnosticCode.MIX_REF_UNKNOWN.code());
        assertEquals(
                "MIX-PUBLICATION-PROVENANCE-MISMATCH",
                DiagnosticCode.MIX_PUBLICATION_PROVENANCE_MISMATCH.code());
        assertEquals(31, DiagnosticCode.values().length);
    }

    @Test
    void compiledModelSetSnapshotsRegistriesAndDiagnostics() {
        Map<DefinitionKey, CompiledDefinition> definitions =
                new LinkedHashMap<DefinitionKey, CompiledDefinition>();
        DataKey key = new DataKey("orders");
        definitions.put(key, compiledDefinition(key));
        ImmutableRegistry<DefinitionKey, CompiledDefinition> registry =
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(definitions);

        Map<DeferredKey, DeferredDefinition> deferredValues =
                new LinkedHashMap<DeferredKey, DeferredDefinition>();
        DeferredKey deferredKey = new DeferredKey(
                new SystemKey("payment"),
                DeferredKind.INFORMATION,
                0);
        deferredValues.put(
                deferredKey,
                new DeferredDefinition(
                        deferredKey,
                        RequiredStage.P3,
                        "MIX-INFORMATION-OWNER",
                        new SourceRef("mix", 2, 1, "/information"),
                        new NormalizedBody("expression", "payment.success"),
                        Collections.<DefinitionKey>emptyList()));
        ImmutableDeferredRegistry deferred = new ImmutableDeferredRegistry(deferredValues);
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        CompiledModelSet model = new CompiledModelSet(
                PublishedSourceManifest.empty(),
                dec.core.context.model.CompiledViewMaterializationIndex.empty(),
                registry,
                deferred,
                diagnostics,
                new DigestPair("source", "semantic"),
                "1",
                "1",
                "1");

        definitions.clear();
        deferredValues.clear();
        diagnostics.add(diagnostic(3, DiagnosticCode.MIX_REF_UNKNOWN, key, "pass"));

        assertEquals(1, model.definitions().size());
        assertEquals(1, model.typedRegistries().data().size());
        assertEquals(1, model.deferred().size());
        assertTrue(model.diagnostics().isEmpty());
    }

    private static CompiledModelSet emptyModelSet() {
        return new CompiledModelSet(
                PublishedSourceManifest.empty(),
                dec.core.context.model.CompiledViewMaterializationIndex.empty(),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                Collections.<Diagnostic>emptyList(),
                new DigestPair("source", "semantic"),
                "1",
                "1",
                "1");
    }

    private static CompiledDefinition compiledDefinition(DefinitionKey key) {
        return new CompiledDefinition(
                key,
                new SourceRef("mix", 1, 1, "/definition"),
                new NormalizedBody("canonical", key.canonical()));
    }

    private static Diagnostic diagnostic(
            int line,
            DiagnosticCode code,
            DefinitionKey key,
            String pass) {
        return new Diagnostic(
                code,
                DiagnosticSeverity.ERROR,
                "message",
                key,
                new SourceRef("mix", line, 1, "/node"),
                Collections.<SourceRef>emptyList(),
                null,
                pass);
    }
}
