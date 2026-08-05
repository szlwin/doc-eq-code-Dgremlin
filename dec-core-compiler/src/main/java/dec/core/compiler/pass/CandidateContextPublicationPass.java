package dec.core.compiler.pass;

import java.util.Collections;
import java.util.Optional;

/**
 * TASK-P1-T14 第十阶段架构骨架：只准备 candidate，不持有 Publisher capability。
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

    /** 从冻结输入构造并准备 candidate Context。 */
    @Override
    public PassResult execute(PublicationPassContext context) {
        Optional<CompiledModelSetBuilder.FrozenInput> input = context.artifact(
                INPUT_ARTIFACT,
                CompiledModelSetBuilder.FrozenInput.class);
        if (!input.isPresent()) {
            return PassResult.of(Collections.singletonList(
                    PipelineDiagnostics.publicationBlocked()));
        }
        context.prepare(input.get().candidate(Collections.emptyList()));
        return PassResult.passed();
    }
}
