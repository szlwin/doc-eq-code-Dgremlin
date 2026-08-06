package dec.core.compiler.compiled;

import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.source.SourceManifest;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.Registry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 将模型事实、版本域和同一次 T13 计算出的摘要原子绑定为不可拆分输入。
 */
public final class DigestBoundCompiledInput {
    private static final Pattern SHA_256 = Pattern.compile("[0-9a-f]{64}");

    private final PublishedSourceManifest sourceManifest;
    private final ImmutableRegistry<DefinitionKey, CompiledDefinition> definitions;
    private final ImmutableDeferredRegistry deferred;
    private final String compilerVersion;
    private final String schemaVersion;
    private final String optionsDigest;
    private final DigestPair digestPair;

    /**
     * 只允许同包摘要服务在模型快照完成后创建绑定对象。
     */
    private DigestBoundCompiledInput(
            PublishedSourceManifest sourceManifest,
            ImmutableRegistry<DefinitionKey, CompiledDefinition> definitions,
            ImmutableDeferredRegistry deferred,
            String compilerVersion,
            String schemaVersion,
            String optionsDigest,
            DigestPair digestPair) {
        this.sourceManifest = Objects.requireNonNull(
                sourceManifest,
                "sourceManifest");
        this.definitions = Objects.requireNonNull(definitions, "definitions");
        this.deferred = Objects.requireNonNull(deferred, "deferred");
        this.compilerVersion = requireText(compilerVersion, "compilerVersion");
        this.schemaVersion = requireText(schemaVersion, "schemaVersion");
        this.optionsDigest = requireText(optionsDigest, "optionsDigest");
        this.digestPair = requireDigestPair(digestPair);
    }

    /**
     * 先完整冻结模型事实，再用同一快照计算 T13 摘要，禁止任意 DigestPair 注入。
     */
    static DigestBoundCompiledInput bind(
            CompilerDigestService digestService,
            SourceManifest sources,
            PublishedSourceManifest sourceManifest,
            Registry<DefinitionKey, CompiledDefinition> definitions,
            DeferredRegistry deferred,
            String compilerVersion,
            CompilationOptions options) {
        CompilerDigestService checkedService = Objects.requireNonNull(
                digestService,
                "digestService");
        SourceManifest checkedSources = Objects.requireNonNull(sources, "sources");
        PublishedSourceManifest checkedManifest = Objects.requireNonNull(
                sourceManifest,
                "sourceManifest");
        ImmutableRegistry<DefinitionKey, CompiledDefinition> frozenDefinitions =
                snapshotDefinitions(definitions);
        ImmutableDeferredRegistry frozenDeferred = snapshotDeferred(deferred);
        String checkedCompilerVersion = requireText(
                compilerVersion,
                "compilerVersion");
        CompilationOptions checkedOptions = Objects.requireNonNull(
                options,
                "options");

        // 摘要输入和最终发布事实共享同一组不可变快照，避免跨闭包拼接。
        SemanticDigestInput semanticInput = new SemanticDigestInput(
                checkedManifest,
                frozenDefinitions,
                frozenDeferred,
                checkedCompilerVersion,
                checkedOptions.schemaVersion(),
                checkedOptions.optionsDigest());
        DigestPair digestPair = checkedService.compute(
                checkedSources,
                semanticInput);
        return new DigestBoundCompiledInput(
                checkedManifest,
                frozenDefinitions,
                frozenDeferred,
                checkedCompilerVersion,
                checkedOptions.schemaVersion(),
                checkedOptions.optionsDigest(),
                digestPair);
    }

    /** 返回冻结的发布 SourceManifest。 */
    public PublishedSourceManifest sourceManifest() {
        return sourceManifest;
    }

    /** 返回冻结的 Definition Registry。 */
    public ImmutableRegistry<DefinitionKey, CompiledDefinition> definitions() {
        return definitions;
    }

    /** 返回冻结的 Deferred Registry。 */
    public ImmutableDeferredRegistry deferred() {
        return deferred;
    }

    /** 返回参与摘要的 Compiler 版本。 */
    public String compilerVersion() {
        return compilerVersion;
    }

    /** 返回参与摘要的 Schema 版本。 */
    public String schemaVersion() {
        return schemaVersion;
    }

    /** 返回参与摘要的选项摘要。 */
    public String optionsDigest() {
        return optionsDigest;
    }

    /** 返回由同一冻结闭包计算出的真实摘要对。 */
    public DigestPair digestPair() {
        return digestPair;
    }

