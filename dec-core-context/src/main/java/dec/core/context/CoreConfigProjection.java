package dec.core.context;

import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 从同一个 CompiledModelSet 确定性派生的旧 Core 配置只读投影。
 */
public final class CoreConfigProjection {
    private final CompiledModelSet sourceModelSet;
    private final List<CompiledDefinition> data;
    private final List<CompiledDefinition> views;
    private final List<CompiledDefinition> rules;

    private CoreConfigProjection(
            CompiledModelSet sourceModelSet,
            List<CompiledDefinition> data,
            List<CompiledDefinition> views,
            List<CompiledDefinition> rules) {
        this.sourceModelSet = Objects.requireNonNull(sourceModelSet, "sourceModelSet");
        this.data = Objects.requireNonNull(data, "data");
        this.views = Objects.requireNonNull(views, "views");
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    /**
     * 从模型中的 Typed Registry 确定性生成 Projection。
     * 调用方无法传入独立列表，因此 Projection 不会成为第二事实源。
     *
     * @param modelSet Projection 唯一来源模型
     * @return 与该模型同源的只读 Projection
     */
    public static CoreConfigProjection from(CompiledModelSet modelSet) {
        CompiledModelSet source = Objects.requireNonNull(modelSet, "modelSet");
        return new CoreConfigProjection(
                source,
                immutableValues(source.typedRegistries().data()),
                immutableValues(source.typedRegistries().views()),
                immutableValues(source.typedRegistries().ruleViews()));
    }

    /** 返回生成当前 Projection 的唯一模型来源。 */
    public CompiledModelSet sourceModelSet() {
        return sourceModelSet;
    }

    /** 返回 Data 定义只读投影。 */
    public List<CompiledDefinition> data() {
        return data;
    }

    /** 返回 View 定义只读投影。 */
    public List<CompiledDefinition> views() {
        return views;
    }

    /** 返回 RuleView 定义只读投影。 */
    public List<CompiledDefinition> rules() {
        return rules;
    }

    private static <K extends DefinitionKey> List<CompiledDefinition> immutableValues(
            Registry<K, CompiledDefinition> registry) {
        Objects.requireNonNull(registry, "registry");
        List<CompiledDefinition> values =
                new ArrayList<CompiledDefinition>(registry.size());
        // Registry 的 key 已稳定排序，按 key 顺序读取即可得到确定性 Projection。
        for (K key : registry.keys()) {
            values.add(Objects.requireNonNull(
                    registry.require(key),
                    "registry contains null definition"));
        }
        return Collections.unmodifiableList(values);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CoreConfigProjection)) {
            return false;
        }
        CoreConfigProjection that = (CoreConfigProjection) other;
        return sourceModelSet.equals(that.sourceModelSet)
                && data.equals(that.data)
                && views.equals(that.views)
                && rules.equals(that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceModelSet, data, views, rules);
    }

    @Override
    public String toString() {
        return "CoreConfigProjection{"
                + "data=" + data.size()
                + ", views=" + views.size()
                + ", rules=" + rules.size()
                + '}';
    }
}
