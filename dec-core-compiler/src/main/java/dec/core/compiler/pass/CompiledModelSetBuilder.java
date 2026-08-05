package dec.core.compiler.pass;

import dec.core.context.EngineContext;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.Registry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 按固定阶段冻结 candidate EngineContext 所需的完整发布事实。
 *
 * <p>Builder 只能使用一次；Registry 与 Deferred 在各自阶段立即复制，
 * 后续构建不会重新读取调用方提供的可变视图。</p>
 */
public final class CompiledModelSetBuilder {
    private final String compilerVersion;
    private final String schemaVersion;
    private final String optionsVersion;

    private Stage stage = Stage.SOURCE_MANIFEST;
    private PublishedSourceManifest sourceManifest;
    private ImmutableRegistry<DefinitionKey, CompiledDefinition> definitions;
    private ImmutableDeferredRegistry deferred;
    private DigestPair digestPair;

    /** 创建 Builder，并立即冻结三个发布版本域。 */
    public CompiledModelSetBuilder(
            String compilerVersion,
            String schemaVersion,
            String optionsVersion) {
        this.compilerVersion = requireText(compilerVersion, "compilerVersion");
        this.schemaVersion = requireText(schemaVersion, "schemaVersion");
        this.optionsVersion = requireText(optionsVersion, "optionsVersion");
    }

    /** 冻结 SourceManifest 阶段。 */
    public CompiledModelSetBuilder sourceManifest(
            PublishedSourceManifest value) {
        requireStage(Stage.SOURCE_MANIFEST);
        sourceManifest = Objects.requireNonNull(value, "sourceManifest");
        stage = Stage.DEFINITIONS;
        return this;
    }

    /**
     * 冻结 Definition Registry 阶段，并校验外部 Key 与 Definition 内部身份一致。
     */
    public CompiledModelSetBuilder definitions(
            Registry<DefinitionKey, CompiledDefinition> value) {
        requireStage(Stage.DEFINITIONS);
        definitions = snapshotDefinitions(value);
        stage = Stage.DEFERRED;
        return this;
    }

    /**
     * 冻结 Deferred Registry 阶段，并校验 owner、kind、ordinal 的完整身份。
     */
    public CompiledModelSetBuilder deferred(DeferredRegistry value) {
        requireStage(Stage.DEFERRED);
        deferred = snapshotDeferred(value);
        stage = Stage.DIGEST;
        return this;
    }

    /** 冻结与同一输入闭包对应的 DigestPair。 */
    public CompiledModelSetBuilder digestPair(DigestPair value) {
        requireStage(Stage.DIGEST);
        digestPair = Objects.requireNonNull(value, "digestPair");
        stage = Stage.READY;
        return this;
    }

    /**
     * 完成一次性输入闭包；Builder 随后永久封闭，禁止重复 freeze 或追加阶段。
     */
    public FrozenInput freeze() {
        requireStage(Stage.READY);
        stage = Stage.FROZEN;
        return new FrozenInput(
                sourceManifest,
                definitions,
                deferred,
                digestPair,
                compilerVersion,
                schemaVersion,
                optionsVersion);
    }

    /** 在每个入口检查唯一合法阶段，稳定拒绝越序、重复和 build 后复用。 */
    private void requireStage(Stage expected) {
        if (stage != expected) {
            throw new IllegalStateException(
                    "candidate context builder expected "
                            + expected
                            + " but was "
                            + stage);
        }
    }

    /** 对 Definition Registry 执行单次、确定性的防御性复制。 */
    private static ImmutableRegistry<DefinitionKey, CompiledDefinition>
            snapshotDefinitions(
                    Registry<DefinitionKey, CompiledDefinition> source) {
        Registry<DefinitionKey, CompiledDefinition> checked =
                Objects.requireNonNull(source, "definitions");
        Map<DefinitionKey, CompiledDefinition> copy =
                new LinkedHashMap<DefinitionKey, CompiledDefinition>();
        for (DefinitionKey key : checked.keys()) {
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
        return new ImmutableRegistry<DefinitionKey, CompiledDefinition>(copy);
    }

    /** 对 Deferred Registry 执行单次、确定性的防御性复制。 */
    private static ImmutableDeferredRegistry snapshotDeferred(
            DeferredRegistry source) {
        DeferredRegistry checked = Objects.requireNonNull(source, "deferred");
        Map<DeferredKey, DeferredDefinition> copy =
                new LinkedHashMap<DeferredKey, DeferredDefinition>();
        for (DeferredKey key : checked.keys()) {
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
        return new ImmutableDeferredRegistry(copy);
    }

    /** 统一校验并裁剪发布版本文本。 */
    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return checked;
    }

    /** Builder 的唯一成功阶段序列。 */
    private enum Stage {
        SOURCE_MANIFEST,
        DEFINITIONS,
        DEFERRED,
        DIGEST,
        READY,
        FROZEN
    }

    /**
     * 可安全进入 Session artifact 的不可变候选输入闭包。
     */
    public static final class FrozenInput implements ImmutablePipelineArtifact {
        private final PublishedSourceManifest sourceManifest;
        private final ImmutableRegistry<DefinitionKey, CompiledDefinition> definitions;
        private final ImmutableDeferredRegistry deferred;
        private final DigestPair digestPair;
        private final String compilerVersion;
        private final String schemaVersion;
        private final String optionsVersion;

        /** 保存已经完成防御性复制的发布事实，不再接触原始 Registry。 */
        private FrozenInput(
                PublishedSourceManifest sourceManifest,
                ImmutableRegistry<DefinitionKey, CompiledDefinition> definitions,
                ImmutableDeferredRegistry deferred,
                DigestPair digestPair,
                String compilerVersion,
                String schemaVersion,
                String optionsVersion) {
            this.sourceManifest = sourceManifest;
            this.definitions = definitions;
            this.deferred = deferred;
            this.digestPair = digestPair;
            this.compilerVersion = compilerVersion;
            this.schemaVersion = schemaVersion;
            this.optionsVersion = optionsVersion;
        }

        /**
         * 使用当前稳定 Diagnostic 快照构造完整模型和 candidate Context。
         * `CompiledModelSet` 会再次校验 ERROR、Registry 身份及发布事实闭包。
         */
        EngineContext candidate(List<Diagnostic> diagnostics) {
            return new EngineContext(new CompiledModelSet(
                    sourceManifest,
                    definitions,
                    deferred,
                    Objects.requireNonNull(diagnostics, "diagnostics"),
                    digestPair,
                    compilerVersion,
                    schemaVersion,
                    optionsVersion));
        }
    }
}
