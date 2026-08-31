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
     * 一次性冻结发布事实闭包。任何 ERROR 或身份错配都必须在此边界失败。
     *
     * @param sourceManifest Context 中立 SourceManifest 发布视图
     * @param viewMaterializationIndex 随模型原子发布的 View 物化索引
     * @param modelAccessPolicyIndex 随模型原子发布的精确 ModelAccess 授权索引
     * @param definitions 已编译定义 Registry
     * @param deferred Deferred Registry
     * @param diagnostics 无 ERROR 的稳定诊断集合
     * @param digestPair 源摘要和基础语义摘要
     * @param compilerVersion Compiler 版本
     * @param schemaVersion Schema 版本
     * @param optionsVersion 编译选项版本
     */
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
        this.viewMaterializationIndex = Objects.requireNonNull(
                viewMaterializationIndex,
                "viewMaterializationIndex");
        this.modelAccessPolicyIndex = Objects.requireNonNull(
                modelAccessPolicyIndex,
                "modelAccessPolicyIndex");
        this.definitions = snapshotDefinitions(
                Objects.requireNonNull(definitions, "definitions"));
        this.deferred = snapshotDeferred(Objects.requireNonNull(deferred, "deferred"));
        this.diagnostics = immutablePublishedDiagnostics(diagnostics);
        this.digestPair = withRuntimeAggregateDigest(
                Objects.requireNonNull(digestPair, "digestPair"),
                this.viewMaterializationIndex,
                this.modelAccessPolicyIndex);
        this.compilerVersion = AbstractDefinitionKey.requireText(
                compilerVersion,
                "compilerVersion");
        this.schemaVersion = AbstractDefinitionKey.requireText(
                schemaVersion,
                "schemaVersion");
        this.optionsVersion = AbstractDefinitionKey.requireText(
                optionsVersion,
                "optionsVersion");
        this.typedRegistries = TypedDefinitionRegistries.from(this.definitions);
    }

    /** 返回 Context 中立 SourceManifest 发布视图。 */
    public PublishedSourceManifest sourceManifest() {
        return sourceManifest;
    }

    /** 返回随模型原子发布的 View 物化索引。 */
    public CompiledViewMaterializationIndex viewMaterializationIndex() {
        return viewMaterializationIndex;
    }

    /** 返回与当前模型同一快照原子发布的精确授权索引。 */
    public ModelAccessPolicyIndex modelAccessPolicyIndex() {
        return modelAccessPolicyIndex;
    }

    /** 返回完整 Definition Registry，供统一遍历和兼容读取使用。 */
    public Registry<DefinitionKey, CompiledDefinition> definitions() {
        return definitions;
    }

    /** 返回按 TypedKey 类型拆分的正式发布 Registry。 */
    public TypedDefinitionRegistries typedRegistries() {
        return typedRegistries;
    }

    /** 返回不可变 Deferred Registry。 */
    public DeferredRegistry deferred() {
        return deferred;
    }

    /** 返回无 ERROR 的稳定诊断集合。 */
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /** 返回源摘要和包含 P2 runtime aggregates 的最终语义摘要。 */
    public DigestPair digestPair() {
        return digestPair;
    }

    /** 返回 Compiler 版本。 */
    public String compilerVersion() {
        return compilerVersion;
    }

    /** 返回 Schema 版本。 */
    public String schemaVersion() {
        return schemaVersion;
    }

    /** 返回编译选项版本。 */
    public String optionsVersion() {
        return optionsVersion;
    }

    private static ImmutableRegistry<DefinitionKey, CompiledDefinition> snapshotDefinitions(
            Registry<DefinitionKey, CompiledDefinition> source) {
        Map<DefinitionKey, CompiledDefinition> copy =
                new LinkedHashMap<DefinitionKey, CompiledDefinition>();
        for (DefinitionKey key : source.keys()) {
            DefinitionKey nonNullKey = Objects.requireNonNull(
                    key,
                    "definitions contains null key");
            CompiledDefinition definition = Objects.requireNonNull(
                    source.require(nonNullKey),
                    "definitions contains null value");
            // Registry 外部身份必须和 Definition 内部身份完全一致。
            if (!nonNullKey.equals(definition.key())) {
                throw new IllegalArgumentException(
                        "Definition registry identity mismatch: map key="
                                + nonNullKey
                                + ", definition key="
                                + definition.key());
            }
            copy.put(nonNullKey, definition);
        }
        return new ImmutableRegistry<DefinitionKey, CompiledDefinition>(copy);
    }

    private static ImmutableDeferredRegistry snapshotDeferred(DeferredRegistry source) {
        Map<DeferredKey, DeferredDefinition> copy =
                new LinkedHashMap<DeferredKey, DeferredDefinition>();
        for (DeferredKey key : source.keys()) {
            DeferredKey nonNullKey = Objects.requireNonNull(
                    key,
                    "deferred contains null key");
            DeferredDefinition definition = source.find(nonNullKey).orElseThrow(
                    () -> new IllegalArgumentException(
                            "Deferred registry key has no definition: " + nonNullKey));
            // DeferredKey 同时冻结 owner、kind 和 ordinal，禁止 Map key 与值身份分裂。
            if (!nonNullKey.equals(definition.key())) {
                throw new IllegalArgumentException(
                        "Deferred registry identity mismatch: map key="
                                + nonNullKey
                                + ", definition key="
                                + definition.key());
            }
            copy.put(nonNullKey, definition);
        }
        return new ImmutableDeferredRegistry(copy);
    }

    private static List<Diagnostic> immutablePublishedDiagnostics(
            List<Diagnostic> values) {
        Objects.requireNonNull(values, "diagnostics");
        List<Diagnostic> copy = new ArrayList<Diagnostic>(values.size());
        for (Diagnostic diagnostic : values) {
            Diagnostic nonNullDiagnostic = Objects.requireNonNull(
                    diagnostic,
                    "diagnostics contains null");
            // CompiledModelSet 代表可发布聚合，任何 ERROR 都必须在构造前阻断。
            if (nonNullDiagnostic.severity() == DiagnosticSeverity.ERROR) {
                throw new IllegalArgumentException(
                        "CompiledModelSet must not contain ERROR diagnostic: "
                                + nonNullDiagnostic.code().code());
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
        return Objects.hash(
                sourceManifest,
                viewMaterializationIndex,
                modelAccessPolicyIndex,
                definitions,
                typedRegistries,
                deferred,
                diagnostics,
                digestPair,
                compilerVersion,
                schemaVersion,
                optionsVersion);
    }

    @Override
    public String toString() {
        return "CompiledModelSet{"
                + "sources=" + sourceManifest.sources().size()
                + ", materializationPlans=" + viewMaterializationIndex.viewKeys().size()
                + ", modelAccessRules=" + modelAccessPolicyIndex.keys().size()
                + ", definitions=" + definitions.size()
                + ", deferred=" + deferred.size()
                + ", digestPair=" + digestPair
                + '}';
    }

    /**
     * 将 compiler semantic digest 与全部 mandatory runtime aggregate 组合为最终发布摘要。
     * sourceDigest 保持原始输入 provenance，不被运行聚合重写。
     */
    private static DigestPair withRuntimeAggregateDigest(
            DigestPair base,
            CompiledViewMaterializationIndex materializationIndex,
            ModelAccessPolicyIndex policyIndex) {
        return new DigestPair(
                base.sourceDigest(),
                sha256(base.semanticDigest()
                        + "\nmaterialization="
                        + materializationIndex.canonicalForm()
                        + "\nmodelAccessPolicy="
                        + policyIndex.canonicalForm()));
    }

    /** 使用固定 UTF-8 和小写十六进制生成稳定 SHA-256。 */
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
