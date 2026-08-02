package dec.core.compiler.source;

import dec.core.context.model.SourceRef;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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
        return parse(Objects.requireNonNull(source, "source"), true);
    }

    /**
     * 提取 systems 文档中的 rule-file 声明。
     */
    List<SourceGraphEdge> parseSystems(DocumentSource source) {
        return parse(Objects.requireNonNull(source, "source"), false);
    }

    /**
     * 使用关闭 DTD 和外部实体的 StAX，只识别 T03 冻结的完整声明路径。
     */
    private static List<SourceGraphEdge> parse(
            DocumentSource source,
            boolean rootDocument) {
        XMLInputFactory factory = secureFactory();
        List<SourceGraphEdge> edges = new ArrayList<SourceGraphEdge>();
        List<String> path = new ArrayList<String>();
        byte[] content = source.content();
        DeclarationLocator locator = new DeclarationLocator(content);
        XMLStreamReader reader = null;
        boolean rootSeen = false;
        try {
            reader = factory.createXMLStreamReader(
                    new ByteArrayInputStream(content),
                    "UTF-8");
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.DTD) {
                    throw new SourceDeclarationException(
                            "DTD is not allowed in source declarations");
                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String localName = reader.getLocalName();
                    path.add(localName);
                    if (path.size() == 1) {
                        validateDocumentRoot(localName, rootDocument);
                        rootSeen = true;
                    }
                    SourceEdgeType edgeType = rootDocument
                            ? rootEdgeType(path)
                            : systemEdgeType(path);
                    if (edgeType != null) {
                        edges.add(edge(
                                source,
                                edgeType,
                                requiredPath(reader),
                                reader.getLocation(),
                                localName,
                                path,
                                locator));
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT
                        && !path.isEmpty()) {
                    path.remove(path.size() - 1);
                }
            }
            if (!rootSeen) {
                throw new SourceDeclarationException(
                        "Source declaration document must contain a root element");
            }
            if (!rootDocument && edges.isEmpty()) {
                throw new SourceDeclarationException(
                        "Systems declaration must contain at least one rule-file");
            }
        } catch (XMLStreamException parseFailure) {
            throw new SourceDeclarationException(
                    "Unable to parse source declarations",
                    parseFailure);
        } finally {
            close(reader);
        }
        Collections.sort(edges);
        return Collections.unmodifiableList(edges);
    }

    /**
     * 验证两类声明文档拥有各自冻结的根元素，防止错误文档提升声明权限。
     */
    private static void validateDocumentRoot(
            String localName,
            boolean rootDocument) {
        String expected = rootDocument ? "orm-config" : "systems";
        if (!expected.equals(localName)) {
            throw new SourceDeclarationException(
                    "Unexpected source declaration root: " + localName);
        }
    }

    /**
     * 创建禁止 DTD、外部实体和外部资源解析的 StAX 工厂。
     */
    private static XMLInputFactory secureFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        setProperty(factory, XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        setProperty(
                factory,
                "javax.xml.stream.isSupportingExternalEntities",
                Boolean.FALSE);
        setProperty(
                factory,
                XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
                Boolean.FALSE);
        factory.setXMLResolver(new XMLResolver() {
            @Override
            public Object resolveEntity(
                    String publicId,
                    String systemId,
                    String baseUri,
                    String namespace) throws XMLStreamException {
                throw new XMLStreamException(
                        "External resources are not allowed in source declarations");
            }
        });
        return factory;
    }

    /**
     * 设置安全属性；不支持关键安全属性时立即阻断，而不是静默降级。
     */
    private static void setProperty(
            XMLInputFactory factory,
            String property,
            Object value) {
        try {
            factory.setProperty(property, value);
        } catch (IllegalArgumentException unsupported) {
            throw new SourceDeclarationException(
                    "Required XML security property is unavailable: " + property,
                    unsupported);
        }
    }

    /**
     * 根据完整元素栈识别 root 文档中的四类直接声明。
     */
    private static SourceEdgeType rootEdgeType(List<String> path) {
        if (matchesPath(
                path,
                "orm-config",
                "orm-data-file-info",
                "orm-file")) {
            return SourceEdgeType.ROOT_DATA_FILESET;
        }
        if (matchesPath(
                path,
                "orm-config",
                "orm-view-file-info",
                "orm-file")) {
            return SourceEdgeType.ROOT_VIEW_FILESET;
        }
        if (matchesPath(
                path,
                "orm-config",
                "system-file-info",
                "system-file")) {
            return SourceEdgeType.ROOT_SYSTEM_FILE;
        }
        if (matchesPath(
                path,
                "orm-config",
                "business-file-info",
                "business-file")) {
            return SourceEdgeType.ROOT_BUSINESS_FILE;
        }
        return null;
    }

    /**
     * 根据完整元素栈识别 systems 文档中的 rule-file 声明。
     */
    private static SourceEdgeType systemEdgeType(List<String> path) {
        return matchesPath(
                path,
                "systems",
                "system",
                "rule-file-info",
                "rule-file")
                ? SourceEdgeType.SYSTEM_RULE_FILE
                : null;
    }

    /**
     * 精确比较当前 local-name 元素栈，不允许额外祖先、错误嵌套或后缀匹配。
     */
    private static boolean matchesPath(
            List<String> path,
            String... expected) {
        if (path.size() != expected.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index++) {
            if (!expected[index].equals(path.get(index))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 校验声明节点必须提供非空 path 属性。
     */
    private static String requiredPath(XMLStreamReader reader) {
        String value = reader.getAttributeValue(null, "path");
        if (value == null || value.trim().isEmpty()) {
            throw new SourceDeclarationException(
                    "Source declaration path must not be blank");
        }
        return value.trim();
    }

    /**
     * 创建带声明元素 `<` 精确起始位置的不可变 SourceGraphEdge。
     */
    private static SourceGraphEdge edge(
            DocumentSource source,
            SourceEdgeType edgeType,
            String target,
            Location location,
            String localName,
            List<String> path,
            DeclarationLocator locator) {
        Position position = locator.locate(location, localName);
        return new SourceGraphEdge(
                edgeType,
                source.sourceId(),
                new SourceReference(target),
                new SourceRef(
                        source.sourceId(),
                        position.line(),
                        position.column(),
                        nodePath(path)));
    }

    /**
     * 将当前 XML 元素栈转换为稳定节点路径。
     */
    private static String nodePath(List<String> path) {
        StringBuilder result = new StringBuilder();
        for (String element : path) {
            result.append('/').append(element);
        }
        return result.toString();
    }

    /**
     * 安全关闭 StAX reader；关闭异常不覆盖原始解析失败。
     */
    private static void close(XMLStreamReader reader) {
        if (reader == null) {
            return;
        }
        try {
            reader.close();
        } catch (XMLStreamException ignored) {
            // 关闭失败不改变已确定的 Source discovery 语义结果。
        }
    }

    /**
     * 基于原始 UTF-8 文本定位当前 start tag 的 `<`，不采用 StAX 标签末尾列号。
     */
    private static final class DeclarationLocator {
        private final String source;
        private final List<Integer> lineStarts;

        private DeclarationLocator(byte[] content) {
            this.source = new String(content, StandardCharsets.UTF_8);
            this.lineStarts = lineStarts(source);
        }

        /**
         * 以 StAX character offset 为上界反向寻找当前元素 start tag。
         */
        private Position locate(Location location, String localName) {
            int upperBound = location == null
                    ? source.length()
                    : location.getCharacterOffset();
            if (upperBound < 0 || upperBound > source.length()) {
                upperBound = lineEnd(location == null
                        ? lineStarts.size()
                        : location.getLineNumber());
            }
            int offset = findStartTag(upperBound, localName);
            if (offset < 0 && location != null) {
                offset = findStartTag(lineEnd(location.getLineNumber()), localName);
            }
            if (offset < 0) {
                throw new SourceDeclarationException(
                        "Unable to locate declaration start tag: " + localName);
            }
            return position(offset);
        }

        /**
         * 从上界向前查找名称匹配的最近 start tag。
         */
        private int findStartTag(int upperBound, String localName) {
            int cursor = Math.min(Math.max(upperBound, 0), source.length()) - 1;
            while (cursor >= 0) {
                int candidate = source.lastIndexOf('<', cursor);
                if (candidate < 0) {
                    return -1;
                }
                if (matchesStartTag(candidate, localName)) {
                    return candidate;
                }
                cursor = candidate - 1;
            }
            return -1;
        }

        /**
         * 判断 `<` 后的 qualified name 是否对应当前 local name。
         */
        private boolean matchesStartTag(int offset, String localName) {
            int nameStart = offset + 1;
            if (nameStart >= source.length()) {
                return false;
            }
            char first = source.charAt(nameStart);
            if (first == '/' || first == '!' || first == '?') {
                return false;
            }
            int nameEnd = nameStart;
            while (nameEnd < source.length()) {
                char current = source.charAt(nameEnd);
                if (Character.isWhitespace(current)
                        || current == '>'
                        || current == '/') {
                    break;
                }
                nameEnd++;
            }
            if (nameEnd <= nameStart) {
                return false;
            }
            String qualifiedName = source.substring(nameStart, nameEnd);
            int colon = qualifiedName.lastIndexOf(':');
            String actualLocalName = colon < 0
                    ? qualifiedName
                    : qualifiedName.substring(colon + 1);
            return localName.equals(actualLocalName);
        }

        /**
         * 将字符偏移转换为 1-based 行、列。
         */
        private Position position(int offset) {
            int lineIndex = 0;
            for (int index = 1; index < lineStarts.size(); index++) {
                if (lineStarts.get(index) > offset) {
                    break;
                }
                lineIndex = index;
            }
            return new Position(
                    lineIndex + 1,
                    offset - lineStarts.get(lineIndex) + 1);
        }

        /**
         * 返回指定 1-based 行的结束偏移。
         */
        private int lineEnd(int line) {
            int checkedLine = Math.max(1, line);
            if (checkedLine >= lineStarts.size()) {
                return source.length();
            }
            return lineStarts.get(checkedLine);
        }

        /**
         * 建立兼容 LF、CRLF 和 CR 的行首索引。
         */
        private static List<Integer> lineStarts(String source) {
            List<Integer> starts = new ArrayList<Integer>();
            starts.add(0);
            for (int index = 0; index < source.length(); index++) {
                char current = source.charAt(index);
                if (current == '\r') {
                    if (index + 1 < source.length()
                            && source.charAt(index + 1) == '\n') {
                        index++;
                    }
                    starts.add(index + 1);
                } else if (current == '\n') {
                    starts.add(index + 1);
                }
            }
            return starts;
        }
    }

    /**
     * 声明元素起始位置值对象。
     */
    private static final class Position {
        private final int line;
        private final int column;

        private Position(int line, int column) {
            this.line = line;
            this.column = column;
        }

        private int line() {
            return line;
        }

        private int column() {
            return column;
        }
    }

    /**
     * 声明解析失败的内部受控异常，由 Resolver 映射为稳定 Diagnostic。
     */
    static final class SourceDeclarationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        SourceDeclarationException(String message) {
            super(message);
        }

        SourceDeclarationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
