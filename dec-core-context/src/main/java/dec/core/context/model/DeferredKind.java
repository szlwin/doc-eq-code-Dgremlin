package dec.core.context.model;

/** Compiler 尚未执行、需要由后续阶段负责的语义类型。 */
public enum DeferredKind {
    /** 简化运行模型不再延迟 System 权限；仅保留枚举值兼容旧调用方。 */
    @Deprecated
    SYSTEM_PERMISSION,
    /** model-access 已在编译期完成校验；仅保留枚举值兼容旧调用方。 */
    @Deprecated
    MODEL_ACCESS,
    INFORMATION,
    ACTION,
    PRODUCE,
    DIRECTORY,
    QUERY,
    TRANSACTION
}
