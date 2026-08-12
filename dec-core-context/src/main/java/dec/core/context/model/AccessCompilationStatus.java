package dec.core.context.model;

/**
 * P2 model-access 的编译期分类结果。
 * STATIC_DENY 只用于分类，不允许作为已发布授权规则存在。
 */
public enum AccessCompilationStatus {
    STATIC_DENY,
    STATIC_ALLOW,
    RUNTIME_GUARD_REQUIRED
}
