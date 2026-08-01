package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CompiledModelSet {
    private final ImmutableRegistry<DefinitionKey, CompiledDefinition> definitions;
    private final ImmutableDeferredRegistry deferred;
    private final List<Diagnostic> diagnostics;
    private final DigestPair digestPair;
    private final String compilerVersion;
    private final String schemaVersion;
    private final String optionsVersion;

    public CompiledModelSet(Registry<DefinitionKey, CompiledDefinition> definitions,
                            DeferredRegistry deferred, List<Diagnostic> diagnostics,
                            DigestPair digestPair, String compilerVersion,
                            String schemaVersion, String optionsVersion) {
        this.definitions = snapshotDefinitions(Objects.requireNonNull(definitions, "definitions"));
        this.deferred = snapshotDeferred(Objects.requireNonNull(deferred, "deferred"));
        List<Diagnostic> diagnosticCopy = new ArrayList<Diagnostic>(Objects.requireNonNull(diagnostics, "diagnostics"));
        if (diagnosticCopy.contains(null)) throw new NullPointerException("diagnostics contains null");
        Collections.sort(diagnosticCopy);
        this.diagnostics = Collections.unmodifiableList(diagnosticCopy);
        this.digestPair = Objects.requireNonNull(digestPair, "digestPair");
        this.compilerVersion = AbstractDefinitionKey.requireText(compilerVersion, "compilerVersion");
        this.schemaVersion = AbstractDefinitionKey.requireText(schemaVersion, "schemaVersion");
        this.optionsVersion = AbstractDefinitionKey.requireText(optionsVersion, "optionsVersion");
    }

    private static ImmutableRegistry<DefinitionKey, CompiledDefinition> snapshotDefinitions(
            Registry<DefinitionKey, CompiledDefinition> source) {
        Map<DefinitionKey, CompiledDefinition> copy = new LinkedHashMap<DefinitionKey, CompiledDefinition>();
        for (DefinitionKey key : source.keys()) copy.put(key, source.require(key));
        return new ImmutableRegistry<DefinitionKey, CompiledDefinition>(copy);
    }

    private static ImmutableDeferredRegistry snapshotDeferred(DeferredRegistry source) {
        Map<DeferredKey, DeferredDefinition> copy = new LinkedHashMap<DeferredKey, DeferredDefinition>();
        for (DeferredKey key : source.keys()) copy.put(key, source.find(key).orElseThrow(
                () -> new IllegalArgumentException("Deferred registry key has no definition: " + key)));
        return new ImmutableDeferredRegistry(copy);
    }

    public Registry<DefinitionKey, CompiledDefinition> definitions() { return definitions; }
    public DeferredRegistry deferred() { return deferred; }
    public List<Diagnostic> diagnostics() { return diagnostics; }
    public DigestPair digestPair() { return digestPair; }
    public String compilerVersion() { return compilerVersion; }
    public String schemaVersion() { return schemaVersion; }
    public String optionsVersion() { return optionsVersion; }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CompiledModelSet)) return false;
        CompiledModelSet that = (CompiledModelSet) other;
        return definitions.equals(that.definitions) && deferred.equals(that.deferred)
                && diagnostics.equals(that.diagnostics) && digestPair.equals(that.digestPair)
                && compilerVersion.equals(that.compilerVersion) && schemaVersion.equals(that.schemaVersion)
                && optionsVersion.equals(that.optionsVersion);
    }
    @Override public int hashCode() { return Objects.hash(definitions, deferred, diagnostics, digestPair, compilerVersion, schemaVersion, optionsVersion); }
    @Override public String toString() { return "CompiledModelSet{" + definitions.size() + "," + deferred.size() + "," + digestPair + "}"; }
}
