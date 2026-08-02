package dec.core.compiler.canonical;

import dec.core.compiler.source.DocumentSource;

/**
 * 将安全文档 Source 转换为 Canonical 结果的可替换 Frontend。
 */
public interface DocumentFrontend {
    /**
     * 返回当前 Frontend 唯一支持的文档格式。
     */
    DocumentFormat format();

    /**
     * 使用显式选项解析文档，不读取全局 Schema 或 Parser 状态。
     *
     * @param source Provider 返回的不可变文档源
     * @param options 当前 Session 的 Frontend 选项
     * @return Canonical 成功或稳定 Diagnostic 失败结果
     */
    FrontendResult parse(DocumentSource source, FrontendOptions options);
}
