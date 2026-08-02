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
     * 创建允许根，并在规范化前后拒绝查询参数、片段和父目录穿越。
     *
     * <p>必须先验证原始 URI，再调用 {@link URI#normalize()}，否则字面量
     * {@code ..} 可能在检查前消失并扩大允许根。对于 opaque URI，query
     * 位于 scheme-specific part 中，不能只依赖 {@link URI#getQuery()}。</p>
     *
     * @param uri Provider 已验证的绝对根 URI
     */
    public AllowedRoot(URI uri) {
        URI original = Objects.requireNonNull(uri, "uri");
        requireSafeAbsolute(original, "allowed root");
        URI normalized = original.normalize();
        requireSafeAbsolute(normalized, "normalized allowed root");
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
     * <p>候选必须在规范化前后都通过安全验证。非法候选属于 Provider
     * 策略失败，因此本方法返回 false，而不是向 Compiler 抛出预期异常。</p>
     *
     * @param candidate 待验证候选 URI
     * @return 候选与根相同或位于根的后代路径时返回 true
     */
    public boolean contains(URI candidate) {
        URI original = Objects.requireNonNull(candidate, "candidate");
        if (!isSafeAbsolute(original)) {
            return false;
        }
        URI normalized = original.normalize();
        if (!isSafeAbsolute(normalized)) {
            return false;
        }
        if (!Objects.equals(uri.getScheme(), normalized.getScheme())
                || !Objects.equals(uri.getAuthority(), normalized.getAuthority())) {
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
     * 校验绝对 URI 的来源安全语义，并为构造器提供明确失败原因。
     */
    private static void requireSafeAbsolute(URI value, String description) {
        if (!value.isAbsolute()) {
            throw new IllegalArgumentException(description + " URI must be absolute");
        }
        if (hasQueryOrFragment(value)) {
            throw new IllegalArgumentException(
                    description + " must not contain query or fragment");
        }
        if (containsTraversalSegment(rawLocation(value))
                || containsTraversalSegment(location(value))) {
            throw new IllegalArgumentException(
                    description + " must not contain parent traversal");
        }
    }

    /**
     * 判断 URI 是否可安全用于根边界比较。
     */
    private static boolean isSafeAbsolute(URI value) {
        return value.isAbsolute()
                && !hasQueryOrFragment(value)
                && !containsTraversalSegment(rawLocation(value))
                && !containsTraversalSegment(location(value));
    }

    /**
     * 检查层次 URI 的 query/fragment，以及 opaque URI scheme-specific part
     * 中不会由 URI.getQuery() 暴露的 query 标记。
     */
    private static boolean hasQueryOrFragment(URI value) {
        if (value.getRawFragment() != null || value.getFragment() != null) {
            return true;
        }
        if (!value.isOpaque()) {
            return value.getRawQuery() != null || value.getQuery() != null;
        }
        return containsQueryMarker(rawLocation(value))
                || containsQueryMarker(location(value));
    }

    /**
     * 返回层次 URI 的解码 path，或不透明 URI 的解码 scheme-specific part。
     */
    private static String location(URI value) {
        String location = value.isOpaque()
                ? value.getSchemeSpecificPart()
                : value.getPath();
        return location == null ? "" : location;
    }

    /**
     * 返回层次 URI 的 raw path，或不透明 URI 的 raw scheme-specific part。
     */
    private static String rawLocation(URI value) {
        String location = value.isOpaque()
                ? value.getRawSchemeSpecificPart()
                : value.getRawPath();
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
     * 检查 raw 或解码路径中是否存在独立的父目录穿越段。
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

    /**
     * 检查 opaque URI 的 scheme-specific part 是否包含 query 分隔符。
     */
    private static boolean containsQueryMarker(String value) {
        return value.indexOf('?') >= 0;
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
