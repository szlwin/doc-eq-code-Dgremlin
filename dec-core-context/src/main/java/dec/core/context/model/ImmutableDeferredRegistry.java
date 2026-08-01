package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public final class ImmutableDeferredRegistry implements DeferredRegistry {
    private final Map<DeferredKey, DeferredDefinition> entries;
    private final List<DeferredKey> keys;

    public ImmutableDeferredRegistry(Map<DeferredKey, DeferredDefinition> entries) {
        Objects.requireNonNull(entries, "entries");
        TreeMap<DeferredKey, DeferredDefinition> copy = new TreeMap<DeferredKey, DeferredDefinition>();
        for (Map.Entry<DeferredKey, DeferredDefinition> entry : entries.entrySet()) {
            copy.put(Objects.requireNonNull(entry.getKey(), "key"),
                    Objects.requireNonNull(entry.getValue(), "value"));
        }
        this.entries = Collections.unmodifiableMap(copy);
        this.keys = Collections.unmodifiableList(new ArrayList<DeferredKey>(copy.keySet()));
    }

    @Override public List<DeferredDefinition> requiredBy(RequiredStage stage) {
        Objects.requireNonNull(stage, "stage");
        List<DeferredDefinition> result = new ArrayList<DeferredDefinition>();
        for (DeferredDefinition definition : entries.values()) if (definition.requiredStage() == stage) result.add(definition);
        return Collections.unmodifiableList(result);
    }
    @Override public List<DeferredDefinition> ownedBy(DefinitionKey key) {
        Objects.requireNonNull(key, "key");
        List<DeferredDefinition> result = new ArrayList<DeferredDefinition>();
        for (DeferredDefinition definition : entries.values()) if (definition.ownerKey().equals(key)) result.add(definition);
        return Collections.unmodifiableList(result);
    }
    @Override public Optional<DeferredDefinition> find(DeferredKey key) { return Optional.ofNullable(entries.get(Objects.requireNonNull(key, "key"))); }
    @Override public List<DeferredKey> keys() { return keys; }
    @Override public int size() { return entries.size(); }
    @Override public boolean equals(Object other) { return this == other || (other instanceof ImmutableDeferredRegistry && entries.equals(((ImmutableDeferredRegistry) other).entries)); }
    @Override public int hashCode() { return entries.hashCode(); }
    @Override public String toString() { return entries.toString(); }
}
