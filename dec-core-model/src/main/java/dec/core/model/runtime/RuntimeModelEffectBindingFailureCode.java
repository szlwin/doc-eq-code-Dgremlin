package dec.core.model.runtime;

/** DESIGN-P2-R30 保留的 EffectProvider 绑定失败代数。 */
public enum RuntimeModelEffectBindingFailureCode {
    SCOPE_INACTIVE,
    SESSION_NOT_SEALED,
    SESSION_CLOSED,
    SESSION_SCOPE_MISMATCH
}
