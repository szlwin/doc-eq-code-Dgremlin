package dec.core.compiler.modelaccess;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * read/write@path 的精确共享模型源路径。
 */
public final class SharedModelPath implements Comparable<SharedModelPath> {
    private final String value;
    private final List<String> segments;

    /** 冻结精确路径；星号只允许作为完整路径。 */
    public SharedModelPath(String value) {
        this.value = requirePath(value, "sourcePath");
        this.segments = immutableSegments(this.value);
    }

    /** 返回未改写的规范路径值。 */
    public String value() {
        return value;
    }

    /** 返回不可修改的路径分段。 */
    public List<String> segments() {
        return segments;
    }

    /** 判断当前路径是否与另一 WRITE 路径相同或形成祖先/后代关系。 */
    public boolean overlaps(SharedModelPath other) {
        Objects.requireNonNull(other, "other");
        return value.equals(other.value)
                || value.startsWith(other.value + ".")
                || other.value.startsWith(value + ".");
    }

    @Override
    public int compareTo(SharedModelPath other) {
        return value.compareTo(Objects.requireNonNull(other, "other").value);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof SharedModelPath
                && value.equals(((SharedModelPath) other).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    /** 校验精确点分路径。 */
    private static String requirePath(String raw, String field) {
        if (raw == null || raw.trim().isEmpty() || !raw.equals(raw.trim())) {
            throw new IllegalArgumentException(field + " must be non-blank and trimmed");
        }
        if ("*".equals(raw)) {
            return raw;
        }
        String[] values = raw.split("\\.", -1);
        for (String value : values) {
            if (value.trim().isEmpty() || !value.equals(value.trim())) {
                throw new IllegalArgumentException(field + " contains blank segment");
            }
        }
        return raw;
    }

    /** 冻结点分段。 */
    private static List<String> immutableSegments(String path) {
        if ("*".equals(path)) {
            return Collections.singletonList(path);
        }
        return Collections.unmodifiableList(Arrays.asList(path.split("\\.", -1)));
    }
}
