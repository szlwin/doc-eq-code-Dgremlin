package dec.core.context.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** P2 统一的规范 ModelPath。路径仅表达属性导航，不携带 target 或 owner 身份。 */
public final class ModelPath implements Comparable<ModelPath> {
    private final List<String> segments;
    private final String canonical;

    private ModelPath(List<String> segments) {
        Objects.requireNonNull(segments, "segments");
        if (segments.isEmpty()) throw new IllegalArgumentException("segments must not be empty");
        List<String> copy = new ArrayList<String>(segments.size());
        for (String segment : segments) copy.add(requireSegment(segment));
        this.segments = Collections.unmodifiableList(copy);
        this.canonical = join(copy);
    }

    /** 从点分路径创建规范值；不 trim、不大小写折叠，也不允许通配符进入运行时路径。 */
    public static ModelPath of(String path) {
        Objects.requireNonNull(path, "path");
        if (!path.equals(path.trim()) || path.isEmpty()) throw new IllegalArgumentException("path must be non-blank and trimmed");
        return new ModelPath(Arrays.asList(path.split("\\.", -1)));
    }
    /** 从已经编译的精确分段创建规范路径。 */
    public static ModelPath ofSegments(List<String> segments) { return new ModelPath(segments); }
    public List<String> segments() { return segments; }
    public String canonical() { return canonical; }

    @Override
    public int compareTo(ModelPath other) { return canonical.compareTo(Objects.requireNonNull(other, "other").canonical); }
    @Override
    public boolean equals(Object other) { return this == other || other instanceof ModelPath && segments.equals(((ModelPath) other).segments); }
    @Override
    public int hashCode() { return segments.hashCode(); }
    @Override
    public String toString() { return canonical; }

    private static String requireSegment(String value) {
        Objects.requireNonNull(value, "segment");
        if (value.isEmpty() || !value.equals(value.trim()) || "*".equals(value)) throw new IllegalArgumentException("model path contains illegal segment: " + value);
        return value;
    }
    private static String join(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) { if (result.length() > 0) result.append('.'); result.append(value); }
        return result.toString();
    }
}
