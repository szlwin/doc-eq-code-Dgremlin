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
}
