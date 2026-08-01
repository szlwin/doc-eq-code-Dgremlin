package dec.core.context.model;

import java.util.Objects;

/**
 * Directory 在 BusinessScope 内的强类型身份。
 */
public final class DirectoryKey extends AbstractDefinitionKey {
    private final BusinessScopeKey owner;
    private final String name;

    /**
     * 使用业务范围和目录名称构造完整身份。
     *
     * @param owner Directory 所属的 BusinessScope
     * @param name Directory 在所属 BusinessScope 内的名称
     */
    public DirectoryKey(BusinessScopeKey owner, String name) {
        super("directory:" + Objects.requireNonNull(owner, "owner").canonical()
                + ":" + requireText(name, "name"));
        this.owner = owner;
        this.name = requireText(name, "name");
    }

    /**
     * 返回 Directory 所属的 BusinessScope。
     */
    public BusinessScopeKey owner() {
        return owner;
    }

    /**
     * 返回 Directory 在所属 BusinessScope 内的名称。
     */
    public String name() {
        return name;
    }
}
