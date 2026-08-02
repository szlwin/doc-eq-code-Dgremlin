package dec.core.compiler.source;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 调用方提供的不可变 Source 引用及其统一 canonical key。
 *
 * <p>该值对象只消除独立的当前目录 {@code .} 段及其一次编码形式，
 * 并统一 URI scheme 大小写。不消除 {@code ..}、query、fragment、编码分隔符
 * 或非法 URI 文本。这样 Provider、声明边、排序和环路检测可以共享同一身份，
 * 同时 SourcePolicy 仍能看到并拒绝父目录穿越和其它安全违规。</p>
 */
public final class SourceReference {
    private final String value;

    /**
     * 创建 Source 引用，并冻结 Provider、图和解析路径共用的 canonical key。
     *
     * @param value Source 引用文本
     */
    public SourceReference(String value) {
        Objects.requireNonNull(value, "value");
        String checked = value.trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException("value must not be blank");
        }
        String canonical = canonicalize(checked);
        if (canonical.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "canonical value must not be blank");
        }
        this.value = canonical;
    }

    /**
     * 返回 Provider、SourceGraphEdge 和 ancestor stack 共用的规范引用键。
     */
    public String value() {
        return value;
    }

    /**
     * 在不隐藏安全证据的前提下规范 URI 或相对路径中的独立点段。
     */
    private static String canonicalize(String input) {
        try {
            URI uri = URI.create(input);
            if (uri.isOpaque()) {
                return canonicalOpaque(uri);
            }
            return canonicalHierarchical(uri);
        } catch (IllegalArgumentException invalidUri) {
            // 非法 URI 仍原样交给 SourcePolicy 拒绝，值对象不提前改变错误类型。
            return input;
        }
    }

    /**
     * 规范 opaque URI 的 scheme-specific part，例如 classpath:mix/%2e/a.xml。
     */
    private static String canonicalOpaque(URI uri) {
        String scheme = uri.getScheme() == null
                ? ""
                : uri.getScheme().toLowerCase(Locale.ROOT);
        String rawPart = valueOrEmpty(uri.getRawSchemeSpecificPart());
        int queryIndex = rawPart.indexOf('?');
        String pathPart = queryIndex < 0
                ? rawPart
                : rawPart.substring(0, queryIndex);
        String suffix = queryIndex < 0
                ? ""
                : rawPart.substring(queryIndex);
        StringBuilder result = new StringBuilder();
        if (!scheme.isEmpty()) {
            result.append(scheme).append(':');
        }
        result.append(removeCurrentDirectorySegments(pathPart)).append(suffix);
        if (uri.getRawFragment() != null) {
            result.append('#').append(uri.getRawFragment());
        }
        return result.toString();
    }

    /**
     * 规范 hierarchical URI 或相对路径，同时保留 authority、query 和 fragment。
     */
    private static String canonicalHierarchical(URI uri) {
        StringBuilder result = new StringBuilder();
        if (uri.getScheme() != null) {
            result.append(uri.getScheme().toLowerCase(Locale.ROOT)).append(':');
        }
        if (uri.getRawAuthority() != null) {
            result.append("//").append(uri.getRawAuthority());
        }
        result.append(removeCurrentDirectorySegments(
                valueOrEmpty(uri.getRawPath())));
        if (uri.getRawQuery() != null) {
            result.append('?').append(uri.getRawQuery());
        }
        if (uri.getRawFragment() != null) {
            result.append('#').append(uri.getRawFragment());
        }
        return result.toString();
    }

    /**
     * 逐个 raw segment 删除一次解码后恰好为 {@code .} 的当前目录段。
     *
     * <p>解码后为 {@code ..} 的 segment 保留原始文本，让 SourcePolicy 明确拒绝；
     * {@code %2F} 等编码分隔符不会在这里被解码，因此不能改变路径结构。</p>
     */
    private static String removeCurrentDirectorySegments(String value) {
        if (value.isEmpty()) {
            return value;
        }
        String[] segments = value.split("/", -1);
        List<String> output = new ArrayList<String>(segments.length);
        for (String segment : segments) {
            if (dotSegmentKind(segment) != DotSegmentKind.CURRENT_DIRECTORY) {
                output.add(segment);
            }
        }

        String result = joinSegments(output);
        if (result.isEmpty()) {
            // 绝对根仍为根；仅由当前目录段组成的相对引用统一保留为点。
            return value.startsWith("/") ? "/" : ".";
        }
        if (dotSegmentKind(segments[segments.length - 1])
                == DotSegmentKind.CURRENT_DIRECTORY
                && !result.endsWith("/")) {
            return result + '/';
        }
        return result;
    }

    /**
     * 判断 raw segment 一次解码后是单点、双点还是普通 segment。
     *
     * <p>这里只识别 ASCII 点及其百分号编码，不执行通用 URI 解码。</p>
     */
    private static DotSegmentKind dotSegmentKind(String rawSegment) {
        if (rawSegment.isEmpty()) {
            return DotSegmentKind.NOT_DOT;
        }
        int dotCount = 0;
        for (int index = 0; index < rawSegment.length(); index++) {
            char current = rawSegment.charAt(index);
            if (current == '.') {
                dotCount++;
                continue;
            }
            if (current != '%' || index + 2 >= rawSegment.length()) {
                return DotSegmentKind.NOT_DOT;
            }
            int high = hexValue(rawSegment.charAt(index + 1));
            int low = hexValue(rawSegment.charAt(index + 2));
            if (high < 0 || low < 0 || ((high << 4) + low) != '.') {
                return DotSegmentKind.NOT_DOT;
            }
            dotCount++;
            index += 2;
        }
        if (dotCount == 1) {
            return DotSegmentKind.CURRENT_DIRECTORY;
        }
        if (dotCount == 2) {
            return DotSegmentKind.PARENT_DIRECTORY;
        }
        return DotSegmentKind.NOT_DOT;
    }

    /**
     * 将十六进制字符转换为数值；非法字符返回 -1。
     */
    private static int hexValue(char value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        if (value >= 'a' && value <= 'f') {
            return value - 'a' + 10;
        }
        if (value >= 'A' && value <= 'F') {
            return value - 'A' + 10;
        }
        return -1;
    }

    /**
     * 使用原有斜杠边界连接 raw segment。
     */
    private static String joinSegments(List<String> segments) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < segments.size(); index++) {
            if (index > 0) {
                result.append('/');
            }
            result.append(segments.get(index));
        }
        return result.toString();
    }

    /**
     * 将可空 URI 组件转换为空字符串。
     */
    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof SourceReference
                && value.equals(((SourceReference) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "SourceReference{value='" + value + "'}";
    }

    /**
     * raw segment 一次解码后的点段类别；不携带运行时状态。
     */
    private enum DotSegmentKind {
        NOT_DOT,
        CURRENT_DIRECTORY,
        PARENT_DIRECTORY
    }
}
