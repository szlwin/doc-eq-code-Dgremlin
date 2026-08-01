package dec.core.context.model;

import java.util.Objects;

public final class InformationKey extends AbstractDefinitionKey {
    private final SystemKey owner;
    private final String name;

    public InformationKey(SystemKey owner, String name) {
        super(Objects.requireNonNull(owner, "owner").canonical() + "." + requireText(name, "name"));
        this.owner = owner;
        this.name = requireText(name, "name");
    }

    public SystemKey owner() { return owner; }
    public String name() { return name; }
}
