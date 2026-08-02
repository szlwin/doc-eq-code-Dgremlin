package dec.core.compiler.api;

/**
 * 为单次编译会话提供协作式取消状态。
 */
public interface CancellationToken {
    /**
     * 返回当前编译是否应在下一个可观察状态转换前停止。
     *
     * @return 已请求取消时返回 {@code true}
     */
    boolean isCancellationRequested();
}
