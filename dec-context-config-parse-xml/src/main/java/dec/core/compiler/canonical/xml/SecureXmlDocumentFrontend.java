package dec.core.compiler.canonical.xml;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendOptions;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendResults;
import dec.core.compiler.source.DocumentSource;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * 将 XML DocumentSource 安全转换为格式中立 CanonicalDocumentNode。
 *
 * <p>该 Frontend 只读取调用方提供的文档字节，不读取全局 Schema、网络、
 * 文件系统或旧 Config 状态，也不登记业务定义和运行时对象。</p>
 */
public final class SecureXmlDocumentFrontend implements DocumentFrontend {
    private static final String XML_SCHEMA_INSTANCE_NAMESPACE =
            "http://www.w3.org/2001/XMLSchema-instance";
    private static final String PASS = "xml-frontend";
    private final Consumer<String> externalAccessObserver;
    private final XmlFrontendLimits limits;

    /**
     * 创建生产使用的安全 XML Frontend，并使用 Design R19 冻结预算。
     */
    public SecureXmlDocumentFrontend() {
        this(new Consumer<String>() {
            @Override
            public void accept(String location) {
                // 生产模式不记录被拒绝的外部位置，避免额外状态和信息泄漏。
            }
        }, XmlFrontendLimits.production());
    }

    /**
     * 创建带外部访问探针的 Frontend，供同包安全测试证明零副作用边界。
     *
     * @param externalAccessObserver 仅在解析器尝试解析外部资源时调用的探针
     */
    SecureXmlDocumentFrontend(Consumer<String> externalAccessObserver) {
        this(externalAccessObserver, XmlFrontendLimits.production());
    }

    /**
     * 创建带可注入预算的同包测试 Frontend。
     *
     * @param externalAccessObserver 外部资源解析尝试探针
     * @param limits 当前解析使用的不可变资源预算
     */
    SecureXmlDocumentFrontend(
            Consumer<String> externalAccessObserver,
            XmlFrontendLimits limits) {
        this.externalAccessObserver = Objects.requireNonNull(
                externalAccessObserver,
                "externalAccessObserver");
        this.limits = Objects.requireNonNull(limits, "limits");
    }

    /**
     * 返回该 Frontend 唯一支持的 XML 格式。
     */
    @Override
    public DocumentFormat format() {
        return DocumentFormat.XML;
    }

    /**
     * 在受控失败边界内解析 XML，任何失败均不发布部分 Canonical 根。
     *
     * @param source Provider 返回的不可变 XML 文档源
     * @param options 当前 Session 的显式 Frontend 选项
     * @return PARSED 或不携带部分根的 FAILED 结果
     */
    @Override
    public FrontendResult parse(DocumentSource source, FrontendOptions options) {
        SourceRef fallback = fallbackRef(source);
        if (source == null) {
            return failed(
                    "xml.frontend.source.missing",
                    fallback,
                    "传入非空 XML DocumentSource");
        }
        if (options == null) {
            return failed(
                    "xml.frontend.options.missing",
                    fallback,
                    "传入非空 FrontendOptions");
        }
        if (source.format() != DocumentFormat.XML) {
            return failed(
                    "xml.frontend.format.unsupported",
                    fallback,
                    "为 XML Frontend 提供 DocumentFormat.XML 文档");
        }

        XMLStreamReader reader = null;
        try {
            byte[] content = source.content();
            XMLInputFactory factory = secureFactory();
            reader = factory.createXMLStreamReader(
                    new ByteArrayInputStream(content),
                    StandardCharsets.UTF_8.name());
            CanonicalDocumentNode root = parseDocument(
                    reader,
                    source,
                    options,
                    content);
            return FrontendResults.parsed(
                    root,
                    Collections.<Diagnostic>emptyList());
        } catch (XmlFrontendFailure failure) {
            return failed(
                    failure.messageKey(),
                    failure.sourceRef(),
                    failure.recoveryHint());
        } catch (XMLStreamException failure) {
            return failed(
                    "xml.frontend.malformed",
                    streamRef(source, failure.getLocation()),
                    "修复 XML 结构并确保文档完整闭合");
        } catch (RuntimeException failure) {
            return failed(
                    "xml.frontend.unexpected",
                    fallback,
                    "检查 XML 内容、解析器安全能力和 Canonical 输入合同");
        } finally {
            close(reader);
        }
    }

    /**
     * 创建关闭 DTD、实体替换和外部资源解析的 StAX 工厂。
     */
    private XMLInputFactory secureFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        setRequiredProperty(factory, XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        setRequiredProperty(
                factory,
                "javax.xml.stream.isSupportingExternalEntities",
                Boolean.FALSE);
        setRequiredProperty(
                factory,
                XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
                Boolean.FALSE);
        setRequiredProperty(
                factory,
                XMLInputFactory.IS_NAMESPACE_AWARE,
                Boolean.TRUE);
        factory.setXMLResolver(new XMLResolver() {
            @Override
            public Object resolveEntity(
                    String publicId,
                    String systemId,
                    String baseUri,
                    String namespace) throws XMLStreamException {
                String location = systemId == null ? "<unknown>" : systemId;
                externalAccessObserver.accept(location);
                throw new XMLStreamException(
                        "External XML resources are not allowed");
            }
        });
        return factory;
    }

