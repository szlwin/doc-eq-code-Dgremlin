package dec.core.compiler.compiled;

import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.PublishedSourceDependency;
import dec.core.context.model.PublishedSourceDescriptor;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.Registry;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * DEC-SEMANTIC-DIGEST-V1 的不可变语义事实快照。
 *
 * <p>构造时即完成全部领域排序和 SourceRef 物理位置剥离，后续 Registry
 * 或调用方集合变化不会改变已经生成的 canonical JSON。</p>
 */
public final class SemanticDigestInput {
    public static final String DIGEST_ALGORITHM_VERSION =
            "DEC-SEMANTIC-DIGEST-V1";

    private static final Comparator<String> TEXT_ORDER =
            CanonicalJsonWriter.codePointOrder();

    private final String compilerVersion;
    private final String schemaVersion;
    private final String optionsDigest;
    private final String canonicalJson;

    /**
     * 冻结发布清单、定义、Deferred 与版本域。
     *
     * @param sourceManifest Context 中立 SourceManifest 发布视图
     * @param definitions 已编译定义 Registry
     * @param deferred Deferred Registry
     * @param compilerVersion Compiler 版本
     * @param schemaVersion Schema 版本
     * @param optionsDigest 编译选项摘要或稳定版本
     */
    public SemanticDigestInput(
            PublishedSourceManifest sourceManifest,
            Registry<DefinitionKey, CompiledDefinition> definitions,
            DeferredRegistry deferred,
            String compilerVersion,
            String schemaVersion,
            String optionsDigest) {
        PublishedSourceManifest checkedManifest = Objects.requireNonNull(
                sourceManifest,
                "sourceManifest");
        Registry<DefinitionKey, CompiledDefinition> checkedDefinitions =
                Objects.requireNonNull(definitions, "definitions");
        DeferredRegistry checkedDeferred = Objects.requireNonNull(
                deferred,
                "deferred");
        this.compilerVersion = requireText(compilerVersion, "compilerVersion");
        this.schemaVersion = requireText(schemaVersion, "schemaVersion");
        this.optionsDigest = requireText(optionsDigest, "optionsDigest");

        Map<String, Object> root = object();
        root.put("algorithm", DIGEST_ALGORITHM_VERSION);
        root.put("compilerVersion", this.compilerVersion);
        root.put("definitions", definitionFacts(checkedDefinitions));
        root.put("deferred", deferredFacts(checkedDeferred));
        root.put("optionsDigest", this.optionsDigest);
        root.put("schemaVersion", this.schemaVersion);
        root.put("sourceManifest", sourceManifestFacts(checkedManifest));
        this.canonicalJson = CanonicalJsonWriter.write(root);
    }

    /** 返回固定摘要算法版本。 */
    public String digestAlgorithmVersion() {
        return DIGEST_ALGORITHM_VERSION;
    }

    /** 返回 Compiler 版本。 */
    public String compilerVersion() {
        return compilerVersion;
    }

    /** 返回 Schema 版本。 */
    public String schemaVersion() {
        return schemaVersion;
    }

    /** 返回编译选项摘要。 */
    public String optionsDigest() {
        return optionsDigest;
    }

    /** 返回构造时冻结的 UTF-8 canonical JSON 文本。 */
    public String canonicalJson() {
        return canonicalJson;
    }

    /** 构造不包含格式和内容摘要的 SourceManifest 语义视图。 */
    private static Map<String, Object> sourceManifestFacts(
            PublishedSourceManifest manifest) {
        Map<String, Object> result = object();
        result.put("rootSourceId", manifest.rootSourceId());

        List<String> sourceIds = new ArrayList<String>();
        for (PublishedSourceDescriptor source : manifest.sources()) {
            sourceIds.add(Objects.requireNonNull(source, "source").sourceId());
        }
        Collections.sort(sourceIds, TEXT_ORDER);
        List<Object> sources = new ArrayList<Object>(sourceIds.size());
        for (String sourceId : sourceIds) {
            Map<String, Object> source = object();
            source.put("sourceId", sourceId);
            sources.add(source);
        }
        result.put("sources", sources);

        List<PublishedSourceDependency> dependencies =
                new ArrayList<PublishedSourceDependency>(manifest.dependencies());
        Collections.sort(dependencies, new Comparator<PublishedSourceDependency>() {
            @Override
            public int compare(
                    PublishedSourceDependency left,
                    PublishedSourceDependency right) {
                return TEXT_ORDER.compare(
                        dependencyKey(left),
                        dependencyKey(right));
            }
        });
        List<Object> dependencyFacts = new ArrayList<Object>(dependencies.size());
        for (PublishedSourceDependency dependency : dependencies) {
            Map<String, Object> fact = object();
            fact.put("declaration", sourceRefFact(dependency.declarationSourceRef()));
            fact.put("edgeType", dependency.edgeType());
            fact.put("fromSourceId", dependency.fromSourceId());
            fact.put("targetSourceId", dependency.targetSourceId());
            dependencyFacts.add(fact);
        }
        result.put("dependencies", dependencyFacts);
        return result;
    }

    /** 生成不含物理行列的稳定依赖排序 key。 */
    private static String dependencyKey(PublishedSourceDependency dependency) {
        SourceRef sourceRef = dependency.declarationSourceRef();
        return dependency.fromSourceId()
                + '\u0000' + dependency.edgeType()
                + '\u0000' + dependency.targetSourceId()
                + '\u0000' + sourceRef.sourceId()
                + '\u0000' + sourceRef.nodePath();
    }

