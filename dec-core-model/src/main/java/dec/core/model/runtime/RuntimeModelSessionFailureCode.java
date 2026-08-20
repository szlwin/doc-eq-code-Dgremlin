package dec.core.model.runtime;

/** DESIGN-P2-R30 保留的 Session 封闭失败代数。 */
public enum RuntimeModelSessionFailureCode {
    SCOPE_INACTIVE,
    SESSION_CLOSED,
    SESSION_ALREADY_SEALED,
    DUPLICATE_REGISTRATION,
    OWNERSHIP_CONFLICT
}
