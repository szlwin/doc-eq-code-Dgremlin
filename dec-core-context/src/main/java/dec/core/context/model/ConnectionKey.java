package dec.core.context.model;

public final class ConnectionKey extends AbstractDefinitionKey {
    private final String name;

    public ConnectionKey(String name) {
        super("connection:" + requireText(name, "name"));
        this.name = requireText(name, "name");
    }

    public String name() {
        return name;
    }
}
