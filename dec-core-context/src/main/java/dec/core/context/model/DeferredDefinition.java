package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DeferredDefinition {
    private final DefinitionKey ownerKey;
    private final DeferredKind kind;
    private final RequiredStage requiredStage;
    private final String reasonCode;
    private final SourceRef sourceRef;
    private final NormalizedBody body;
    private final List<DefinitionKey> resolvedReferences;

    public DeferredDefinition(DefinitionKey ownerKey, DeferredKind kind, RequiredStage requiredStage,
                              String reasonCode, SourceRef sourceRef, NormalizedBody body,
                              List<DefinitionKey> resolvedReferences) {
        this.ownerKey = Objects.requireNonNull(ownerKey, "ownerKey");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.requiredStage = Objects.requireNonNull(requiredStage, "requiredStage");
        this.reasonCode = AbstractDefinitionKey.requireText(reasonCode, "reasonCode");
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
        this.body = Objects.requireNonNull(body, "body");
        List<DefinitionKey> copy = new ArrayList<DefinitionKey>(Objects.requireNonNull(resolvedReferences, "resolvedReferences"));
        if (copy.contains(null)) throw new NullPointerException("resolvedReferences contains null");
        Collections.sort(copy);
        this.resolvedReferences = Collections.unmodifiableList(copy);
    }

    public DefinitionKey ownerKey() { return ownerKey; }
    public DeferredKind kind() { return kind; }
    public RequiredStage requiredStage() { return requiredStage; }
    public String reasonCode() { return reasonCode; }
    public SourceRef sourceRef() { return sourceRef; }
    public NormalizedBody body() { return body; }
    public List<DefinitionKey> resolvedReferences() { return resolvedReferences; }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof DeferredDefinition)) return false;
        DeferredDefinition that = (DeferredDefinition) other;
        return ownerKey.equals(that.ownerKey) && kind == that.kind && requiredStage == that.requiredStage
                && reasonCode.equals(that.reasonCode) && sourceRef.equals(that.sourceRef)
                && body.equals(that.body) && resolvedReferences.equals(that.resolvedReferences);
    }
    @Override public int hashCode() { return Objects.hash(ownerKey, kind, requiredStage, reasonCode, sourceRef, body, resolvedReferences); }
    @Override public String toString() { return ownerKey + ":" + kind + "@" + requiredStage; }
}
