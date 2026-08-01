package dec.core.context.model;

public final class SystemKey extends AbstractDefinitionKey {
    private final String name;

    public SystemKey(String name) {
        super("system:" + requireText(name, "name"));
        this.name = requireText(name, "name");
    }

    public String name() {
        return name;
    }
}
