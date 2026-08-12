package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Compiler 发布给运行上下文的完整、不可变模型事实集合。
 */
public final class CompiledModelSet {
    private final PublishedSourceManifest sourceManifest;
    private final CompiledViewMaterializationIndex viewMaterializationIndex;
    private final ModelAccessPolicyIndex modelAccessPolicyIndex;
    private final ImmutableRegistry<DefinitionKey, CompiledDefinition> definitions;
    private final TypedDefinitionRegistries typedRegistries;
    private final ImmutableDeferredRegistry deferred;
    private final List<Diagnostic> diagnostics;
    private final DigestPair digestPair;
    private final String compilerVersion;
    private final String schemaVersion;
    private final String optionsVersion;

    /**
     * helper-only 旧构造兼容：仅为导出完整源码快照，正式 feature 分支不保留此入口。
     */
    @Deprecated
    public CompiledModelSet(
            PublishedSourceManifest sourceManifest,
            CompiledViewMaterializationIndex viewMaterializationIndex,
            Registry<DefinitionKey, CompiledDefinition> definitions,
            DeferredRegistry deferred,
            List<Diagnostic> diagnostics,
            DigestPair digestPair,
            String compilerVersion,
            String schemaVersion,
            String optionsVersion) {
        this(
                sourceManifest,
                viewMaterializationIndex,
                ModelAccessPolicyIndex.empty(),
                definitions,
                deferred,
                diagnostics,
                digestPair,
                compilerVersion,
                schemaVersion,
                optionsVersion);
    }

    /** 一次性冻结发布事实闭包。 */
    public CompiledModelSet(
            PublishedSourceManifest sourceManifest,
            CompiledViewMaterializationIndex viewMaterializationIndex,
            ModelAccessPolicyIndex modelAccessPolicyIndex,
            Registry<DefinitionKey, CompiledDefinition> definitions,
            DeferredRegistry deferred,
            List<Diagnostic> diagnostics,
            DigestPair digestPair,
            String compilerVersion,
            String schemaVersion,
            String optionsVersion) {
        this.sourceManifest = Objects.requireNonNull(sourceManifest, "sourceManifest");
        this.viewMaterializationIndex = Objects.requireNonNull(viewMaterializationIndex, "viewMaterializationIndex");
        this.modelAccessPolicyIndex = Objects.requireNonNull(modelAccessPolicyIndex, "modelAccessPolicyIndex");
        this.definitions = snapshotDefinitions(Objects.requireNonNull(definitions, "definitions"));
        this.deferred = snapshotDeferred(Objects.requireNonNull(deferred, "deferred"));
        this.diagnostics = immutablePublishedDiagnostics(diagnostics);
        this.digestPair = withRuntimeAggregateDigest(
                Objects.requireNonNull(digestPair, "digestPair"),
                this.viewMaterializationIndex,
                this.modelAccessPolicyIndex);
        this.compilerVersion = AbstractDefinitionKey.requireText(compilerVersion, "compilerVersion");
        this.schemaVersion = AbstractDefinitionKey.requireText(schemaVersion, "schemaVersion");
        this.optionsVersion = AbstractDefinitionKey.requireText(optionsVersion, "optionsVersion");
        this.typedRegistries = TypedDefinitionRegistries.from(this.definitions);
    }

    public PublishedSourceManifest sourceManifest() {
        return sourceManifest;
    }

    public CompiledViewMaterializationIndex viewMaterializationIndex() {
        return viewMaterializationIndex;
    }

    public ModelAccessPolicyIndex modelAccessPolicyIndex() {
        return modelAccessPolicyIndex;
    }

    public Registry<DefinitionKey, CompiledDefinition> definitions() {
        return definitions;
    }

    public TypedDefinitionRegistries typedRegistries() {
        return typedRegistries;
    }

    public DeferredRegistry deferred() {
        return deferred;
    }

    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    public DigestPair digestPair() {
        return digestPair;
    }

    public String compilerVersion() {
        return compilerVersion;
    }

    public String schemaVersion() {
        return schemaVersion;
    }

    public String optionsVersion() {
        return optionsVersion;
    }

