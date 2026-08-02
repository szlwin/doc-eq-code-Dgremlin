package dec.core.compiler.source;

import java.net.URI;
import java.util.Objects;

/**
 * Provider 完成来源策略判断后传递给 Compiler 的不可变允许根事实。
 *
 * <p>该值对象只负责 URI 规范化和词法路径边界，不执行文件系统 IO、
 * 符号链接解析或网络访问。真实路径与策略校验仍由 Source Provider 负责。</p>
 */
public final class AllowedRoot {
    private final URI uri;

    /**
     * 创建允许根，并拒绝相对 URI、查询参数、片段和显式路径穿越。
     *
     * @param uri Provider 已验证的绝对根 URI
     */
    public AllowedRoot(URI uri) {
        URI normalized = Objects.requireNonNull(uri, "uri").normalize();
        if (!normalized.isAbsolute()) {
            throw new IllegalArgumentException("uri must be absolute");
        }
        if (normalized.getQuery() != null || normalized.getFragment() != null) {
            throw new IllegalArgumentException(
                    "allowed root must not contain query or fragment");
        }
        if (containsTraversalSegment(location(normalized))) {
            throw new IllegalArgumentException(
                    "allowed root must not contain parent traversal");
        }
        this.uri = normalized;
    }

    /**
     * 返回规范化后的绝对允许根 URI。
     */
    public URI uri() {
        return uri;
    }

    /**
     * 判断候选 URI 是否位于同 scheme、同 authority 的根边界内。
     *
     * <p>判断使用路径段边界而不是普通字符串前缀，避免
     * {@code /config} 错误匹配 {@code /configuration}。根和候选的尾部斜杠
     * 会在边界比较时统一，因此 {@code /config} 与 {@code /config/}
     * 被视为同一根位置。</p>
     *
     * @param candidate 待验证候选 URI
     * @return 候选与根相同或位于根的后代路径时返回 true
     */
    public boolean contains(URI candidate) {
        URI normalized = Objects.requireNonNull(candidate, "candidate").normalize();
        if (!normalized.isAbsolute()) {
            return false;
        }
        if (normalized.getQuery() != null || normalized.getFragment() != null) {
            return false;
        }
        if (!Objects.equals(uri.getScheme(), normalized.getScheme())
                || !Objects.equals(uri.getAuthority(), normalized.getAuthority())) {
            return false;
        }

        String decodedCandidateLocation = location(normalized);
        if (containsTraversalSegment(decodedCandidateLocation)) {
            return false;
        }
        String rootLocation = boundaryLocation(uri);
        String candidateLocation = boundaryLocation(normalized);
        if (rootLocation.equals(candidateLocation)) {
            return true;
        }
        if (rootLocation.isEmpty() || "/".equals(rootLocation)) {
            return candidateLocation.startsWith("/") || !candidateLocation.isEmpty();
        }
        return candidateLocation.startsWith(rootLocation + "/");
    }

    /**
     * 返回层次 URI 的解码 path，或不透明 URI 的 scheme-specific part。
     */
    private static String location(URI value) {
        String location = value.isOpaque()
                ? value.getSchemeSpecificPart()
                : value.getPath();
        return location == null ? "" : location;
    }

    /**
     * 统一路径分隔符并移除非根路径的尾部斜杠，用于稳定边界比较。
     */
    private static String boundaryLocation(URI value) {
        String normalized = location(value).replace('\\', '/');
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * 检查解码路径中是否仍存在独立的父目录穿越段。
     */
    private static boolean containsTraversalSegment(String value) {
        String[] segments = value.replace('\\', '/').split("/");
        for (String segment : segments) {
            if ("..".equals(segment)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof AllowedRoot
                && uri.equals(((AllowedRoot) other).uri));
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        return "AllowedRoot{uri=" + uri + '}';
    }
}
