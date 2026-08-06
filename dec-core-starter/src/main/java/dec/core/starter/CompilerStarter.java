package dec.core.starter;

import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationResult;
import dec.core.compiler.api.ModelCompiler;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.api.PublishedCompilationResult;
import dec.core.context.CoreConfigProjection;
import java.util.Objects;

/**
 * 使用实例级 Compiler 完成单次编译发布，并暴露同源只读投影。
 *
 * <p>该入口不保存全局 current Context，也不修改旧 Config Registry。</p>
 */
public final class CompilerStarter {
    private final ModelCompiler compiler;

    /**
     * 创建只依赖公共 Compiler 契约的 Starter。
     *
     * @param compiler 单次编译和发布实现
     */
    public CompilerStarter(ModelCompiler compiler) {
        this.compiler = Objects.requireNonNull(compiler, "compiler");
    }

    /**
     * 将不可变请求和发布边界原样委托给同一个 Compiler。
     *
     * @param request 单次编译输入
     * @param publicationRequest 条件发布输入
     * @return Compiler 返回的同一个结果实例
     */
    public CompilationResult compileAndPublish(
            CompilationRequest request,
            PublicationRequest publicationRequest) {
        return compiler.compileAndPublish(
                Objects.requireNonNull(request, "request"),
                Objects.requireNonNull(publicationRequest, "publicationRequest"));
    }

    /**
     * 从已发布结果持有的同一个 EngineContext 获取旧核心只读投影。
     *
     * @param result 编译发布结果
     * @return 与已发布模型同源的只读投影
     */
    public CoreConfigProjection projection(CompilationResult result) {
        CompilationResult checked = Objects.requireNonNull(result, "result");
        if (!(checked instanceof PublishedCompilationResult)) {
            throw new IllegalStateException(
                    "projection requires a published compilation result");
        }
        PublishedCompilationResult published = (PublishedCompilationResult) checked;
        return published.engineContext().projection();
    }
}
