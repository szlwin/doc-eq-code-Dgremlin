package dec.core.context.model;

public final class BusinessScopeKey extends AbstractDefinitionKey {
    private final String name;

    public BusinessScopeKey(String name) {
        super("business-scope:" + requireText(name, "name"));
        this.name = requireText(name, "name");
    }

    public String name() {
        return name;
    }
}
