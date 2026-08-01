package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.List;

public final class ImmutableRegistry<K extends DefinitionKey, V> implements Registry<K, V> {
    private final Map<K, V> entries;
    private final List<K> keys;

    public ImmutableRegistry(Map<K, V> entries) {
        Objects.requireNonNull(entries, "entries");
        TreeMap<K, V> copy = new TreeMap<K, V>();
        for (Map.Entry<K, V> entry : entries.entrySet()) {
            copy.put(Objects.requireNonNull(entry.getKey(), "key"),
                    Objects.requireNonNull(entry.getValue(), "value"));
        }
        this.entries = Collections.unmodifiableMap(copy);
        this.keys = Collections.unmodifiableList(new ArrayList<K>(copy.keySet()));
    }

    @Override public Optional<V> find(K key) { return Optional.ofNullable(entries.get(Objects.requireNonNull(key, "key"))); }
    @Override public V require(K key) {
        V value = entries.get(Objects.requireNonNull(key, "key"));
        if (value == null) throw new NoSuchElementException("No definition for " + key.canonical());
        return value;
    }
    @Override public List<K> keys() { return keys; }
    @Override public int size() { return entries.size(); }

    @Override public boolean equals(Object other) {
        return this == other || (other instanceof ImmutableRegistry && entries.equals(((ImmutableRegistry<?, ?>) other).entries));
    }
    @Override public int hashCode() { return entries.hashCode(); }
    @Override public String toString() { return entries.toString(); }
}
