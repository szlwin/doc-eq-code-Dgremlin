package dec.core.context.model;

/**
 * P2 model-access 的编译期分类结果。
 * STATIC_DENY 只用于分类，不允许作为已发布授权规则存在。
 */
public enum AccessCompilationStatus {
    STATIC_DENY,
    STATIC_ALLOW,
    /** 简化运行模型不再生成此状态，仅保留枚举值兼容旧调用方。 */
    @Deprecated
    RUNTIME_GUARD_REQUIRED
}
