package dec.core.compiler.pass;

import dec.core.compiler.compiled.DigestBoundCompiledInput;
import dec.core.context.EngineContext;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.CompiledViewMaterializationIndex;
import dec.core.context.model.Diagnostic;
import java.util.List;
import java.util.Objects;

/**
 * 将 T13 原子绑定的 provenance 输入一次性冻结为 candidate 构造闭包。
 */
public final class CompiledModelSetBuilder {
    private final DigestBoundCompiledInput boundInput;
    private boolean frozen;

    /** 创建只接受原子 provenance 输入的 Builder，禁止分别注入模型事实或摘要。 */
    public CompiledModelSetBuilder(DigestBoundCompiledInput boundInput) {
        this.boundInput = Objects.requireNonNull(boundInput, "boundInput");
    }

    /** 完成一次性冻结；重复调用稳定拒绝，避免同一 Builder 被跨 Session 复用。 */
    public FrozenInput freeze() {
        if (frozen) {
            throw new IllegalStateException("candidate context builder already frozen");
        }
        frozen = true;
        return new FrozenInput(boundInput);
    }

    /** 可安全进入 Session artifact 的不可变候选输入闭包。 */
    public static final class FrozenInput implements ImmutablePipelineArtifact {
        private final DigestBoundCompiledInput boundInput;

        /** 保存已经由 T13 摘要服务原子绑定的不可变事实。 */
        private FrozenInput(DigestBoundCompiledInput boundInput) {
            this.boundInput = Objects.requireNonNull(boundInput, "boundInput");
        }

        /** 校验当前请求的 schema/options 与摘要闭包完全一致。 */
        boolean matchesRequest(String schemaVersion, String optionsDigest) {
            return boundInput.schemaVersion().equals(schemaVersion)
                    && boundInput.optionsDigest().equals(optionsDigest);
        }

        /**
         * 使用当前稳定 Diagnostic 快照构造完整模型和 candidate Context。
         * DEV-04 只负责 mandatory aggregate construction adaptation；具体 descriptor 生成归 DEV-03。
         */
        EngineContext candidate(List<Diagnostic> diagnostics) {
            return new EngineContext(new CompiledModelSet(
                    boundInput.sourceManifest(),
                    CompiledViewMaterializationIndex.empty(),
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
