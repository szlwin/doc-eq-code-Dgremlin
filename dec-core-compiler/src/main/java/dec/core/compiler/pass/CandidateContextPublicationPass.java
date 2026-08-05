package dec.core.compiler.pass;

import java.util.Collections;
import java.util.Optional;

/**
 * TASK-P1-T14 第十阶段：从冻结模型输入准备 candidate，不持有 Publisher capability。
 */
public final class CandidateContextPublicationPass
        implements PublicationCompilerPass {
    public static final String INPUT_ARTIFACT =
            "t14.compiled-model-set-input";

    /** 返回固定的第十 Pass 名称。 */
    @Override
    public String name() {
        return CompilerPipeline.PUBLICATION_PASS;
    }

    /**
     * 从 Session 的不可变输入闭包构造 candidate，并只交给 Context 的 prepare 边界。
     */
    @Override
    public PassResult execute(PublicationPassContext context) {
        Optional<CompiledModelSetBuilder.FrozenInput> input = context.artifact(
                INPUT_ARTIFACT,
                CompiledModelSetBuilder.FrozenInput.class);
        if (!input.isPresent()) {
            return PassResult.of(Collections.singletonList(
                    PipelineDiagnostics.publicationBlocked()));
        }

        // Diagnostic 快照在 candidate 构造前读取；任何 ERROR 都由模型边界拒绝。
        context.prepare(input.get().candidate(context.diagnostics()));
        return PassResult.passed();
    }
}
