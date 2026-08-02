package dec.core.compiler.api;

import java.util.Objects;

/**
 * 单次 Compiler 会话的不可变输入。
 */
public final class CompilationRequest {
    private final String rootSourceId;
    private final CompilationOptions options;
    private final CancellationToken cancellationToken;

    /**
     * 冻结根源标识以及调用方可控制的执行边界。
     *
     * @param rootSourceId 根配置源的规范化标识
     * @param options 不可变的 Schema、选项与截止时间
     * @param cancellationToken 会话级协作式取消入口
     */
    public CompilationRequest(
            String rootSourceId,
            CompilationOptions options,
            CancellationToken cancellationToken) {
        this.rootSourceId = ApiContracts.requireText(rootSourceId, "rootSourceId");
        this.options = Objects.requireNonNull(options, "options");
        this.cancellationToken = Objects.requireNonNull(
                cancellationToken,
                "cancellationToken");
    }

    /**
     * 返回规范化根源标识。
     */
    public String rootSourceId() {
        return rootSourceId;
    }

    /**
     * 返回本次会话的不可变选项与截止时间边界。
     */
    public CompilationOptions options() {
        return options;
    }

    /**
     * 返回本次编译请求持有的取消令牌。
     */
    public CancellationToken cancellationToken() {
        return cancellationToken;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CompilationRequest)) {
            return false;
        }
        CompilationRequest that = (CompilationRequest) other;
        return rootSourceId.equals(that.rootSourceId)
                && options.equals(that.options)
                && cancellationToken.equals(that.cancellationToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootSourceId, options, cancellationToken);
    }

    @Override
    public String toString() {
        return "CompilationRequest{"
                + "rootSourceId='" + rootSourceId + '\''
                + ", options=" + options
                + '}';
    }
}
