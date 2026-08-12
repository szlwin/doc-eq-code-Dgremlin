package dec.core.compiler.pass;

import dec.core.compiler.compiled.DigestBoundCompiledInput;
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
    private final ModelAccessPolicyIndex policyIndex;
    private boolean frozen;

    /**
     * 保留 P1 T14 的 atomic provenance 兼容入口；生产 pipeline 必须使用显式 policy 构造器。
     */
    public CompiledModelSetBuilder(DigestBoundCompiledInput boundInput) {
        this(boundInput, ModelAccessPolicyIndex.empty());
    }

    /** 创建原子 provenance 与同 Session policy 的生产 Builder，禁止发布期重新推导策略。 */
    public CompiledModelSetBuilder(
            DigestBoundCompiledInput boundInput,
            ModelAccessPolicyIndex policyIndex) {
        this.boundInput = Objects.requireNonNull(boundInput, "boundInput");
        this.policyIndex = Objects.requireNonNull(policyIndex, "policyIndex");
    }

    /** 完成一次性冻结；重复调用稳定拒绝，避免同一 Builder 被跨 Session 复用。 */
    public FrozenInput freeze() {
        if (frozen) {
            throw new IllegalStateException("candidate context builder already frozen");
        }
        frozen = true;
        return new FrozenInput(boundInput, policyIndex);
    }

    /** 可安全进入 Session artifact 的不可变候选输入闭包。 */
    public static final class FrozenInput implements ImmutablePipelineArtifact {
        private final DigestBoundCompiledInput boundInput;
        private final ModelAccessPolicyIndex policyIndex;

        /** 保存 T13 摘要闭包与同一 Compilation Session 编译出的 exact policy。 */
        private FrozenInput(
                DigestBoundCompiledInput boundInput,
                ModelAccessPolicyIndex policyIndex) {
            this.boundInput = Objects.requireNonNull(boundInput, "boundInput");
            this.policyIndex = Objects.requireNonNull(policyIndex, "policyIndex");
        }

        /** 校验当前请求的 schema/options 与摘要闭包完全一致。 */
        boolean matchesRequest(String schemaVersion, String optionsDigest) {
            return boundInput.schemaVersion().equals(schemaVersion)
                    && boundInput.optionsDigest().equals(optionsDigest);
        }

        /**
         * 使用当前稳定 Diagnostic 快照构造完整模型和 candidate Context。
         * policyIndex 必须来自同一 Compilation Session，禁止 null、全局或发布期重新推导。
         */
        EngineContext candidate(List<Diagnostic> diagnostics) {
            return new EngineContext(new CompiledModelSet(
                    boundInput.sourceManifest(),
                    CompiledViewMaterializationIndex.empty(),
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
