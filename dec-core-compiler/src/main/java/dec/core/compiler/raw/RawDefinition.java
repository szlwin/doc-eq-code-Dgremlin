package dec.core.compiler.raw;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * T06 输出的格式中立不可变 Raw 定义。
 */
public final class RawDefinition {
    private final RawDefinitionKind kind;
    private final long sourceOrdinal;
    private final SourceRef sourceRef;
    private final Optional<String> ownerToken;
    private final Optional<String> name;
    private final Map<String, String> attributes;
    private final List<RawReference> references;
    private final RawNodeBody body;
    private final DocumentFormat format;
    private final String schemaVersion;

    /**
     * 冻结一个 RawDefinition 的全部 lexical 与来源事实。
     */
    public RawDefinition(
            RawDefinitionKind kind,
            long sourceOrdinal,
            SourceRef sourceRef,
            Optional<String> ownerToken,
            Optional<String> name,
            Map<String, String> attributes,
            List<RawReference> references,
            RawNodeBody body,
            DocumentFormat format,
            String schemaVersion) {
        if (sourceOrdinal < 0L) {
            throw new IllegalArgumentException("sourceOrdinal must not be negative");
        }
        this.kind = Objects.requireNonNull(kind, "kind");
        this.sourceOrdinal = sourceOrdinal;
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
        this.ownerToken = immutableOptional(ownerToken, "ownerToken");
        this.name = immutableOptional(name, "name");
        this.attributes = immutableAttributes(attributes);
        this.references = immutableReferences(references);
        this.body = Objects.requireNonNull(body, "body");
        this.format = Objects.requireNonNull(format, "format");
        this.schemaVersion = requireText(schemaVersion, "schemaVersion");
    }

    /**
     * 返回冻结的定义类别。
     */
    public RawDefinitionKind kind() {
        return kind;
    }

    /**
     * 返回从 0 开始的稳定来源顺序号。
     */
    public long sourceOrdinal() {
        return sourceOrdinal;
    }

    /**
     * 返回语义定义节点的 Canonical 来源位置。
     */
    public SourceRef sourceRef() {
        return sourceRef;
    }

    /**
     * 返回尚未解析的 owner lexical token。
     */
    public Optional<String> ownerToken() {
        return ownerToken;
    }

    /**
     * 返回定义名称；允许无显式名称的 PRODUCE 为空。
     */
    public Optional<String> name() {
        return name;
    }

    /**
     * 返回稳定排序且不可变的定义节点属性。
     */
    public Map<String, String> attributes() {
        return attributes;
    }

    /**
     * 返回保持 Canonical 遍历顺序的未解析引用。
     */
    public List<RawReference> references() {
        return references;
    }

    /**
     * 返回完整的格式中立 normalized body。
     */
    public RawNodeBody body() {
        return body;
    }

    /**
     * 返回原始文档格式来源事实。
     */
    public DocumentFormat format() {
        return format;
    }

    /**
     * 返回 Frontend 传播的 schemaVersion。
     */
    public String schemaVersion() {
        return schemaVersion;
    }

    /**
     * 复制可选文本并拒绝 present-but-blank 值。
     */
    private static Optional<String> immutableOptional(
            Optional<String> value,
            String name) {
        Objects.requireNonNull(value, name);
        return value.isPresent()
                ? Optional.of(requireText(value.get(), name))
                : Optional.<String>empty();
    }

    /**
     * 复制并按 key 排序属性，阻断调用方后续修改。
     */
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

    /**
     * 复制引用列表并逐项拒绝 null。
     */
    private static List<RawReference> immutableReferences(
            List<RawReference> references) {
        Objects.requireNonNull(references, "references");
        List<RawReference> copy = new ArrayList<RawReference>(references.size());
        for (RawReference reference : references) {
            copy.add(Objects.requireNonNull(reference, "references contains null"));
        }
        return Collections.unmodifiableList(copy);
    }

    /**
     * 规范化必填文本并拒绝空白。
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
        if (!(other instanceof RawDefinition)) {
            return false;
        }
        RawDefinition that = (RawDefinition) other;
        return sourceOrdinal == that.sourceOrdinal
                && kind == that.kind
                && sourceRef.equals(that.sourceRef)
                && ownerToken.equals(that.ownerToken)
                && name.equals(that.name)
                && attributes.equals(that.attributes)
                && references.equals(that.references)
                && body.equals(that.body)
                && format == that.format
                && schemaVersion.equals(that.schemaVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, sourceOrdinal, sourceRef, ownerToken, name,
                attributes, references, body, format, schemaVersion);
    }

    @Override
    public String toString() {
        return "RawDefinition{" + kind + "#" + sourceOrdinal
                + ", owner=" + ownerToken + ", name=" + name
                + ", sourceRef=" + sourceRef + '}';
    }
}
