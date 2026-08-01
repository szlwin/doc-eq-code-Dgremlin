package dec.core.context.model;
import java.util.Objects;
public final class CompiledDefinition {
    private final DefinitionKey key;
    private final SourceRef sourceRef;
    private final NormalizedBody normalizedBody;
    public CompiledDefinition(DefinitionKey key, SourceRef sourceRef, NormalizedBody normalizedBody) {
        this.key = Objects.requireNonNull(key, "key");
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
        this.normalizedBody = Objects.requireNonNull(normalizedBody, "normalizedBody");
    }
    public DefinitionKey key() { return key; }
    public SourceRef sourceRef() { return sourceRef; }
    public NormalizedBody normalizedBody() { return normalizedBody; }
    @Override public boolean equals(Object other) { return this == other || (other instanceof CompiledDefinition && key.equals(((CompiledDefinition) other).key) && sourceRef.equals(((CompiledDefinition) other).sourceRef) && normalizedBody.equals(((CompiledDefinition) other).normalizedBody)); }
    @Override public int hashCode() { return Objects.hash(key, sourceRef, normalizedBody); }
    @Override public String toString() { return key + "@" + sourceRef; }
}
