package dec.core.context.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
        this.dataSources = Objects.requireNonNull(dataSources, "dataSources");
        this.connections = Objects.requireNonNull(connections, "connections");
        this.data = Objects.requireNonNull(data, "data");
        this.views = Objects.requireNonNull(views, "views");
        this.systems = Objects.requireNonNull(systems, "systems");
        this.ruleViews = Objects.requireNonNull(ruleViews, "ruleViews");
        this.businessScopes = Objects.requireNonNull(businessScopes, "businessScopes");
        this.information = Objects.requireNonNull(information, "information");
        this.directories = Objects.requireNonNull(directories, "directories");
        this.actions = Objects.requireNonNull(actions, "actions");
        this.produces = Objects.requireNonNull(produces, "produces");
    }

    /**
     * 从已经验证的完整 Registry 确定性派生全部 Typed Registry。
     * 未知 Key 类型必须立即失败，禁止形成未分类的发布事实。
     */
    static TypedDefinitionRegistries from(
            Registry<DefinitionKey, CompiledDefinition> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        Map<DataSourceKey, CompiledDefinition> dataSources =
                new LinkedHashMap<DataSourceKey, CompiledDefinition>();
        Map<ConnectionKey, CompiledDefinition> connections =
                new LinkedHashMap<ConnectionKey, CompiledDefinition>();
        Map<DataKey, CompiledDefinition> data =
                new LinkedHashMap<DataKey, CompiledDefinition>();
        Map<ViewKey, CompiledDefinition> views =
                new LinkedHashMap<ViewKey, CompiledDefinition>();
        Map<SystemKey, CompiledDefinition> systems =
                new LinkedHashMap<SystemKey, CompiledDefinition>();
        Map<RuleViewKey, CompiledDefinition> ruleViews =
                new LinkedHashMap<RuleViewKey, CompiledDefinition>();
        Map<BusinessScopeKey, CompiledDefinition> businessScopes =
                new LinkedHashMap<BusinessScopeKey, CompiledDefinition>();
        Map<InformationKey, CompiledDefinition> information =
                new LinkedHashMap<InformationKey, CompiledDefinition>();
        Map<DirectoryKey, CompiledDefinition> directories =
                new LinkedHashMap<DirectoryKey, CompiledDefinition>();
        Map<ActionKey, CompiledDefinition> actions =
                new LinkedHashMap<ActionKey, CompiledDefinition>();
        Map<ProduceKey, CompiledDefinition> produces =
                new LinkedHashMap<ProduceKey, CompiledDefinition>();

        for (DefinitionKey key : definitions.keys()) {
            DefinitionKey nonNullKey = Objects.requireNonNull(key, "definitions contains null key");
            CompiledDefinition definition = Objects.requireNonNull(
                    definitions.require(nonNullKey),
                    "definitions contains null value");
            if (!nonNullKey.equals(definition.key())) {
                throw new IllegalArgumentException(
                        "Typed registry identity mismatch: map key="
                                + nonNullKey
                                + ", definition key="
                                + definition.key());
            }
            // 通过显式类型分派冻结发布 Registry，避免字符串类型码和未经审查的扩展。
            if (nonNullKey instanceof DataSourceKey) {
                dataSources.put((DataSourceKey) nonNullKey, definition);
            } else if (nonNullKey instanceof ConnectionKey) {
                connections.put((ConnectionKey) nonNullKey, definition);
            } else if (nonNullKey instanceof DataKey) {
                data.put((DataKey) nonNullKey, definition);
            } else if (nonNullKey instanceof ViewKey) {
                views.put((ViewKey) nonNullKey, definition);
            } else if (nonNullKey instanceof SystemKey) {
                systems.put((SystemKey) nonNullKey, definition);
            } else if (nonNullKey instanceof RuleViewKey) {
                ruleViews.put((RuleViewKey) nonNullKey, definition);
            } else if (nonNullKey instanceof BusinessScopeKey) {
                businessScopes.put((BusinessScopeKey) nonNullKey, definition);
            } else if (nonNullKey instanceof InformationKey) {
                information.put((InformationKey) nonNullKey, definition);
            } else if (nonNullKey instanceof DirectoryKey) {
                directories.put((DirectoryKey) nonNullKey, definition);
            } else if (nonNullKey instanceof ActionKey) {
                actions.put((ActionKey) nonNullKey, definition);
            } else if (nonNullKey instanceof ProduceKey) {
                produces.put((ProduceKey) nonNullKey, definition);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported published DefinitionKey type: "
                                + nonNullKey.getClass().getName());
            }
        }

        return new TypedDefinitionRegistries(
                new ImmutableRegistry<DataSourceKey, CompiledDefinition>(dataSources),
                new ImmutableRegistry<ConnectionKey, CompiledDefinition>(connections),
                new ImmutableRegistry<DataKey, CompiledDefinition>(data),
                new ImmutableRegistry<ViewKey, CompiledDefinition>(views),
                new ImmutableRegistry<SystemKey, CompiledDefinition>(systems),
                new ImmutableRegistry<RuleViewKey, CompiledDefinition>(ruleViews),
                new ImmutableRegistry<BusinessScopeKey, CompiledDefinition>(businessScopes),
                new ImmutableRegistry<InformationKey, CompiledDefinition>(information),
                new ImmutableRegistry<DirectoryKey, CompiledDefinition>(directories),
                new ImmutableRegistry<ActionKey, CompiledDefinition>(actions),
                new ImmutableRegistry<ProduceKey, CompiledDefinition>(produces));
    }

    /** 返回 DataSource 类型 Registry。 */
    public Registry<DataSourceKey, CompiledDefinition> dataSources() {
        return dataSources;
    }

    /** 返回 Connection 类型 Registry。 */
    public Registry<ConnectionKey, CompiledDefinition> connections() {
        return connections;
    }

    /** 返回 Data 类型 Registry。 */
    public Registry<DataKey, CompiledDefinition> data() {
        return data;
    }

    /** 返回 View 类型 Registry。 */
    public Registry<ViewKey, CompiledDefinition> views() {
        return views;
    }

    /** 返回 System 类型 Registry。 */
    public Registry<SystemKey, CompiledDefinition> systems() {
        return systems;
    }

    /** 返回 RuleView 类型 Registry。 */
    public Registry<RuleViewKey, CompiledDefinition> ruleViews() {
        return ruleViews;
    }

    /** 返回 BusinessScope 类型 Registry。 */
    public Registry<BusinessScopeKey, CompiledDefinition> businessScopes() {
        return businessScopes;
    }

    /** 返回 Information 类型 Registry。 */
    public Registry<InformationKey, CompiledDefinition> information() {
        return information;
    }

    /** 返回 Directory 类型 Registry。 */
    public Registry<DirectoryKey, CompiledDefinition> directories() {
        return directories;
    }

    /** 返回 Action 类型 Registry。 */
    public Registry<ActionKey, CompiledDefinition> actions() {
        return actions;
    }

    /** 返回 Produce 类型 Registry。 */
    public Registry<ProduceKey, CompiledDefinition> produces() {
        return produces;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TypedDefinitionRegistries)) {
            return false;
        }
        TypedDefinitionRegistries that = (TypedDefinitionRegistries) other;
        return dataSources.equals(that.dataSources)
                && connections.equals(that.connections)
                && data.equals(that.data)
                && views.equals(that.views)
                && systems.equals(that.systems)
                && ruleViews.equals(that.ruleViews)
                && businessScopes.equals(that.businessScopes)
                && information.equals(that.information)
                && directories.equals(that.directories)
                && actions.equals(that.actions)
                && produces.equals(that.produces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                dataSources,
                connections,
                data,
                views,
                systems,
                ruleViews,
                businessScopes,
                information,
                directories,
                actions,
                produces);
    }

    @Override
    public String toString() {
        return "TypedDefinitionRegistries{"
                + "dataSources=" + dataSources.size()
                + ", connections=" + connections.size()
                + ", data=" + data.size()
                + ", views=" + views.size()
                + ", systems=" + systems.size()
                + ", ruleViews=" + ruleViews.size()
                + ", businessScopes=" + businessScopes.size()
                + ", information=" + information.size()
                + ", directories=" + directories.size()
                + ", actions=" + actions.size()
                + ", produces=" + produces.size()
                + '}';
    }
}