    /** 按 DefinitionKey canonical 排序并复制定义语义事实。 */
    private static List<Object> definitionFacts(
            Registry<DefinitionKey, CompiledDefinition> definitions) {
        List<DefinitionKey> keys = new ArrayList<DefinitionKey>(definitions.keys());
        Collections.sort(keys, new Comparator<DefinitionKey>() {
            @Override
            public int compare(DefinitionKey left, DefinitionKey right) {
                return TEXT_ORDER.compare(left.canonical(), right.canonical());
            }
        });
        ensureUniqueDefinitionKeys(keys);

        List<Object> result = new ArrayList<Object>(keys.size());
        for (DefinitionKey key : keys) {
            DefinitionKey checkedKey = Objects.requireNonNull(key, "definition key");
            CompiledDefinition definition = Objects.requireNonNull(
                    definitions.require(checkedKey),
                    "definition");
            if (!checkedKey.equals(definition.key())) {
                throw new IllegalArgumentException(
                        "definition registry identity mismatch: "
                                + checkedKey.canonical());
            }
            Map<String, Object> fact = object();
            fact.put("body", bodyFact(
                    definition.normalizedBody().format(),
                    definition.normalizedBody().value()));
            fact.put("key", checkedKey.canonical());
            fact.put("source", sourceRefFact(definition.sourceRef()));
            result.add(fact);
        }
        return result;
    }

    /** 拒绝不同 DefinitionKey 对同一 canonical 文本的歧义。 */
    private static void ensureUniqueDefinitionKeys(List<DefinitionKey> keys) {
        String previous = null;
        for (DefinitionKey key : keys) {
            String canonical = Objects.requireNonNull(key, "definition key").canonical();
            if (canonical.equals(previous)) {
                throw new IllegalArgumentException(
                        "duplicate semantic definition key: " + canonical);
            }
            previous = canonical;
        }
    }

    /** 按 DeferredKey canonical 排序并复制 Deferred 语义事实。 */
    private static List<Object> deferredFacts(DeferredRegistry deferred) {
        List<DeferredKey> keys = new ArrayList<DeferredKey>(deferred.keys());
        Collections.sort(keys, new Comparator<DeferredKey>() {
            @Override
            public int compare(DeferredKey left, DeferredKey right) {
                return TEXT_ORDER.compare(left.canonical(), right.canonical());
            }
        });
        ensureUniqueDeferredKeys(keys);

        List<Object> result = new ArrayList<Object>(keys.size());
        for (DeferredKey key : keys) {
            DeferredDefinition definition = deferred.find(key).orElse(null);
            if (definition == null) {
                throw new IllegalArgumentException(
                        "deferred key has no definition: " + key.canonical());
            }
            if (!key.equals(definition.key())) {
                throw new IllegalArgumentException(
                        "deferred registry identity mismatch: " + key.canonical());
            }
            Map<String, Object> fact = object();
            fact.put("body", bodyFact(
                    definition.body().format(),
                    definition.body().value()));
            fact.put("key", key.canonical());
            fact.put("reasonCode", definition.reasonCode());
            fact.put("requiredStage", definition.requiredStage().name());
            fact.put("resolvedReferences", resolvedReferences(definition));
            fact.put("source", sourceRefFact(definition.sourceRef()));
            result.add(fact);
        }
        return result;
    }

    /** 拒绝 Deferred canonical key 冲突。 */
    private static void ensureUniqueDeferredKeys(List<DeferredKey> keys) {
        String previous = null;
        for (DeferredKey key : keys) {
            String canonical = Objects.requireNonNull(key, "deferred key").canonical();
            if (canonical.equals(previous)) {
                throw new IllegalArgumentException(
                        "duplicate semantic deferred key: " + canonical);
            }
            previous = canonical;
        }
    }

    /** 复制并排序 Deferred 的强类型引用 canonical key。 */
    private static List<Object> resolvedReferences(
            DeferredDefinition definition) {
        List<String> values = new ArrayList<String>();
        for (DefinitionKey reference : definition.resolvedReferences()) {
            values.add(Objects.requireNonNull(reference, "reference").canonical());
        }
        Collections.sort(values, TEXT_ORDER);
        return new ArrayList<Object>(values);
    }

    /** 编码 NormalizedBody 的 format/value。 */
    private static Map<String, Object> bodyFact(String format, String value) {
        Map<String, Object> result = object();
        result.put("format", format);
        result.put("value", value);
        return result;
    }

    /** SourceRef 只保留语义 sourceId/nodePath，明确排除 line/column。 */
    private static Map<String, Object> sourceRefFact(SourceRef sourceRef) {
        SourceRef checked = Objects.requireNonNull(sourceRef, "sourceRef");
        Map<String, Object> result = object();
        result.put("nodePath", checked.nodePath());
        result.put("sourceId", checked.sourceId());
        return result;
    }

    /** 创建只在构造线程内使用的 JSON Object。 */
    private static Map<String, Object> object() {
        return new LinkedHashMap<String, Object>();
    }

    /** 规范化必填版本域文本。 */
    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return checked;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof SemanticDigestInput
                && canonicalJson.equals(
                        ((SemanticDigestInput) other).canonicalJson));
    }

    @Override
    public int hashCode() {
        return canonicalJson.hashCode();
    }

    @Override
    public String toString() {
        return "SemanticDigestInput{" + DIGEST_ALGORITHM_VERSION + '}';
    }
}
