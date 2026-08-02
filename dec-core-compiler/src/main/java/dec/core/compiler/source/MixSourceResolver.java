package dec.core.compiler.source;

import java.util.Objects;

/**
 * 从固定 mix 根入口安全发现 Source，并构建精确声明图。
 */
public final class MixSourceResolver {
    /**
     * 解析根 Source；TDD RED 阶段仅建立可编译入口，不实现业务行为。
     */
    public SourceGraphResolutionResult resolve(
            SourceReference root,
            DocumentSourceProvider provider,
            SourcePolicy policy) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(policy, "policy");
        throw new AssertionError("Architecture skeleton only");
    }
}
