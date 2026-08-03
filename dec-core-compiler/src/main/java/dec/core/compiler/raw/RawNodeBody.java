package dec.core.compiler.raw;

import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * 不包含 parser 对象的递归格式中立 Raw body。
 */
public final class RawNodeBody {
    private final String name;
    private final Map<String, String> attributes;
    private final Optional<String> scalar;
    private final List<RawNodeBody> children;
    private final SourceRef sourceRef;

    /**
     * 冻结一个 Canonical 节点的 normalized body 事实。
     */
    public RawNodeBody(
            String name,
            Map<String, String> attributes,
            Optional<String> scalar,
            List<RawNodeBody> children,
            SourceRef sourceRef) {
        this.name = requireText(name, "name");
        this.attributes = immutableAttributes(attributes);
        this.scalar = Objects.requireNonNull(scalar, "scalar");
        this.children = immutableChildren(children);
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
    }

    /** 返回节点名称。 */
    public String name() {
        return name;
    }

    /** 返回稳定排序且不可变的属性。 */
    public Map<String, String> attributes() {
        return attributes;
    }

    /** 返回可选标量。 */
    public Optional<String> scalar() {
        return scalar;
    }

    /** 返回保持文档顺序的不可变 children。 */
    public List<RawNodeBody> children() {
        return children;
    }

    /** 返回节点来源位置。 */
    public SourceRef sourceRef() {
        return sourceRef;
    }

    private static Map<String, String> immutableAttributes(
            Map<String, String> attributes) {
        Objects.requireNonNull(attributes, "attributes");
        Map<String, String> copy = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            copy.put(requireText(entry.getKey(), "attribute key"),
                    Objects.requireNonNull(entry.getValue(), "attribute value"));
        }
        return Collections.unmodifiableMap(copy);
    }

    private static List<RawNodeBody> immutableChildren(List<RawNodeBody> children) {
        Objects.requireNonNull(children, "children");
        List<RawNodeBody> copy = new ArrayList<RawNodeBody>(children.size());
        for (RawNodeBody child : children) {
            copy.add(Objects.requireNonNull(child, "children contains null"));
        }
        return Collections.unmodifiableList(copy);
    }

    private static String requireText(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RawNodeBody)) {
            return false;
        }
        RawNodeBody that = (RawNodeBody) other;
        return name.equals(that.name)
                && attributes.equals(that.attributes)
                && scalar.equals(that.scalar)
                && children.equals(that.children)
                && sourceRef.equals(that.sourceRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, attributes, scalar, children, sourceRef);
    }

    @Override
    public String toString() {
        return "RawNodeBody{" + name + ", attributes=" + attributes
                + ", scalar=" + scalar + ", children=" + children
                + ", sourceRef=" + sourceRef + '}';
    }
}
