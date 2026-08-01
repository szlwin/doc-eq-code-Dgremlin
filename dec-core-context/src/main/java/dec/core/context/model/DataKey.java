package dec.core.context.model;

public final class DataKey extends AbstractDefinitionKey {
    private final String name;

    public DataKey(String name) {
        super("data:" + requireText(name, "name"));
        this.name = requireText(name, "name");
    }

    public String name() {
        return name;
    }
}
