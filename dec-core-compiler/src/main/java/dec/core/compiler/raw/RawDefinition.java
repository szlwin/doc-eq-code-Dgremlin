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
     *
     * <p>公开构造器同时强制 R24 的 Kind/owner/name 矩阵，避免调用方绕过
     * RawDefinitionBuilder 构造下游无法解释的非法状态。</p>
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
        requireKindMatrix(this.kind, this.ownerToken, this.name);
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
     * 返回尚未解析且未规范化的 owner lexical token。
     */
    public Optional<String> ownerToken() {
        return ownerToken;
    }

    /**
     * 返回尚未规范化的定义名称；PRODUCE 允许没有显式名称。
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
     * 复制可选文本；只使用 trim 判断空白，保存时保留原始 lexical token。
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
     * 强制 R24 冻结的 14 Kind owner/name 公开构造矩阵。
     */
    private static void requireKindMatrix(
            RawDefinitionKind kind,
            Optional<String> ownerToken,
            Optional<String> name) {
        boolean ownerRequired;
        boolean nameRequired = kind != RawDefinitionKind.PRODUCE;
        switch (kind) {
            case DATA_SOURCE:
            case CONNECTION:
            case INFORMATION:
            case MODEL_ACCESS:
            case RULE_VIEW:
            case RULE:
            case DIRECTORY:
            case ACTION:
            case PRODUCE:
                ownerRequired = true;
                break;
            case ROOT_CONFIG:
            case DATA:
            case VIEW:
            case SYSTEM:
            case BUSINESS_SCOPE:
                ownerRequired = false;
                break;
            default:
                throw new IllegalStateException("unexpected RawDefinitionKind: " + kind);
        }
        if (ownerToken.isPresent() != ownerRequired) {
            throw new IllegalArgumentException(
                    "ownerToken contract violated for kind " + kind);
        }
        if (nameRequired && !name.isPresent()) {
            throw new IllegalArgumentException(
                    "name contract violated for kind " + kind);
        }
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
     * 校验必填文本非空白并保留原始 lexical 值。
     */
    private static String requireText(String value, String name) {
        String required = Objects.requireNonNull(value, name);
        if (required.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return required;
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

    /**
     * 输出与 equals/hashCode 完全相同的全部语义字段。
     */
    @Override
    public String toString() {
        return "RawDefinition{"
                + "kind=" + kind
                + ", sourceOrdinal=" + sourceOrdinal
                + ", sourceRef=" + sourceRef
                + ", ownerToken=" + ownerToken
                + ", name=" + name
                + ", attributes=" + attributes
                + ", references=" + references
                + ", body=" + body
                + ", format=" + format
                + ", schemaVersion=" + schemaVersion
                + '}';
    }
}
