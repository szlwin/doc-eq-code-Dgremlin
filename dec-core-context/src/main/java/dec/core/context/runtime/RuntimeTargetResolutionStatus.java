package dec.core.context.runtime;

/** Stable target-resolution status algebra frozen by the R29/R30 protected-access contract. */
public enum RuntimeTargetResolutionStatus {
    RESOLVED,
    NOT_FOUND,
    AMBIGUOUS,
    CONTEXT_MISMATCH,
    PROVENANCE_MISMATCH
}
