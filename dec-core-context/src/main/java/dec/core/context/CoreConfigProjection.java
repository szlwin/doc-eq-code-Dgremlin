package dec.core.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CoreConfigProjection {
    private final List<?> data;
    private final List<?> views;
    private final List<?> rules;

    public CoreConfigProjection(List<?> data, List<?> views, List<?> rules) {
        this.data = immutableCopy(data, "data");
        this.views = immutableCopy(views, "views");
        this.rules = immutableCopy(rules, "rules");
    }

    public static CoreConfigProjection empty() {
        return new CoreConfigProjection(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public List<?> data() { return data; }
    public List<?> views() { return views; }
    public List<?> rules() { return rules; }

    private static List<?> immutableCopy(List<?> values, String name) {
        Objects.requireNonNull(values, name);
        List<Object> copy = new ArrayList<Object>(values.size());
        for (Object value : values) copy.add(Objects.requireNonNull(value, name + " contains null"));
        return Collections.unmodifiableList(copy);
    }

    @Override public boolean equals(Object other) { return this == other || (other instanceof CoreConfigProjection && data.equals(((CoreConfigProjection) other).data) && views.equals(((CoreConfigProjection) other).views) && rules.equals(((CoreConfigProjection) other).rules)); }
    @Override public int hashCode() { return Objects.hash(data, views, rules); }
    @Override public String toString() { return "CoreConfigProjection{" + data.size() + "," + views.size() + "," + rules.size() + "}"; }
}
