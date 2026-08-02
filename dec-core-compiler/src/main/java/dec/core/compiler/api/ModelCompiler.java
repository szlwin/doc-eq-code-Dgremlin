package dec.core.compiler.api;

/**
 * 协调单次隔离编译及其条件 Context 发布。
 *
 * <p>公共 API 不提供 compile-only 成功路径。后续任务可以在模块内部增加编译 Pass，
 * 但所有外部调用方必须经过同一发布状态边界。</p>
 */
public interface ModelCompiler {
    /**
     * 编译请求指定的根源，并在同一次调用中发布候选 Context。
     *
     * @param request 不可变的源、选项、取消与截止时间输入
     * @param publicationRequest 当前状态预期与发布器边界
     * @return 成功发布结果或失败结果
     */
    CompilationResult compileAndPublish(
            CompilationRequest request,
            PublicationRequest publicationRequest);
}
