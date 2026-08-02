package dec.core.compiler.source;

/**
 * Provider 解析后交给 Frontend 的不可变文档源视图。
 */
public interface DocumentSource {
    /**
     * 返回用于排序、去重和诊断的稳定 Source 身份。
     */
    String sourceId();

    /**
     * 返回文档内容的防御性字节副本。
     */
    byte[] content();

    /**
     * 返回文档内容的稳定摘要。
     */
    String contentDigest();
}
