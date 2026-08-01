package dec.core.context.model;

public final class ViewKey extends AbstractDefinitionKey {
    private final String name;

    public ViewKey(String name) {
        super("view:" + requireText(name, "name"));
        this.name = requireText(name, "name");
    }

    public String name() {
        return name;
    }
}
