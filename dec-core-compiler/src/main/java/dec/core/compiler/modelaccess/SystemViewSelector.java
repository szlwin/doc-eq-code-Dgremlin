package dec.core.compiler.modelaccess;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 当前 System 本地 View 的区分大小写精确 selector。
 */
public final class SystemViewSelector implements Comparable<SystemViewSelector> {
    private final String value;
    private final List<String> segments;

    /** 冻结精确 selector，禁止空段与隐式 trim。 */
    public SystemViewSelector(String value) {
        this.value = requireSelector(value);
        this.segments = Collections.unmodifiableList(
                Arrays.asList(this.value.split("\\.", -1)));
    }

    /** 返回完整 selector。 */
    public String value() {
        return value;
    }

    /** 返回不可修改的 selector 分段。 */
    public List<String> segments() {
        return segments;
    }

    @Override
    public int compareTo(SystemViewSelector other) {
        return value.compareTo(Objects.requireNonNull(other, "other").value);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof SystemViewSelector
                && value.equals(((SystemViewSelector) other).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    /** 校验 selector lexical。 */
    private static String requireSelector(String raw) {
        if (raw == null || raw.trim().isEmpty() || !raw.equals(raw.trim())) {
            throw new IllegalArgumentException("selector must be non-blank and trimmed");
        }
        for (String segment : raw.split("\\.", -1)) {
            if (segment.trim().isEmpty() || !segment.equals(segment.trim())) {
                throw new IllegalArgumentException("selector contains blank segment");
            }
        }
        return raw;
    }
}
