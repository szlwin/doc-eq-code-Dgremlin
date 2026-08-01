package dec.core.context.model;

import java.util.Objects;

public final class ActionKey extends AbstractDefinitionKey {
    private final DirectoryKey owner;
    private final String actionName;

    public ActionKey(DirectoryKey owner, String actionName) {
        super("action:" + Objects.requireNonNull(owner, "owner").canonical()
                + ":" + requireText(actionName, "actionName"));
        this.owner = owner;
        this.actionName = requireText(actionName, "actionName");
    }

    public DirectoryKey owner() { return owner; }
    public String actionName() { return actionName; }
}