    /**
     * 设置关键安全属性；运行时不支持时立即失败，禁止静默降级。
     */
    private static void setRequiredProperty(
            XMLInputFactory factory,
            String property,
            Object value) {
        try {
            factory.setProperty(property, value);
        } catch (IllegalArgumentException unsupported) {
            throw new XmlFrontendFailure(
                    "xml.frontend.security.property.unsupported",
                    new SourceRef("<xml-parser>", 0, 0, "/"),
                    "使用支持必需 StAX 安全属性的 XMLInputFactory",
                    unsupported);
        }
    }

    /**
     * 单次扫描构建完整 Canonical 树，完成前不向调用方发布任何节点。
     */
    private static CanonicalDocumentNode parseDocument(
            XMLStreamReader reader,
            DocumentSource source,
            FrontendOptions options,
            byte[] content) throws XMLStreamException {
        List<NodeBuilder> stack = new ArrayList<NodeBuilder>();
        CanonicalDocumentNode root = null;
        StartTagLocator locator = new StartTagLocator(content);
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.DTD) {
                throw unsafe(
                        "xml.frontend.doctype.forbidden",
                        streamRef(source, reader.getLocation()),
                        "移除 DOCTYPE 和实体声明");
            }
            if (event == XMLStreamConstants.ENTITY_REFERENCE) {
                throw unsafe(
                        "xml.frontend.entity.forbidden",
                        streamRef(source, reader.getLocation()),
                        "移除 XML 实体引用");
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                Position position = locator.locate(reader.getLocation(), localName);
                SourceRef sourceRef = new SourceRef(
                        source.sourceId(),
                        position.line(),
                        position.column(),
                        nodePath(stack, localName));
                stack.add(new NodeBuilder(
                        localName,
                        attributes(reader, sourceRef),
                        sourceRef));
            } else if ((event == XMLStreamConstants.CHARACTERS
                    || event == XMLStreamConstants.CDATA
                    || event == XMLStreamConstants.SPACE)
                    && !stack.isEmpty()) {
                stack.get(stack.size() - 1).appendText(reader.getText());
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (stack.isEmpty()) {
                    throw unsafe(
                            "xml.frontend.structure.invalid",
                            streamRef(source, reader.getLocation()),
                            "修复 XML 元素嵌套结构");
                }
                NodeBuilder completed = stack.remove(stack.size() - 1);
                CanonicalDocumentNode node = completed.build(options.schemaVersion());
                if (stack.isEmpty()) {
                    if (root != null) {
                        throw unsafe(
                                "xml.frontend.multiple.roots",
                                completed.sourceRef(),
                                "确保 XML 仅包含一个根元素");
                    }
                    root = node;
                } else {
                    stack.get(stack.size() - 1).addChild(node);
                }
            }
        }
        if (!stack.isEmpty() || root == null) {
            throw unsafe(
                    "xml.frontend.root.missing",
                    fallbackRef(source),
                    "提供包含唯一根元素的完整 XML 文档");
        }
        return root;
    }

    /**
     * 读取业务属性并拒绝外部 Schema 指示与 local-name 冲突。
     */
    private static Map<String, String> attributes(
            XMLStreamReader reader,
            SourceRef sourceRef) {
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        for (int index = 0; index < reader.getAttributeCount(); index++) {
            String namespace = reader.getAttributeNamespace(index);
            String localName = reader.getAttributeLocalName(index);
            if (XML_SCHEMA_INSTANCE_NAMESPACE.equals(namespace)
                    && ("schemaLocation".equals(localName)
                    || "noNamespaceSchemaLocation".equals(localName))) {
                throw unsafe(
                        "xml.frontend.external.schema.forbidden",
                        sourceRef,
                        "移除 xsi:schemaLocation 和 xsi:noNamespaceSchemaLocation");
            }
            if (attributes.containsKey(localName)) {
                throw unsafe(
                        "xml.frontend.attribute.local-name.duplicate",
                        sourceRef,
                        "避免不同命名空间属性使用相同 local-name");
            }
            attributes.put(localName, reader.getAttributeValue(index));
        }
        return attributes;
    }

    /**
     * 根据当前未闭合节点和新节点名称生成完整 local-name 路径。
     */
    private static String nodePath(List<NodeBuilder> stack, String localName) {
        StringBuilder path = new StringBuilder();
        for (NodeBuilder node : stack) {
            path.append('/').append(node.name());
        }
        return path.append('/').append(localName).toString();
    }

    /**
     * 创建稳定 XML 安全 Diagnostic 的失败结果。
     */
    private static FrontendResult failed(
            String messageKey,
            SourceRef sourceRef,
            String recoveryHint) {
        Diagnostic diagnostic = new Diagnostic(
                DiagnosticCode.MIX_FRONTEND_XML_UNSAFE,
                DiagnosticSeverity.ERROR,
                messageKey,
                null,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                recoveryHint,
                PASS);
        return FrontendResults.failed(Collections.singletonList(diagnostic));
    }

    /**
     * 创建携带来源和恢复建议的内部受控失败。
     */
    private static XmlFrontendFailure unsafe(
            String messageKey,
            SourceRef sourceRef,
            String recoveryHint) {
        return new XmlFrontendFailure(
                messageKey,
                sourceRef,
                recoveryHint,
                null);
    }

    /**
     * 创建参数和未知解析错误使用的稳定回退 SourceRef。
     */
    private static SourceRef fallbackRef(DocumentSource source) {
        String sourceId = source == null ? "<unknown-xml-source>" : source.sourceId();
        return new SourceRef(sourceId, 0, 0, "/");
    }

    /**
     * 使用 StAX 报告位置创建非负 SourceRef，作为 malformed XML 诊断位置。
     */
    private static SourceRef streamRef(DocumentSource source, Location location) {
        int line = location == null ? 0 : Math.max(0, location.getLineNumber());
        int column = location == null ? 0 : Math.max(0, location.getColumnNumber());
        return new SourceRef(source.sourceId(), line, column, "/");
    }

    /**
     * 安全关闭 reader；关闭失败不得覆盖已确定的解析结果。
     */
    private static void close(XMLStreamReader reader) {
        if (reader == null) {
            return;
        }
        try {
            reader.close();
        } catch (XMLStreamException ignored) {
            // 关闭异常不改变成功或失败的 Canonical 语义。
        }
    }

    /**
     * 未完成元素的隔离可变构建器，只在完整关闭后生成不可变节点。
     */
    private static final class NodeBuilder {
        private final String name;
        private final Map<String, String> attributes;
        private final SourceRef sourceRef;
        private final List<CanonicalDocumentNode> children =
                new ArrayList<CanonicalDocumentNode>();
        private final StringBuilder text = new StringBuilder();

        private NodeBuilder(
                String name,
                Map<String, String> attributes,
                SourceRef sourceRef) {
            this.name = name;
            this.attributes = attributes;
            this.sourceRef = sourceRef;
        }

        private String name() {
            return name;
        }

        private SourceRef sourceRef() {
            return sourceRef;
        }

        private void appendText(String value) {
            text.append(value);
        }

        private void addChild(CanonicalDocumentNode child) {
            children.add(child);
        }

        /**
         * 将当前元素冻结为不可变 Canonical 节点。
         */
        private CanonicalDocumentNode build(String schemaVersion) {
            String normalizedText = text.toString().trim();
            Optional<String> scalar = normalizedText.isEmpty()
                    ? Optional.<String>empty()
                    : Optional.of(normalizedText);
            return new CanonicalDocumentNode(
                    name,
                    attributes,
                    scalar,
                    children,
                    sourceRef,
                    DocumentFormat.XML,
                    schemaVersion);
        }
    }

    /**
     * 基于原始 UTF-8 文本定位 start tag 的小于号，避免使用标签末尾列号。
     */
    private static final class StartTagLocator {
        private final String source;
        private final List<Integer> lineStarts;

        private StartTagLocator(byte[] content) {
            this.source = new String(content, StandardCharsets.UTF_8);
            this.lineStarts = lineStarts(source);
        }

        /**
         * 使用 StAX character offset 作为上界反向定位当前 start tag。
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
                throw unsafe(
                        "xml.frontend.source.position.unavailable",
                        new SourceRef("<xml-source>", 0, 0, "/"),
                        "检查 XML 编码和 start tag 位置");
            }
            return position(offset);
        }

        /**
         * 从指定上界向前寻找名称匹配的最近 start tag。
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
         * 判断 qualified-name 的 local-name 是否与当前 StAX 事件一致。
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
     * start tag 的 1-based 行列值对象。
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
     * 内部受控失败，确保调用方只看到稳定 FrontendResult。
     */
    private static final class XmlFrontendFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final String messageKey;
        private final SourceRef sourceRef;
        private final String recoveryHint;

        private XmlFrontendFailure(
                String messageKey,
                SourceRef sourceRef,
                String recoveryHint,
                Throwable cause) {
            super(messageKey, cause);
            this.messageKey = Objects.requireNonNull(messageKey, "messageKey");
            this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
            this.recoveryHint = Objects.requireNonNull(
                    recoveryHint,
                    "recoveryHint");
        }

        private String messageKey() {
            return messageKey;
        }

        private SourceRef sourceRef() {
            return sourceRef;
        }

        private String recoveryHint() {
            return recoveryHint;
        }
    }
}
