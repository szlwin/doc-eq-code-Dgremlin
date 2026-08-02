package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * 基于 DeferredKey 稳定排序的不可变 Deferred Registry。
 */
public final class ImmutableDeferredRegistry implements DeferredRegistry {
    private final Map<DeferredKey, DeferredDefinition> entries;
    private final List<DeferredKey> keys;

    /**
     * 对输入执行防御性复制，并校验 Map key 与 Definition 完整身份一致。
     *
     * @param entries Deferred 定义映射
     */
    public ImmutableDeferredRegistry(Map<DeferredKey, DeferredDefinition> entries) {
        Objects.requireNonNull(entries, "entries");
        TreeMap<DeferredKey, DeferredDefinition> copy =
                new TreeMap<DeferredKey, DeferredDefinition>();
        for (Map.Entry<DeferredKey, DeferredDefinition> entry : entries.entrySet()) {
            DeferredKey key = Objects.requireNonNull(entry.getKey(), "key");
            DeferredDefinition value = Objects.requireNonNull(entry.getValue(), "value");
            // owner、kind 和 ordinal 均由 DeferredKey 冻结，任何错配都必须在入口拒绝。
            if (!key.equals(value.key())) {
                throw new IllegalArgumentException(
                        "Deferred registry identity mismatch: map key="
                                + key
                                + ", definition key="
                                + value.key());
            }
            copy.put(key, value);
        }
        this.entries = Collections.unmodifiableMap(copy);
        this.keys = Collections.unmodifiableList(new ArrayList<DeferredKey>(copy.keySet()));
    }

    /** 返回由指定后续阶段负责的 Deferred 定义。 */
    @Override
    public List<DeferredDefinition> requiredBy(RequiredStage stage) {
        Objects.requireNonNull(stage, "stage");
        List<DeferredDefinition> result = new ArrayList<DeferredDefinition>();
        for (DeferredDefinition definition : entries.values()) {
            if (definition.requiredStage() == stage) {
                result.add(definition);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /** 返回由指定 DefinitionKey 拥有的 Deferred 定义。 */
    @Override
    public List<DeferredDefinition> ownedBy(DefinitionKey key) {
        Objects.requireNonNull(key, "key");
        List<DeferredDefinition> result = new ArrayList<DeferredDefinition>();
        for (DeferredDefinition definition : entries.values()) {
            if (definition.ownerKey().equals(key)) {
                result.add(definition);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /** 按完整 DeferredKey 查找定义。 */
    @Override
    public Optional<DeferredDefinition> find(DeferredKey key) {
        return Optional.ofNullable(entries.get(Objects.requireNonNull(key, "key")));
    }

    /** 返回稳定排序、不可修改的 Key 列表。 */
    @Override
    public List<DeferredKey> keys() {
        return keys;
    }

    /** 返回 Registry 中的定义数量。 */
    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof ImmutableDeferredRegistry
                && entries.equals(((ImmutableDeferredRegistry) other).entries));
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }

    @Override
    public String toString() {
        return entries.toString();
    }
}
