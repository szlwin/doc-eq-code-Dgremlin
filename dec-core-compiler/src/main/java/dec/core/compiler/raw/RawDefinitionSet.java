package dec.core.compiler.raw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 按 sourceOrdinal 冻结的不可变 RawDefinition 集合。
 */
public final class RawDefinitionSet {
    private final List<RawDefinition> definitions;
    private final Map<RawDefinitionKind, List<RawDefinition>> byKind;

    /**
     * 复制、校验并冻结定义；同名定义在 T06 不覆盖、不去重。
     */
    public RawDefinitionSet(List<RawDefinition> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        List<RawDefinition> copy = new ArrayList<RawDefinition>(definitions.size());
        Set<Long> ordinals = new HashSet<Long>();
        for (RawDefinition definition : definitions) {
            RawDefinition required = Objects.requireNonNull(
                    definition,
                    "definitions contains null");
            if (!ordinals.add(required.sourceOrdinal())) {
                throw new IllegalArgumentException(
                        "duplicate sourceOrdinal: " + required.sourceOrdinal());
            }
            copy.add(required);
        }
        Collections.sort(copy, Comparator.comparingLong(
                RawDefinition::sourceOrdinal));
        this.definitions = Collections.unmodifiableList(copy);
        this.byKind = indexByKind(copy);
    }

    /** 返回按 sourceOrdinal 升序的全部定义。 */
    public List<RawDefinition> definitions() {
        return definitions;
    }

    /** 返回指定 Kind 的不可变定义子列表。 */
    public List<RawDefinition> definitions(RawDefinitionKind kind) {
        List<RawDefinition> values = byKind.get(
                Objects.requireNonNull(kind, "kind"));
        return values == null
                ? Collections.<RawDefinition>emptyList()
                : values;
    }

    /** 返回定义数量。 */
    public int size() {
        return definitions.size();
    }

    private static Map<RawDefinitionKind, List<RawDefinition>> indexByKind(
            List<RawDefinition> definitions) {
        Map<RawDefinitionKind, List<RawDefinition>> mutable =
                new EnumMap<RawDefinitionKind, List<RawDefinition>>(
                        RawDefinitionKind.class);
        for (RawDefinition definition : definitions) {
            List<RawDefinition> values = mutable.get(definition.kind());
            if (values == null) {
                values = new ArrayList<RawDefinition>();
                mutable.put(definition.kind(), values);
            }
            values.add(definition);
        }
        Map<RawDefinitionKind, List<RawDefinition>> frozen =
                new EnumMap<RawDefinitionKind, List<RawDefinition>>(
                        RawDefinitionKind.class);
        for (Map.Entry<RawDefinitionKind, List<RawDefinition>> entry
                : mutable.entrySet()) {
            frozen.put(entry.getKey(),
                    Collections.unmodifiableList(
                            new ArrayList<RawDefinition>(entry.getValue())));
        }
        return Collections.unmodifiableMap(frozen);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof RawDefinitionSet
                && definitions.equals(((RawDefinitionSet) other).definitions);
    }

    @Override
    public int hashCode() {
        return definitions.hashCode();
    }

    @Override
    public String toString() {
        return "RawDefinitionSet" + definitions;
    }
}
