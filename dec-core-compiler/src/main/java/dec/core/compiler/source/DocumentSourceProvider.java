package dec.core.compiler.source;

/**
 * 由调用方注入的文档 Source 解析边界。
 */
public interface DocumentSourceProvider {
    /**
     * 解析恰好一个文档 Source。
     *
     * @param reference 待解析的 Source 引用
     * @param context 当前 CompilationSession 的解析上下文
     * @return 不以 null 或预期异常表达业务失败的稳定结果
     */
    SourceResolutionResult resolve(
            SourceReference reference,
            SourceResolutionContext context);

    /**
     * 展开并解析文件集合，结果由 Provider 按稳定 sourceId 顺序返回。
     *
     * @param reference 待展开的 Source 引用
     * @param context 当前 CompilationSession 的解析上下文
     * @return 不以 null 或预期异常表达业务失败的稳定结果
     */
    SourceResolutionResult resolveFileSet(
            SourceReference reference,
            SourceResolutionContext context);
}
