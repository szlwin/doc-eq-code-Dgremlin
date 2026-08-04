package dec.core.compiler.symbol;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * T07 发布的强类型、稳定有序且只读的符号表。
 */
public final class SymbolTable {
    private final Registry<DefinitionKey, RawDefinition> registry;
    private final List<RawDefinition> definitions;
    private final RawDefinitionSet sourceSnapshot;

    /**
     * 根据完整定义映射和原始输入快照构造不可变 SymbolTable。
     *
     * @param entries TypedKey 到 RawDefinition 的完整映射
     * @param sourceSnapshot 生成当前符号表的完整 RawDefinitionSet
     */
    SymbolTable(
            Map<DefinitionKey, RawDefinition> entries,
            RawDefinitionSet sourceSnapshot) {
        this.registry = new ImmutableRegistry<DefinitionKey, RawDefinition>(
                Objects.requireNonNull(entries, "entries"));
        this.sourceSnapshot = Objects.requireNonNull(
                sourceSnapshot,
                "sourceSnapshot");
        List<RawDefinition> ordered = new ArrayList<RawDefinition>();
        for (DefinitionKey key : registry.keys()) {
            ordered.add(registry.require(key));
        }
        this.definitions = Collections.unmodifiableList(ordered);
    }

    /**
     * 按精确 TypedKey 查询定义。
     */
    public Optional<RawDefinition> find(DefinitionKey key) {
        return registry.find(Objects.requireNonNull(key, "key"));
    }

    /**
     * 按精确 TypedKey 返回定义；不存在时由 Registry 抛出稳定异常。
     */
    public RawDefinition require(DefinitionKey key) {
        return registry.require(Objects.requireNonNull(key, "key"));
    }

    /**
     * 返回按 DefinitionKey 自然顺序冻结的 Key。
     */
    public List<DefinitionKey> keys() {
        return registry.keys();
    }

    /**
     * 返回与 keys 一一对应的不可变 RawDefinition 快照。
     */
    public List<RawDefinition> definitions() {
        return definitions;
    }

    /**
     * 返回构建该符号表的完整不可变 Raw 输入快照，仅供同包阶段绑定校验。
     */
    RawDefinitionSet sourceSnapshot() {
        return sourceSnapshot;
    }

    /**
     * 返回已登记符号数量。
     */
    public int size() {
        return registry.size();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof SymbolTable
                && registry.equals(((SymbolTable) other).registry);
    }

    @Override
    public int hashCode() {
        return registry.hashCode();
    }

    @Override
    public String toString() {
        return "SymbolTable" + registry;
    }
}
