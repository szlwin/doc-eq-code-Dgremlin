package dec.core.context.model;

import java.util.Objects;

public final class DeferredKey extends AbstractDefinitionKey {
    private final DefinitionKey owner;
    private final DeferredKind kind;
    private final int ordinal;

    public DeferredKey(DefinitionKey owner, DeferredKind kind, int ordinal) {
        super("deferred:" + Objects.requireNonNull(owner, "owner").canonical()
                + ":" + Objects.requireNonNull(kind, "kind").name()
                + ":" + requireOrdinal(ordinal));
        this.owner = owner;
        this.kind = kind;
        this.ordinal = ordinal;
    }

    public DefinitionKey owner() { return owner; }
    public DeferredKind kind() { return kind; }
    public int ordinal() { return ordinal; }

    private static int requireOrdinal(int value) {
        if (value < 0) throw new IllegalArgumentException("ordinal must be >= 0");
        return value;
    }
}
