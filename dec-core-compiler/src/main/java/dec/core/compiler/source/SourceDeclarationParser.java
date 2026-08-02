package dec.core.compiler.source;

import dec.core.context.model.SourceRef;
import java.io.ByteArrayInputStream;
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
     * 使用关闭 DTD 和外部实体的 StAX，只识别 T03 允许的 Source 声明节点。
     */
    private static List<SourceGraphEdge> parse(
            DocumentSource source,
            boolean rootDocument) {
        XMLInputFactory factory = secureFactory();
        List<SourceGraphEdge> edges = new ArrayList<SourceGraphEdge>();
        List<String> path = new ArrayList<String>();
        XMLStreamReader reader = null;
        try {
            reader = factory.createXMLStreamReader(
                    new ByteArrayInputStream(source.content()),
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
                    SourceEdgeType edgeType = rootDocument
                            ? rootEdgeType(path)
                            : systemEdgeType(path);
                    if (edgeType != null) {
                        edges.add(edge(
                                source,
                                edgeType,
                                requiredPath(reader),
                                reader.getLocation(),
                                path));
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT
                        && !path.isEmpty()) {
                    path.remove(path.size() - 1);
                }
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
     * 根据完整节点路径识别 root 文档中的四类声明。
     */
    private static SourceEdgeType rootEdgeType(List<String> path) {
        if (endsWith(path, "orm-data-file-info", "orm-file")) {
            return SourceEdgeType.ROOT_DATA_FILESET;
        }
        if (endsWith(path, "orm-view-file-info", "orm-file")) {
            return SourceEdgeType.ROOT_VIEW_FILESET;
        }
        if (endsWith(path, "system-file-info", "system-file")) {
            return SourceEdgeType.ROOT_SYSTEM_FILE;
        }
        if (endsWith(path, "business-file-info", "business-file")) {
            return SourceEdgeType.ROOT_BUSINESS_FILE;
        }
        return null;
    }

    /**
     * 根据完整节点路径识别 systems 文档中的 rule-file 声明。
     */
    private static SourceEdgeType systemEdgeType(List<String> path) {
        return endsWith(path, "rule-file-info", "rule-file")
                ? SourceEdgeType.SYSTEM_RULE_FILE
                : null;
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
     * 创建带精确声明位置的不可变 SourceGraphEdge。
     */
    private static SourceGraphEdge edge(
            DocumentSource source,
            SourceEdgeType edgeType,
            String target,
            Location location,
            List<String> path) {
        int line = Math.max(1, location == null ? 1 : location.getLineNumber());
        int column = Math.max(1, location == null ? 1 : location.getColumnNumber());
        return new SourceGraphEdge(
                edgeType,
                source.sourceId(),
                new SourceReference(target),
                new SourceRef(
                        source.sourceId(),
                        line,
                        column,
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
     * 判断当前元素栈是否以给定父子节点结尾。
     */
    private static boolean endsWith(
            List<String> path,
            String parent,
            String child) {
        int size = path.size();
        return size >= 2
                && parent.equals(path.get(size - 2))
                && child.equals(path.get(size - 1));
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
