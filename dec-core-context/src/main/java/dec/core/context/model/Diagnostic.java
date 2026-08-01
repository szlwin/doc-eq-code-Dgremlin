package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Diagnostic implements Comparable<Diagnostic> {
    private final DiagnosticCode code;
    private final DiagnosticSeverity severity;
    private final String messageKey;
    private final DefinitionKey definitionKey;
    private final SourceRef sourceRef;
    private final List<SourceRef> relatedRefs;
    private final String recoveryHint;
    private final String pass;

    public Diagnostic(DiagnosticCode code, DiagnosticSeverity severity, String messageKey,
                      DefinitionKey definitionKey, SourceRef sourceRef,
                      List<SourceRef> relatedRefs, String recoveryHint, String pass) {
        this.code = Objects.requireNonNull(code, "code");
        this.severity = Objects.requireNonNull(severity, "severity");
        this.messageKey = AbstractDefinitionKey.requireText(messageKey, "messageKey");
        this.definitionKey = definitionKey;
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
        List<SourceRef> copy = new ArrayList<SourceRef>(Objects.requireNonNull(relatedRefs, "relatedRefs"));
        if (copy.contains(null)) throw new NullPointerException("relatedRefs contains null");
        Collections.sort(copy);
        this.relatedRefs = Collections.unmodifiableList(copy);
        this.recoveryHint = recoveryHint == null ? null : recoveryHint.trim();
        this.pass = AbstractDefinitionKey.requireText(pass, "pass");
    }

    public DiagnosticCode code() { return code; }
    public DiagnosticSeverity severity() { return severity; }
    public String messageKey() { return messageKey; }
    public Optional<DefinitionKey> definitionKey() { return Optional.ofNullable(definitionKey); }
    public SourceRef sourceRef() { return sourceRef; }
    public List<SourceRef> relatedRefs() { return relatedRefs; }
    public Optional<String> recoveryHint() { return Optional.ofNullable(recoveryHint); }
    public String pass() { return pass; }
    public String entityKey() { return definitionKey == null ? "" : definitionKey.canonical(); }

    @Override
    public int compareTo(Diagnostic other) {
        Objects.requireNonNull(other, "other");
        int comparison = sourceRef.sourceId().compareTo(other.sourceRef.sourceId());
        if (comparison != 0) return comparison;
        comparison = Integer.compare(sourceRef.line(), other.sourceRef.line());
        if (comparison != 0) return comparison;
        comparison = Integer.compare(sourceRef.column(), other.sourceRef.column());
        if (comparison != 0) return comparison;
        comparison = code.code().compareTo(other.code.code());
        if (comparison != 0) return comparison;
        comparison = entityKey().compareTo(other.entityKey());
        if (comparison != 0) return comparison;
        return pass.compareTo(other.pass);
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Diagnostic)) return false;
        Diagnostic that = (Diagnostic) other;
        return code == that.code && severity == that.severity && messageKey.equals(that.messageKey)
                && Objects.equals(definitionKey, that.definitionKey) && sourceRef.equals(that.sourceRef)
                && relatedRefs.equals(that.relatedRefs) && Objects.equals(recoveryHint, that.recoveryHint)
                && pass.equals(that.pass);
    }
    @Override public int hashCode() {
        return Objects.hash(code, severity, messageKey, definitionKey, sourceRef, relatedRefs, recoveryHint, pass);
    }
    @Override public String toString() { return severity + ":" + code.code() + "@" + sourceRef + ":" + messageKey; }
}
