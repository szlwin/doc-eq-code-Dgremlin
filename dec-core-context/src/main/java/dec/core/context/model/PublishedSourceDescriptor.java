package dec.core.context.model;

import java.util.Objects;

/**
 * 已发布 SourceManifest 中单个源文件的中立描述。
 */
public final class PublishedSourceDescriptor implements Comparable<PublishedSourceDescriptor> {
    private final String sourceId;
    private final String format;
    private final String contentDigest;

    /**
     * 构造不依赖具体解析器的源文件描述。
     *
     * @param sourceId 规范化源标识
     * @param format 源格式，例如 XML 或 YAML
     * @param contentDigest 源内容摘要
     */
    public PublishedSourceDescriptor(String sourceId, String format, String contentDigest) {
        this.sourceId = AbstractDefinitionKey.requireText(sourceId, "sourceId");
        this.format = AbstractDefinitionKey.requireText(format, "format");
        this.contentDigest = AbstractDefinitionKey.requireText(contentDigest, "contentDigest");
    }

    /**
     * 返回规范化源标识。
     */
    public String sourceId() {
        return sourceId;
    }

    /**
     * 返回不依赖解析库的源格式名称。
     */
    public String format() {
        return format;
    }

    /**
     * 返回源内容摘要。
     */
    public String contentDigest() {
        return contentDigest;
    }

    /**
     * 按 sourceId、format、contentDigest 提供稳定顺序。
     */
    @Override
    public int compareTo(PublishedSourceDescriptor other) {
        Objects.requireNonNull(other, "other");
        int comparison = sourceId.compareTo(other.sourceId);
        if (comparison != 0) {
            return comparison;
        }
        comparison = format.compareTo(other.format);
        return comparison != 0
                ? comparison
                : contentDigest.compareTo(other.contentDigest);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PublishedSourceDescriptor)) {
            return false;
        }
        PublishedSourceDescriptor that = (PublishedSourceDescriptor) other;
        return sourceId.equals(that.sourceId)
                && format.equals(that.format)
                && contentDigest.equals(that.contentDigest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, format, contentDigest);
    }

    @Override
    public String toString() {
        return sourceId + "[" + format + "]@" + contentDigest;
    }
}
