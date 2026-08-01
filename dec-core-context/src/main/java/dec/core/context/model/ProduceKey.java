package dec.core.context.model;

import java.util.Objects;

public final class ProduceKey extends AbstractDefinitionKey {
    private final ActionKey owner;
    private final int sourceOrdinal;

    public ProduceKey(ActionKey owner, int sourceOrdinal) {
        super("produce:" + Objects.requireNonNull(owner, "owner").canonical()
                + ":" + requireOrdinal(sourceOrdinal));
        this.owner = owner;
        this.sourceOrdinal = sourceOrdinal;
    }

    public ActionKey owner() { return owner; }
    public int sourceOrdinal() { return sourceOrdinal; }

    private static int requireOrdinal(int value) {
        if (value < 0) throw new IllegalArgumentException("sourceOrdinal must be >= 0");
        return value;
    }
}
