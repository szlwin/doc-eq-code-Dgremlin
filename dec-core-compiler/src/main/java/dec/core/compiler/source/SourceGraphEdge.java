package dec.core.compiler.source;

import dec.core.context.model.SourceRef;
import java.util.Objects;

/**
 * 配置文档中一条显式 Source 声明边。
 */
public final class SourceGraphEdge implements Comparable<SourceGraphEdge> {
    private final SourceEdgeType edgeType;
    private final String fromSourceId;
    private final SourceReference targetReference;
    private final SourceRef declarationSourceRef;

    /**
     * 冻结边类型、来源、目标引用和声明位置。
     */
    public SourceGraphEdge(
            SourceEdgeType edgeType,
            String fromSourceId,
            SourceReference targetReference,
            SourceRef declarationSourceRef) {
        this.edgeType = Objects.requireNonNull(edgeType, "edgeType");
        this.fromSourceId = requireText(fromSourceId, "fromSourceId");
        this.targetReference = Objects.requireNonNull(
                targetReference,
                "targetReference");
        this.declarationSourceRef = Objects.requireNonNull(
                declarationSourceRef,
                "declarationSourceRef");
    }

    public SourceEdgeType edgeType() {
        return edgeType;
    }

    public String fromSourceId() {
        return fromSourceId;
    }

    public SourceReference targetReference() {
        return targetReference;
    }

    public SourceRef declarationSourceRef() {
        return declarationSourceRef;
    }

    @Override
    public int compareTo(SourceGraphEdge other) {
        Objects.requireNonNull(other, "other");
        int comparison = fromSourceId.compareTo(other.fromSourceId);
        if (comparison != 0) {
            return comparison;
        }
        comparison = Integer.compare(edgeType.ordinal(), other.edgeType.ordinal());
        if (comparison != 0) {
            return comparison;
        }
        comparison = targetReference.value().compareTo(
                other.targetReference.value());
        if (comparison != 0) {
            return comparison;
        }
        return declarationSourceRef.compareTo(other.declarationSourceRef);
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
        if (!(other instanceof SourceGraphEdge)) {
            return false;
        }
        SourceGraphEdge that = (SourceGraphEdge) other;
        return edgeType == that.edgeType
                && fromSourceId.equals(that.fromSourceId)
                && targetReference.equals(that.targetReference)
                && declarationSourceRef.equals(that.declarationSourceRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                edgeType,
                fromSourceId,
                targetReference,
                declarationSourceRef);
    }

    @Override
    public String toString() {
        return "SourceGraphEdge{"
                + "edgeType=" + edgeType
                + ", fromSourceId='" + fromSourceId + '\''
                + ", targetReference=" + targetReference
                + ", declarationSourceRef=" + declarationSourceRef
                + '}';
    }
}
