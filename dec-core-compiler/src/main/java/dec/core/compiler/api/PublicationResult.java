package dec.core.compiler.api;

/**
 * ContextPublisher 返回的稳定结果合同。
 */
public interface PublicationResult {
    /**
     * 返回发布成功或 compare-and-set 冲突状态。
     */
    PublicationStatus status();
}
