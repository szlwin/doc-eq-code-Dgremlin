package dec.core.compiler.canonical;

/**
 * 由调用方注入的可替换 Frontend 注册表。
 */
public interface FrontendRegistry {
    /**
     * 返回指定格式的唯一 Frontend；缺失格式由实现以明确异常或失败合同表达。
     *
     * @param format 非空文档格式
     * @return 与格式精确匹配的 Frontend
     */
    DocumentFrontend require(DocumentFormat format);
}
