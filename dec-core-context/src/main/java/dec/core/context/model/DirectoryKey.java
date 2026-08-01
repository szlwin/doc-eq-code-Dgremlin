package dec.core.context.model;

public final class DirectoryKey extends AbstractDefinitionKey {
    private final String name;

    public DirectoryKey(String name) {
        super("directory:" + requireText(name, "name"));
        this.name = requireText(name, "name");
    }

    public String name() {
        return name;
    }
}
