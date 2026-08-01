package dec.core.context.model;
import java.util.Objects;
public final class DigestPair {
    private final String sourceDigest;
    private final String semanticDigest;
    public DigestPair(String sourceDigest, String semanticDigest) {
        this.sourceDigest = AbstractDefinitionKey.requireText(sourceDigest, "sourceDigest");
        this.semanticDigest = AbstractDefinitionKey.requireText(semanticDigest, "semanticDigest");
    }
    public String sourceDigest() { return sourceDigest; }
    public String semanticDigest() { return semanticDigest; }
    @Override public boolean equals(Object other) { return this == other || (other instanceof DigestPair && sourceDigest.equals(((DigestPair) other).sourceDigest) && semanticDigest.equals(((DigestPair) other).semanticDigest)); }
    @Override public int hashCode() { return Objects.hash(sourceDigest, semanticDigest); }
    @Override public String toString() { return sourceDigest + "/" + semanticDigest; }
}
