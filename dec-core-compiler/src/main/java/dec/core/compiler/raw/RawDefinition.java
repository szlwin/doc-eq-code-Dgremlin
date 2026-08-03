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

    public RawDefinitionKind kind() {
        return kind;
    }

    public long sourceOrdinal() {
        return sourceOrdinal;
    }

    public SourceRef sourceRef() {
        return sourceRef;
    }

    public Optional<String> ownerToken() {
        return ownerToken;
    }

    public Optional<String> name() {
        return name;
    }

    public Map<String, String> attributes() {
        return attributes;
    }

    public List<RawReference> references() {
        return references;
    }

    public RawNodeBody body() {
        return body;
    }

    public DocumentFormat format() {
        return format;
    }

    public String schemaVersion() {
        return schemaVersion;
    }

    private static Optional<String> immutableOptional(
            Optional<String> value,
            String name) {
        Objects.requireNonNull(value, name);
        return value.isPresent()
                ? Optional.of(requireText(value.get(), name))
                : Optional.<String>empty();
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

    private static List<RawReference> immutableReferences(
            List<RawReference> references) {
        Objects.requireNonNull(references, "references");
        List<RawReference> copy = new ArrayList<RawReference>(references.size());
        for (RawReference reference : references) {
            copy.add(Objects.requireNonNull(reference, "references contains null"));
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
