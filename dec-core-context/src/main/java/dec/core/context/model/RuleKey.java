package dec.core.context.model;

import java.util.Objects;

/**
 * P2 Rule 的正式编译身份。
 *
 * <p>Rule 只能在 owning RuleView 内拥有局部名称；禁止把局部 rule name
 * 提升为全局身份，也禁止通过大小写折叠建立别名。</p>
 */
public final class RuleKey extends AbstractDefinitionKey {
    private final RuleViewKey owner;
    private final String localName;

    private RuleKey(RuleViewKey owner, String localName) {
        super("rule:"
                + Objects.requireNonNull(owner, "owner").canonical()
                + ":"
                + requireText(localName, "localName"));
        this.owner = owner;
        this.localName = requireText(localName, "localName");
    }

    /**
     * 使用完整 owning RuleViewKey 与区分大小写的局部名称创建 RuleKey。
     */
    public static RuleKey of(RuleViewKey owner, String localName) {
        return new RuleKey(owner, localName);
    }

    /** 返回 Rule 所属的完整 RuleView 复合身份。 */
    public RuleViewKey owner() {
        return owner;
    }

    /** 返回区分大小写的局部 Rule 名称。 */
    public String localName() {
        return localName;
    }
}
