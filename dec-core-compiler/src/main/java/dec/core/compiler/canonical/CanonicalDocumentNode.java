package dec.core.compiler.canonical;

import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Frontend 输出的格式中立不可变 Canonical 文档节点。
 *
 * <p>该类型只包含稳定名称、属性、标量、子节点和来源事实，
 * 不暴露 XML DOM、YAML Node 或第三方 Parser 类型。</p>
 */
public final class CanonicalDocumentNode {
    private final String name;
    private final Map<String, String> attributes;
    private final Optional<String> scalar;
    private final List<CanonicalDocumentNode> children;
    private final SourceRef sourceRef;
    private final DocumentFormat format;
    private final String schemaVersion;

    /**
     * 冻结一个 Canonical 节点的完整格式中立事实。
     *
     * @param name 节点规范名称
     * @param attributes 按 key 稳定排序的属性输入
     * @param scalar 可选标量文本；Optional 本身不接受 null
     * @param children 保持文档顺序的子节点输入
     * @param sourceRef 节点声明位置
     * @param format 产生该节点的文档 Frontend 格式
     * @param schemaVersion 解析时使用的 Schema 版本
     */
    public CanonicalDocumentNode(
            String name,
            Map<String, String> attributes,
            Optional<String> scalar,
            List<CanonicalDocumentNode> children,
            SourceRef sourceRef,
            DocumentFormat format,
            String schemaVersion) {
        this.name = requireText(name, "name");
        this.attributes = immutableAttributes(attributes);
        this.scalar = Objects.requireNonNull(scalar, "scalar");
        this.children = immutableChildren(children);
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
        this.format = Objects.requireNonNull(format, "format");
        this.schemaVersion = requireText(schemaVersion, "schemaVersion");
    }

    /**
     * 返回节点规范名称。
     */
    public String name() {
        return name;
    }

    /**
     * 返回按 key 稳定排序且不可变的属性。
     */
    public Map<String, String> attributes() {
        return attributes;
    }

    /**
     * 返回可选标量文本。
     */
    public Optional<String> scalar() {
        return scalar;
    }

    /**
     * 返回保持文档顺序且不可变的子节点。
     */
    public List<CanonicalDocumentNode> children() {
        return children;
    }

    /**
     * 返回节点声明位置。
     */
    public SourceRef sourceRef() {
        return sourceRef;
    }

    /**
     * 返回产生节点的文档 Frontend 格式。
     */
    public DocumentFormat format() {
        return format;
    }

    /**
     * 返回解析节点时使用的 Schema 版本。
     */
    public String schemaVersion() {
        return schemaVersion;
    }

    /**
     * 复制、校验并按 key 稳定排序属性。
     */
    private static Map<String, String> immutableAttributes(
            Map<String, String> attributes) {
        Objects.requireNonNull(attributes, "attributes");
        Map<String, String> sorted = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = requireText(entry.getKey(), "attribute key");
            String value = Objects.requireNonNull(
                    entry.getValue(),
                    "attribute value");
            sorted.put(key, value);
        }
        return Collections.unmodifiableMap(sorted);
    }

    /**
     * 复制子节点并拒绝 null，保持 Frontend 提供的文档顺序。
     */
    private static List<CanonicalDocumentNode> immutableChildren(
            List<CanonicalDocumentNode> children) {
        Objects.requireNonNull(children, "children");
        List<CanonicalDocumentNode> copy =
                new ArrayList<CanonicalDocumentNode>(children.size());
        for (CanonicalDocumentNode child : children) {
            copy.add(Objects.requireNonNull(child, "children contains null"));
        }
        return Collections.unmodifiableList(copy);
    }

    /**
     * 规范化必填文本并拒绝空白值。
     */
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
        if (!(other instanceof CanonicalDocumentNode)) {
            return false;
        }
        CanonicalDocumentNode that = (CanonicalDocumentNode) other;
        return name.equals(that.name)
                && attributes.equals(that.attributes)
                && scalar.equals(that.scalar)
                && children.equals(that.children)
                && sourceRef.equals(that.sourceRef)
                && format == that.format
                && schemaVersion.equals(that.schemaVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                attributes,
                scalar,
                children,
                sourceRef,
                format,
                schemaVersion);
    }

    @Override
    public String toString() {
        return "CanonicalDocumentNode{"
                + "name='" + name + '\''
                + ", attributes=" + attributes
                + ", scalar=" + scalar
                + ", children=" + children
                + ", sourceRef=" + sourceRef
                + ", format=" + format
                + ", schemaVersion='" + schemaVersion + '\''
                + '}';
    }
}
