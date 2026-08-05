package dec.core.compiler.pass;

import dec.core.context.EngineContext;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DigestPair;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.Registry;
import java.util.List;

/**
 * TASK-P1-T14 架构骨架：按固定阶段冻结 candidate Context 的发布输入。
 */
public final class CompiledModelSetBuilder {

    /** 创建 Builder，并冻结三个发布版本域。 */
    public CompiledModelSetBuilder(
            String compilerVersion,
            String schemaVersion,
            String optionsVersion) {
        // RED 阶段只冻结公开形状，正式阶段补齐状态机与快照实现。
    }

    /** 冻结 SourceManifest 阶段。 */
    public CompiledModelSetBuilder sourceManifest(
            PublishedSourceManifest value) {
        throw notImplemented();
    }

    /** 冻结 Definition Registry 阶段。 */
    public CompiledModelSetBuilder definitions(
            Registry<DefinitionKey, CompiledDefinition> value) {
        throw notImplemented();
    }

    /** 冻结 Deferred Registry 阶段。 */
    public CompiledModelSetBuilder deferred(DeferredRegistry value) {
        throw notImplemented();
    }

    /** 冻结 DigestPair 阶段。 */
    public CompiledModelSetBuilder digestPair(DigestPair value) {
        throw notImplemented();
    }

    /** 完成一次性输入闭包。 */
    public FrozenInput freeze() {
        throw notImplemented();
    }

    /** 返回稳定的 RED 边界异常。 */
    private static UnsupportedOperationException notImplemented() {
        return new UnsupportedOperationException(
                "TASK-P1-T14 candidate context builder is not implemented");
    }

    /**
     * 可安全进入 Session artifact 的不可变候选输入闭包。
     */
    public static final class FrozenInput implements ImmutablePipelineArtifact {
        private FrozenInput() {
        }

        /** 使用当前无 ERROR Diagnostic 构造完整 candidate Context。 */
        EngineContext candidate(List<Diagnostic> diagnostics) {
            throw notImplemented();
        }
    }
}
