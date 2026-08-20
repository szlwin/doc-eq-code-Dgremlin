package dec.core.starter.access;

import java.util.Objects;
import java.util.Optional;

/** Closed factory result: either a composition or a stable failure. */
public final class ProtectedAccessCompositionResult {
    private final ProtectedAccessComposition composition;
    private final ProtectedAccessCompositionFailure failure;

    private ProtectedAccessCompositionResult(
            ProtectedAccessComposition composition,
            ProtectedAccessCompositionFailure failure) {
        this.composition = composition;
        this.failure = failure;
    }

    public static ProtectedAccessCompositionResult created(ProtectedAccessComposition composition) {
        return new ProtectedAccessCompositionResult(
                Objects.requireNonNull(composition, "composition"), null);
    }

    public static ProtectedAccessCompositionResult failed(ProtectedAccessCompositionFailure failure) {
        return new ProtectedAccessCompositionResult(
                null, Objects.requireNonNull(failure, "failure"));
    }

    public boolean created() { return composition != null; }
    public Optional<ProtectedAccessComposition> composition() { return Optional.ofNullable(composition); }
    public Optional<ProtectedAccessCompositionFailure> failure() { return Optional.ofNullable(failure); }
}
