package dec.core.compiler.pass;

import dec.core.compiler.compiled.DigestBoundCompiledInput;
import dec.core.compiler.modelaccess.ViewMaterializationCompiler;
import dec.core.context.EngineContext;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.CompiledViewMaterializationIndex;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.ModelAccessPolicyIndex;
import java.util.List;
import java.util.Objects;

/**
 * 将 T13 原子绑定的 provenance 输入一次性冻结为 candidate 构造闭包。
 */
public final class CompiledModelSetBuilder {
    private final DigestBoundCompiledInput boundInput;
    private final CompiledViewMaterializationIndex materializationIndex;
    private final ModelAccessPolicyIndex policyIndex;
    private boolean frozen;

    /**
     * 保留 P1 T14 的 atomic provenance 兼容入口；无 P2 policy 时 materialization 同样为空。
     */
    public CompiledModelSetBuilder(DigestBoundCompiledInput boundInput) {
        this(
                boundInput,
                CompiledViewMaterializationIndex.empty(),
                ModelAccessPolicyIndex.empty());
    }

    /**
     * 生产入口：从同一 Session 已编译的 exact policy 冻结 materialization aggregate，禁止运行时重读配置。
     */
    public CompiledModelSetBuilder(
            DigestBoundCompiledInput boundInput,
            ModelAccessPolicyIndex policyIndex) {
        this(
                boundInput,
                new ViewMaterializationCompiler().compile(
                        Objects.requireNonNull(policyIndex, "policyIndex")),
                policyIndex);
    }

    /**
     * 创建原子 provenance 与同 Session runtime aggregates 的显式 Builder，禁止发布期重新推导。
     */
    public CompiledModelSetBuilder(
            DigestBoundCompiledInput boundInput,
            CompiledViewMaterializationIndex materializationIndex,
            ModelAccessPolicyIndex policyIndex) {
        this.boundInput = Objects.requireNonNull(boundInput, "boundInput");
        this.materializationIndex = Objects.requireNonNull(
                materializationIndex,
                "materializationIndex");
        this.policyIndex = Objects.requireNonNull(policyIndex, "policyIndex");
    }

    /** 完成一次性冻结；重复调用稳定拒绝，避免同一 Builder 被跨 Session 复用。 */
    public FrozenInput freeze() {
        if (frozen) {
            throw new IllegalStateException("candidate context builder already frozen");
        }
        frozen = true;
        return new FrozenInput(boundInput, materializationIndex, policyIndex);
    }

    /** 可安全进入 Session artifact 的不可变候选输入闭包。 */
    public static final class FrozenInput implements ImmutablePipelineArtifact {
        private final DigestBoundCompiledInput boundInput;
        private final CompiledViewMaterializationIndex materializationIndex;
        private final ModelAccessPolicyIndex policyIndex;

        /**
         * 保存 T13 摘要闭包与同一 Compilation Session 冻结的 materialization/policy 聚合。
         */
        private FrozenInput(
                DigestBoundCompiledInput boundInput,
                CompiledViewMaterializationIndex materializationIndex,
                ModelAccessPolicyIndex policyIndex) {
            this.boundInput = Objects.requireNonNull(boundInput, "boundInput");
            this.materializationIndex = Objects.requireNonNull(
                    materializationIndex,
                    "materializationIndex");
            this.policyIndex = Objects.requireNonNull(policyIndex, "policyIndex");
        }

        /** 校验当前请求的 schema/options 与摘要闭包完全一致。 */
        boolean matchesRequest(String schemaVersion, String optionsDigest) {
            return boundInput.schemaVersion().equals(schemaVersion)
                    && boundInput.optionsDigest().equals(optionsDigest);
        }

        /**
         * 使用当前稳定 Diagnostic 快照构造完整模型和 candidate Context。
         * materializationIndex/policyIndex 必须来自同一 Compilation Session，
         * 禁止 null、全局或发布期重新推导。
         */
        EngineContext candidate(List<Diagnostic> diagnostics) {
            return new EngineContext(new CompiledModelSet(
                    boundInput.sourceManifest(),
                    materializationIndex,
                    policyIndex,
                    boundInput.definitions(),
                    boundInput.deferred(),
                    Objects.requireNonNull(diagnostics, "diagnostics"),
                    boundInput.digestPair(),
                    boundInput.compilerVersion(),
                    boundInput.schemaVersion(),
                    boundInput.optionsDigest()));
        }
    }
}
