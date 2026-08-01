package dec.core.context.model;

/**
 * 已发布模型中按 DefinitionKey 类型拆分的只读 Registry 集合。
 */
public final class TypedDefinitionRegistries {
    private final ImmutableRegistry<DataSourceKey, CompiledDefinition> dataSources;
    private final ImmutableRegistry<ConnectionKey, CompiledDefinition> connections;
    private final ImmutableRegistry<DataKey, CompiledDefinition> data;
    private final ImmutableRegistry<ViewKey, CompiledDefinition> views;
    private final ImmutableRegistry<SystemKey, CompiledDefinition> systems;
    private final ImmutableRegistry<RuleViewKey, CompiledDefinition> ruleViews;
    private final ImmutableRegistry<BusinessScopeKey, CompiledDefinition> businessScopes;
    private final ImmutableRegistry<InformationKey, CompiledDefinition> information;
    private final ImmutableRegistry<DirectoryKey, CompiledDefinition> directories;
    private final ImmutableRegistry<ActionKey, CompiledDefinition> actions;
    private final ImmutableRegistry<ProduceKey, CompiledDefinition> produces;

    private TypedDefinitionRegistries(
            ImmutableRegistry<DataSourceKey, CompiledDefinition> dataSources,
            ImmutableRegistry<ConnectionKey, CompiledDefinition> connections,
            ImmutableRegistry<DataKey, CompiledDefinition> data,
            ImmutableRegistry<ViewKey, CompiledDefinition> views,
            ImmutableRegistry<SystemKey, CompiledDefinition> systems,
            ImmutableRegistry<RuleViewKey, CompiledDefinition> ruleViews,
            ImmutableRegistry<BusinessScopeKey, CompiledDefinition> businessScopes,
            ImmutableRegistry<InformationKey, CompiledDefinition> information,
            ImmutableRegistry<DirectoryKey, CompiledDefinition> directories,
            ImmutableRegistry<ActionKey, CompiledDefinition> actions,
            ImmutableRegistry<ProduceKey, CompiledDefinition> produces) {
        this.dataSources = dataSources;
        this.connections = connections;
        this.data = data;
        this.views = views;
        this.systems = systems;
        this.ruleViews = ruleViews;
        this.businessScopes = businessScopes;
        this.information = information;
        this.directories = directories;
        this.actions = actions;
        this.produces = produces;
    }

    /**
     * 架构骨架阶段仅冻结派生入口，具体分类和身份校验在 Development 实现。
     */
    static TypedDefinitionRegistries from(
            Registry<DefinitionKey, CompiledDefinition> definitions) {
        throw new UnsupportedOperationException("T01 REWORK architecture skeleton");
    }

    /**
     * 返回 DataSource 类型 Registry。
     */
    public Registry<DataSourceKey, CompiledDefinition> dataSources() {
        return dataSources;
    }

    /**
     * 返回 Connection 类型 Registry。
     */
    public Registry<ConnectionKey, CompiledDefinition> connections() {
        return connections;
    }

    /**
     * 返回 Data 类型 Registry。
     */
    public Registry<DataKey, CompiledDefinition> data() {
        return data;
    }

    /**
     * 返回 View 类型 Registry。
     */
    public Registry<ViewKey, CompiledDefinition> views() {
        return views;
    }

    /**
     * 返回 System 类型 Registry。
     */
    public Registry<SystemKey, CompiledDefinition> systems() {
        return systems;
    }

    /**
     * 返回 RuleView 类型 Registry。
     */
    public Registry<RuleViewKey, CompiledDefinition> ruleViews() {
        return ruleViews;
    }

    /**
     * 返回 BusinessScope 类型 Registry。
     */
    public Registry<BusinessScopeKey, CompiledDefinition> businessScopes() {
        return businessScopes;
    }

    /**
     * 返回 Information 类型 Registry。
     */
    public Registry<InformationKey, CompiledDefinition> information() {
        return information;
    }

    /**
     * 返回 Directory 类型 Registry。
     */
    public Registry<DirectoryKey, CompiledDefinition> directories() {
        return directories;
    }

    /**
     * 返回 Action 类型 Registry。
     */
    public Registry<ActionKey, CompiledDefinition> actions() {
        return actions;
    }

    /**
     * 返回 Produce 类型 Registry。
     */
    public Registry<ProduceKey, CompiledDefinition> produces() {
        return produces;
    }
}
