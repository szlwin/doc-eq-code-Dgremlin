package dec.core.context.model;

public final class DataSourceKey extends AbstractDefinitionKey {
    private final String name;

    public DataSourceKey(String name) {
        super("data-source:" + requireText(name, "name"));
        this.name = requireText(name, "name");
    }

    public String name() {
        return name;
    }
}
