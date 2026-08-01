package dec.core.context.model;

import java.util.Objects;

/**
 * 已发布 SourceManifest 中的一条中立依赖边。
 */
public final class PublishedSourceDependency implements Comparable<PublishedSourceDependency> {
    private final String edgeType;
    private final String fromSourceId;
    private final String targetSourceId;
    private final SourceRef declarationSourceRef;

    /**
     * 构造由源声明位置支撑的依赖边。
     *
     * @param edgeType 稳定边类型
     * @param fromSourceId 声明依赖的源标识
     * @param targetSourceId 被依赖的目标源标识
     * @param declarationSourceRef 依赖声明所在位置
     */
    public PublishedSourceDependency(
            String edgeType,
            String fromSourceId,
            String targetSourceId,
            SourceRef declarationSourceRef) {
        this.edgeType = AbstractDefinitionKey.requireText(edgeType, "edgeType");
        this.fromSourceId = AbstractDefinitionKey.requireText(fromSourceId, "fromSourceId");
        this.targetSourceId = AbstractDefinitionKey.requireText(targetSourceId, "targetSourceId");
        this.declarationSourceRef = Objects.requireNonNull(
                declarationSourceRef,
                "declarationSourceRef");
        // 声明位置必须属于依赖起点 Source，否则同一条边会表达两个声明来源。
        if (!this.fromSourceId.equals(this.declarationSourceRef.sourceId())) {
            throw new IllegalArgumentException(
                    "Dependency declaration source must equal fromSourceId: "
                            + this.fromSourceId
                            + " != "
                            + this.declarationSourceRef.sourceId());
        }
    }

    /**
     * 返回稳定边类型。
     */
    public String edgeType() {
        return edgeType;
    }

    /**
     * 返回依赖起点源标识。
     */
    public String fromSourceId() {
        return fromSourceId;
    }

    /**
     * 返回依赖目标源标识。
     */
    public String targetSourceId() {
        return targetSourceId;
    }

    /**
     * 返回依赖声明位置。
     */
    public SourceRef declarationSourceRef() {
        return declarationSourceRef;
    }

    /**
     * 使用稳定语义字段提供确定性排序。
     */
    @Override
    public int compareTo(PublishedSourceDependency other) {
        Objects.requireNonNull(other, "other");
        int comparison = fromSourceId.compareTo(other.fromSourceId);
        if (comparison != 0) {
            return comparison;
        }
        comparison = edgeType.compareTo(other.edgeType);
        if (comparison != 0) {
            return comparison;
        }
        comparison = targetSourceId.compareTo(other.targetSourceId);
        return comparison != 0
                ? comparison
                : declarationSourceRef.compareTo(other.declarationSourceRef);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PublishedSourceDependency)) {
            return false;
        }
        PublishedSourceDependency that = (PublishedSourceDependency) other;
        return edgeType.equals(that.edgeType)
                && fromSourceId.equals(that.fromSourceId)
                && targetSourceId.equals(that.targetSourceId)
                && declarationSourceRef.equals(that.declarationSourceRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edgeType, fromSourceId, targetSourceId, declarationSourceRef);
    }

    @Override
    public String toString() {
        return edgeType + ":" + fromSourceId + "->" + targetSourceId;
    }
}