    private static ImmutableRegistry<DefinitionKey, CompiledDefinition> snapshotDefinitions(
            Registry<DefinitionKey, CompiledDefinition> source) {
        Map<DefinitionKey, CompiledDefinition> copy = new LinkedHashMap<DefinitionKey, CompiledDefinition>();
        for (DefinitionKey key : source.keys()) {
            DefinitionKey nonNullKey = Objects.requireNonNull(key, "definitions contains null key");
            CompiledDefinition definition = Objects.requireNonNull(source.require(nonNullKey), "definitions contains null value");
            if (!nonNullKey.equals(definition.key())) {
                throw new IllegalArgumentException("Definition registry identity mismatch: map key=" + nonNullKey + ", definition key=" + definition.key());
            }
            copy.put(nonNullKey, definition);
        }
        return new ImmutableRegistry<DefinitionKey, CompiledDefinition>(copy);
    }

    private static ImmutableDeferredRegistry snapshotDeferred(DeferredRegistry source) {
        Map<DeferredKey, DeferredDefinition> copy = new LinkedHashMap<DeferredKey, DeferredDefinition>();
        for (DeferredKey key : source.keys()) {
            DeferredKey nonNullKey = Objects.requireNonNull(key, "deferred contains null key");
            DeferredDefinition definition = source.find(nonNullKey).orElseThrow(
                    () -> new IllegalArgumentException("Deferred registry key has no definition: " + nonNullKey));
            if (!nonNullKey.equals(definition.key())) {
                throw new IllegalArgumentException("Deferred registry identity mismatch: map key=" + nonNullKey + ", definition key=" + definition.key());
            }
            copy.put(nonNullKey, definition);
        }
        return new ImmutableDeferredRegistry(copy);
    }

    private static List<Diagnostic> immutablePublishedDiagnostics(List<Diagnostic> values) {
        Objects.requireNonNull(values, "diagnostics");
        List<Diagnostic> copy = new ArrayList<Diagnostic>(values.size());
        for (Diagnostic diagnostic : values) {
            Diagnostic nonNullDiagnostic = Objects.requireNonNull(diagnostic, "diagnostics contains null");
            if (nonNullDiagnostic.severity() == DiagnosticSeverity.ERROR) {
                throw new IllegalArgumentException("CompiledModelSet must not contain ERROR diagnostic: " + nonNullDiagnostic.code().code());
            }
            copy.add(nonNullDiagnostic);
        }
        Collections.sort(copy);
        return Collections.unmodifiableList(copy);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CompiledModelSet)) {
            return false;
        }
        CompiledModelSet that = (CompiledModelSet) other;
        return sourceManifest.equals(that.sourceManifest)
                && viewMaterializationIndex.equals(that.viewMaterializationIndex)
                && modelAccessPolicyIndex.equals(that.modelAccessPolicyIndex)
                && definitions.equals(that.definitions)
                && typedRegistries.equals(that.typedRegistries)
                && deferred.equals(that.deferred)
                && diagnostics.equals(that.diagnostics)
                && digestPair.equals(that.digestPair)
                && compilerVersion.equals(that.compilerVersion)
                && schemaVersion.equals(that.schemaVersion)
                && optionsVersion.equals(that.optionsVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceManifest, viewMaterializationIndex, modelAccessPolicyIndex,
                definitions, typedRegistries, deferred, diagnostics, digestPair,
                compilerVersion, schemaVersion, optionsVersion);
    }

    @Override
    public String toString() {
        return "CompiledModelSet{sources=" + sourceManifest.sources().size()
                + ", materializationPlans=" + viewMaterializationIndex.viewKeys().size()
                + ", modelAccessRules=" + modelAccessPolicyIndex.keys().size()
                + ", definitions=" + definitions.size()
                + ", deferred=" + deferred.size()
                + ", digestPair=" + digestPair + '}';
    }

    private static DigestPair withRuntimeAggregateDigest(
            DigestPair base,
            CompiledViewMaterializationIndex materializationIndex,
            ModelAccessPolicyIndex policyIndex) {
        return new DigestPair(
                base.sourceDigest(),
                sha256(base.semanticDigest()
                        + "\nmaterialization=" + materializationIndex.canonicalForm()
                        + "\nmodelAccessPolicy=" + policyIndex.canonicalForm()));
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                result.append(String.format("%02x", item & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }
}