    /** 对 Definition Registry 执行单次、完整且确定性的防御性复制。 */
    private static ImmutableRegistry<DefinitionKey, CompiledDefinition>
            snapshotDefinitions(
                    Registry<DefinitionKey, CompiledDefinition> source) {
        Registry<DefinitionKey, CompiledDefinition> checked =
                Objects.requireNonNull(source, "definitions");
        int declaredSize = requireNonNegativeSize(
                checked.size(),
                "definitions");
        List<DefinitionKey> keys = new ArrayList<DefinitionKey>(
                Objects.requireNonNull(checked.keys(), "definitions keys"));
        requireCompleteKeyEnumeration(
                "definitions",
                declaredSize,
                keys.size());
        Map<DefinitionKey, CompiledDefinition> copy =
                new LinkedHashMap<DefinitionKey, CompiledDefinition>();
        for (DefinitionKey key : keys) {
            DefinitionKey checkedKey = Objects.requireNonNull(
                    key,
                    "definitions contains null key");
            CompiledDefinition definition = Objects.requireNonNull(
                    checked.require(checkedKey),
                    "definitions contains null value");
            if (!checkedKey.equals(definition.key())) {
                throw new IllegalArgumentException(
                        "Definition registry identity mismatch: map key="
                                + checkedKey
                                + ", definition key="
                                + definition.key());
            }
            if (copy.put(checkedKey, definition) != null) {
                throw new IllegalArgumentException(
                        "definitions contains duplicate key: " + checkedKey);
            }
        }
        requireStableSnapshot(
                "definitions",
                declaredSize,
                copy.size(),
                checked.size());
        return new ImmutableRegistry<DefinitionKey, CompiledDefinition>(copy);
    }

    /** 对 Deferred Registry 执行单次、完整且确定性的防御性复制。 */
    private static ImmutableDeferredRegistry snapshotDeferred(
            DeferredRegistry source) {
        DeferredRegistry checked = Objects.requireNonNull(source, "deferred");
        int declaredSize = requireNonNegativeSize(checked.size(), "deferred");
        List<DeferredKey> keys = new ArrayList<DeferredKey>(
                Objects.requireNonNull(checked.keys(), "deferred keys"));
        requireCompleteKeyEnumeration("deferred", declaredSize, keys.size());
        Map<DeferredKey, DeferredDefinition> copy =
                new LinkedHashMap<DeferredKey, DeferredDefinition>();
        for (DeferredKey key : keys) {
            DeferredKey checkedKey = Objects.requireNonNull(
                    key,
                    "deferred contains null key");
            DeferredDefinition definition = checked.find(checkedKey)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Deferred registry key has no definition: "
                                    + checkedKey));
            if (!checkedKey.equals(definition.key())) {
                throw new IllegalArgumentException(
                        "Deferred registry identity mismatch: map key="
                                + checkedKey
                                + ", definition key="
                                + definition.key());
            }
            if (copy.put(checkedKey, definition) != null) {
                throw new IllegalArgumentException(
                        "deferred contains duplicate key: " + checkedKey);
            }
        }
        requireStableSnapshot(
                "deferred",
                declaredSize,
                copy.size(),
                checked.size());
        return new ImmutableDeferredRegistry(copy);
    }

    /** Registry size 必须是非负稳定事实。 */
    private static int requireNonNegativeSize(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " size must be >= 0");
        }
        return value;
    }

    /** keys 枚举数必须与阶段开始时声明的 size 完全一致。 */
    private static void requireCompleteKeyEnumeration(
            String name,
            int declaredSize,
            int keyCount) {
        if (declaredSize != keyCount) {
            throw new IllegalArgumentException(
                    name
                            + " keys/size mismatch: size="
                            + declaredSize
                            + ", keys="
                            + keyCount);
        }
    }

    /** 复制结果和阶段结束 size 必须与阶段开始事实一致。 */
    private static void requireStableSnapshot(
            String name,
            int declaredSize,
            int copiedSize,
            int finalSize) {
        if (declaredSize != copiedSize || declaredSize != finalSize) {
            throw new IllegalArgumentException(
                    name
                            + " changed during snapshot: declared="
                            + declaredSize
                            + ", copied="
                            + copiedSize
                            + ", final="
                            + finalSize);
        }
    }

    /** 正式 provenance 边界只接受固定 64 位小写 SHA-256。 */
    private static DigestPair requireDigestPair(DigestPair value) {
        DigestPair checked = Objects.requireNonNull(value, "digestPair");
        requireSha256(checked.sourceDigest(), "sourceDigest");
        requireSha256(checked.semanticDigest(), "semanticDigest");
        return checked;
    }

    /** 校验单个摘要文本格式。 */
    private static String requireSha256(String value, String name) {
        String checked = Objects.requireNonNull(value, name);
        if (!SHA_256.matcher(checked).matches()) {
            throw new IllegalArgumentException(
                    name + " must be 64 lowercase SHA-256 hex characters");
        }
        return checked;
    }

    /** 规范化必填文本。 */
    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return checked;
    }
}
