package dec.core.starter.access;

/** Stable composition failure algebra; no branch exposes a usable operation port. */
public enum ProtectedAccessCompositionFailureCode {
    SCOPE_INACTIVE,
    SCOPE_STALE,
    PROVENANCE_MISMATCH,
    SESSION_DUPLICATE_REGISTRATION,
    SESSION_OWNERSHIP_CONFLICT,
    SESSION_ALREADY_SEALED,
    SESSION_CLOSED,
    EFFECT_SESSION_NOT_SEALED,
    EFFECT_SESSION_CLOSED,
    EFFECT_SESSION_SCOPE_MISMATCH
}
