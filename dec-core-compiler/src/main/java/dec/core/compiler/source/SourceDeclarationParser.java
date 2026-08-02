package dec.core.compiler.source;

import java.util.List;
import java.util.Objects;

/**
 * 只提取 Source 声明边的最小安全 XML 解析边界。
 *
 * <p>该类型不是 Canonical Frontend，不解释业务节点，也不登记定义。</p>
 */
final class SourceDeclarationParser {
    /**
     * 提取 root 文档中的 data、view、system 和 business 声明。
     */
    List<SourceGraphEdge> parseRoot(DocumentSource source) {
        Objects.requireNonNull(source, "source");
        throw new AssertionError("Architecture skeleton only");
    }

    /**
     * 提取 systems 文档中的 rule-file 声明。
     */
    List<SourceGraphEdge> parseSystems(DocumentSource source) {
        Objects.requireNonNull(source, "source");
        throw new AssertionError("Architecture skeleton only");
    }
}
