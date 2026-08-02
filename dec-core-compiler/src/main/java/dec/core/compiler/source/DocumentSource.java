package dec.core.compiler.source;

import dec.core.compiler.canonical.DocumentFormat;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provider 解析后交给 Frontend 的完整不可变文档源事实。
 */
public final class DocumentSource {
    private final String sourceId;
    private final URI uri;
    private final DocumentFormat format;
    private final AllowedRoot allowedRoot;
    private final byte[] content;
    private final String contentDigest;

    /**
     * 冻结稳定身份、规范化 URI、文档格式、安全根、内容和内容摘要。
     *
     * @param sourceId 用于排序、去重和 Diagnostic 的稳定 Source 身份
     * @param uri Provider 已解析并规范化的绝对文档 URI
     * @param format 选择 Frontend 所需的显式文档格式
     * @param allowedRoot Provider 已验证的允许根事实
     * @param content 文档内容；构造器执行防御性复制
     * @param contentDigest 文档内容的稳定摘要
     */
    public DocumentSource(
            String sourceId,
            URI uri,
            DocumentFormat format,
            AllowedRoot allowedRoot,
            byte[] content,
            String contentDigest) {
        this.sourceId = requireText(sourceId, "sourceId");
        URI normalizedUri = Objects.requireNonNull(uri, "uri").normalize();
        if (!normalizedUri.isAbsolute()) {
            throw new IllegalArgumentException("uri must be absolute");
        }
        this.format = Objects.requireNonNull(format, "format");
        this.allowedRoot = Objects.requireNonNull(allowedRoot, "allowedRoot");
        if (!allowedRoot.contains(normalizedUri)) {
            throw new IllegalArgumentException("uri must be contained by allowedRoot");
        }
        this.uri = normalizedUri;
        this.content = Objects.requireNonNull(content, "content").clone();
        this.contentDigest = requireText(contentDigest, "contentDigest");
    }

    /**
     * 返回用于排序、去重和 Diagnostic 的稳定 Source 身份。
     */
    public String sourceId() {
        return sourceId;
    }

    /**
     * 返回 Provider 已解析并规范化的绝对文档 URI。
     */
    public URI uri() {
        return uri;
    }

    /**
     * 返回选择 Frontend 所需的显式文档格式。
     */
    public DocumentFormat format() {
        return format;
    }

    /**
     * 返回 Provider 已验证的不可变允许根事实。
     */
    public AllowedRoot allowedRoot() {
        return allowedRoot;
    }

    /**
     * 返回文档内容的防御性字节副本。
     */
    public byte[] content() {
        return content.clone();
    }

    /**
     * 返回文档内容的稳定摘要。
     */
    public String contentDigest() {
        return contentDigest;
    }

    /**
     * 规范化必填文本并拒绝空白值。
     */
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
        if (!(other instanceof DocumentSource)) {
            return false;
        }
        DocumentSource that = (DocumentSource) other;
        return sourceId.equals(that.sourceId)
                && uri.equals(that.uri)
                && format == that.format
                && allowedRoot.equals(that.allowedRoot)
                && Arrays.equals(content, that.content)
                && contentDigest.equals(that.contentDigest);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
                sourceId,
                uri,
                format,
                allowedRoot,
                contentDigest);
        return 31 * result + Arrays.hashCode(content);
    }

    @Override
    public String toString() {
        return "DocumentSource{"
                + "sourceId='" + sourceId + '\''
                + ", uri=" + uri
                + ", format=" + format
                + ", allowedRoot=" + allowedRoot
                + ", contentLength=" + content.length
                + ", contentDigest='" + contentDigest + '\''
                + '}';
    }
}
