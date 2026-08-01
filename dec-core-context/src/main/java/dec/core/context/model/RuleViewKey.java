package dec.core.context.model;

import java.util.Objects;

public final class RuleViewKey extends AbstractDefinitionKey {
    private final SystemKey owner;
    private final String name;

    public RuleViewKey(SystemKey owner, String name) {
        super("rule-view:" + Objects.requireNonNull(owner, "owner").canonical()
                + ":" + requireText(name, "name"));
        this.owner = owner;
        this.name = requireText(name, "name");
    }

    public SystemKey owner() { return owner; }
    public String name() { return name; }
}
