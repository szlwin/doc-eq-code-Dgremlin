package dec.core.compiler.pass;

/**
 * 第十阶段专用的 Publication Pass 合同。
 *
 * <p>只有该接口可以接收 PublicationPassContext；普通 PassContext 不携带发布能力。</p>
 */
public interface PublicationCompilerPass extends CompilerPass {
    /**
     * 在最终发布专用 Context 中执行提交阶段。
     *
     * @param context 仅第十阶段可获得的发布 Context
     * @return 发布阶段的不可变 Diagnostic 结果
     */
    PassResult execute(PublicationPassContext context);

    /**
     * 禁止 Pipeline 或调用方把 Publication Pass 当作普通 Pass 执行。
     */
    @Override
    default PassResult execute(PassContext context) {
        throw new IllegalStateException(
                "PublicationCompilerPass requires PublicationPassContext");
    }
}
