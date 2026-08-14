package dec.core.context.runtime;

import java.util.Objects;
import java.util.Optional;

/** Closed protected-access result algebra: exactly one READ, WRITE or DENY branch. */
public final class ProtectedAccessResult {
    private final ProtectedReadValue readValue;
    private final ProtectedWriteReceipt writeReceipt;
    private final ProtectedAccessDenial denial;

    private ProtectedAccessResult(
            ProtectedReadValue readValue,
            ProtectedWriteReceipt writeReceipt,
            ProtectedAccessDenial denial) {
        int branches = (readValue != null ? 1 : 0)
                + (writeReceipt != null ? 1 : 0)
                + (denial != null ? 1 : 0);
        if (branches != 1) {
            throw new IllegalArgumentException("exactly one result branch is required");
        }
        this.readValue = readValue;
        this.writeReceipt = writeReceipt;
        this.denial = denial;
    }

    public static ProtectedAccessResult allowRead(ProtectedReadValue value) {
        return new ProtectedAccessResult(Objects.requireNonNull(value, "value"), null, null);
    }

    public static ProtectedAccessResult allowWrite(ProtectedWriteReceipt receipt) {
        return new ProtectedAccessResult(null, Objects.requireNonNull(receipt, "receipt"), null);
    }

    public static ProtectedAccessResult deny(ProtectedAccessDenial denial) {
        return new ProtectedAccessResult(null, null, Objects.requireNonNull(denial, "denial"));
    }

    public boolean allowed() { return denial == null; }
    public Optional<ProtectedReadValue> readValue() { return Optional.ofNullable(readValue); }
    public Optional<ProtectedWriteReceipt> writeReceipt() { return Optional.ofNullable(writeReceipt); }
    public Optional<ProtectedAccessDenial> denial() { return Optional.ofNullable(denial); }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProtectedAccessResult)) return false;
        ProtectedAccessResult that = (ProtectedAccessResult) other;
        return Objects.equals(readValue, that.readValue)
                && Objects.equals(writeReceipt, that.writeReceipt)
                && Objects.equals(denial, that.denial);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readValue, writeReceipt, denial);
    }
}
