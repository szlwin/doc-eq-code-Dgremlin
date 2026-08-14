package dec.core.starter.access;

import java.util.Objects;

public final class ProtectedAccessCompositionFailure {
    private final ProtectedAccessCompositionFailureCode code;

    private ProtectedAccessCompositionFailure(ProtectedAccessCompositionFailureCode code) {
        this.code = Objects.requireNonNull(code, "code");
    }

    public static ProtectedAccessCompositionFailure of(ProtectedAccessCompositionFailureCode code) {
        return new ProtectedAccessCompositionFailure(code);
    }

    public ProtectedAccessCompositionFailureCode code() {
        return code;
    }
}
