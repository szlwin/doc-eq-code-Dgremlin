package dec.core.compiler.symbol;

import dec.core.context.model.DefinitionKey;
import dec.core.context.model.SourceRef;
import java.util.Objects;

/**
 * 一个已由原始 lexical 引用精确绑定到 TypedKey 的不可变事实。
 */
public final class ResolvedReference implements Comparable<ResolvedReference> {
    private final DefinitionKey sourceKey;
    private final String role;
    private final String targetToken;
    private final DefinitionKey targetKey;
    private final SourceRef sourceRef;

    /**
     * 冻结来源定义、引用角色、原始目标文本和强类型目标。
     */
    public ResolvedReference(
            DefinitionKey sourceKey,
            String role,
            String targetToken,
            DefinitionKey targetKey,
            SourceRef sourceRef) {
        this.sourceKey = Objects.requireNonNull(sourceKey, "sourceKey");
        this.role = requireText(role, "role");
        this.targetToken = requireText(targetToken, "targetToken");
        this.targetKey = Objects.requireNonNull(targetKey, "targetKey");
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
    }

    public DefinitionKey sourceKey() {
        return sourceKey;
    }

    public String role() {
        return role;
    }

    public String targetToken() {
        return targetToken;
    }

    public DefinitionKey targetKey() {
        return targetKey;
    }

    public SourceRef sourceRef() {
        return sourceRef;
    }

    /**
     * 按来源、来源 Key、角色和目标 Key 建立稳定顺序。
     */
    @Override
    public int compareTo(ResolvedReference other) {
        Objects.requireNonNull(other, "other");
        int comparison = sourceRef.compareTo(other.sourceRef);
        if (comparison != 0) {
            return comparison;
        }
        comparison = sourceKey.compareTo(other.sourceKey);
        if (comparison != 0) {
            return comparison;
        }
        comparison = role.compareTo(other.role);
        if (comparison != 0) {
            return comparison;
        }
        comparison = targetKey.compareTo(other.targetKey);
        if (comparison != 0) {
            return comparison;
        }
        return targetToken.compareTo(other.targetToken);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResolvedReference)) {
            return false;
        }
        ResolvedReference that = (ResolvedReference) other;
        return sourceKey.equals(that.sourceKey)
                && role.equals(that.role)
                && targetToken.equals(that.targetToken)
                && targetKey.equals(that.targetKey)
                && sourceRef.equals(that.sourceRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceKey, role, targetToken, targetKey, sourceRef);
    }

    @Override
    public String toString() {
        return sourceKey + ":" + role + "=" + targetToken + "->" + targetKey;
    }

    private static String requireText(String value, String name) {
        String required = Objects.requireNonNull(value, name);
        if (required.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return required;
    }
}
