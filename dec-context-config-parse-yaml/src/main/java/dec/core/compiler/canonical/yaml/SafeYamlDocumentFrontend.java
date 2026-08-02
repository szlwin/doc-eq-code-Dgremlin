package dec.core.compiler.canonical.yaml;

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
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.inspector.UnTrustedTagInspector;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * 将不可信 YAML 文档转换为 compiler-owned Canonical 树的安全 Frontend。
 *
 * <p>实现只使用 SnakeYAML compose 表示树，不调用通用对象加载入口；
 * 原始字节、tag 来源事实、图结构、路径和资源预算均在发布根之前完成校验。</p>
 */
public final class SafeYamlDocumentFrontend implements DocumentFrontend {
    private static final String PASS = "yaml-frontend";
    private static final String ATTRIBUTES_KEY = "@attributes";
    private static final String TEXT_KEY = "#text";
    private static final String MERGE_KEY = "<<";

    private final YamlFrontendLimits limits;

    /**
     * 使用 Design R20/R21 冻结的生产预算创建 YAML Frontend。
     */
    public SafeYamlDocumentFrontend() {
        this(YamlFrontendLimits.production());
    }

    /**
     * 使用显式小型预算创建 Frontend，仅供同包资源 Oracle 使用。
     *
     * @param limits 不可变 YAML 资源预算
     */
    SafeYamlDocumentFrontend(YamlFrontendLimits limits) {
        this.limits = Objects.requireNonNull(limits, "limits");
    }

    /**
     * 当前 Frontend 唯一支持 YAML 文档。
     */
    @Override
    public DocumentFormat format() {
        return DocumentFormat.YAML;
    }

    /**
     * 使用严格 UTF-8 和安全 compose 表示树解析 YAML。
     *
     * @param source Provider 返回的不可变 YAML Source
     * @param options 当前 Session 的显式 Frontend 选项
     * @return Canonical 成功结果或稳定 YAML 安全失败结果
     */
    @Override
    public FrontendResult parse(
            DocumentSource source,
            FrontendOptions options) {
        if (source == null) {
            return failed(null, "yaml.frontend.source.required", null, "/");
        }
        if (options == null) {
            return failed(source, "yaml.frontend.options.required", null, "/");
        }
        if (source.format() != DocumentFormat.YAML) {
            return failed(source, "yaml.frontend.format.unsupported", null, "/");
        }

        byte[] content = source.content();
        if (content.length > limits.maxDocumentBytes()) {
            return failed(source, "yaml.frontend.limit.document-bytes", null, "/");
        }

        try {
            String yamlText = decodeUtf8(content);
            LoaderOptions loaderOptions = loaderOptions();
            Yaml yaml = new Yaml(new SafeConstructor(loaderOptions));
            Iterator<Node> documents = yaml.composeAll(
                    new StringReader(yamlText)).iterator();
            if (!documents.hasNext()) {
                throw unsafe("yaml.frontend.document.empty", null, "/");
            }
            Node document = documents.next();
            if (document == null) {
                throw unsafe("yaml.frontend.document.empty", null, "/");
            }
            if (documents.hasNext()) {
                throw unsafe("yaml.frontend.document.multiple", null, "/");
            }

            BuildContext context = new BuildContext(
                    source,
                    options.schemaVersion(),
                    limits);
            CanonicalDocumentNode root = context.buildDocument(document);
            return FrontendResults.parsed(
                    root,
                    Collections.<Diagnostic>emptyList());
        } catch (YamlUnsafeException failure) {
            return failed(
                    source,
                    failure.messageKey(),
                    failure.mark(),
                    failure.nodePath());
        } catch (RuntimeException failure) {
            return failed(source, "yaml.frontend.parse.failed", null, "/");
        }
    }

    /**
     * 使用 REPORT 策略严格解码 UTF-8，禁止替换字符掩盖非法原始字节。
     */
    private static String decodeUtf8(byte[] content) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(content))
                    .toString();
        } catch (CharacterCodingException failure) {
            throw unsafe(
                    "yaml.frontend.encoding.invalid-utf8",
                    null,
                    "/");
        }
    }

    /**
     * 创建 fail-closed 的 SnakeYAML LoaderOptions。
     *
     * <p>Parser 限制之外，Canonical 遍历仍会再次验证 tag、图和预算。</p>
     */
    private LoaderOptions loaderOptions() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setAllowRecursiveKeys(false);
        options.setMaxAliasesForCollections(
                limits.maxAliasesForCollections());
        options.setNestingDepthLimit(limits.maxNestingDepth());
        options.setCodePointLimit(limits.maxCodePoints());
        options.setTagInspector(new UnTrustedTagInspector());
        return options;
    }

    /**
     * 创建不携带部分 Canonical root 的稳定 YAML 安全失败结果。
     */
    private static FrontendResult failed(
            DocumentSource source,
            String messageKey,
            Mark mark,
            String nodePath) {
        String sourceId = source == null
                ? "<unknown-yaml-source>"
                : source.sourceId();
        int line = mark == null ? 0 : mark.getLine() + 1;
        int column = mark == null ? 0 : mark.getColumn() + 1;
        String path = nodePath == null || nodePath.trim().isEmpty()
                ? "/"
                : nodePath;
        SourceRef sourceRef = new SourceRef(sourceId, line, column, path);
        Diagnostic diagnostic = new Diagnostic(
                DiagnosticCode.MIX_FRONTEND_YAML_UNSAFE,
                DiagnosticSeverity.ERROR,
                messageKey,
                null,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                "请提供符合 Design R21 来源事实与安全合同的 YAML 文档",
                PASS);
        return FrontendResults.failed(Collections.singletonList(diagnostic));
    }

    /**
     * 创建携带稳定 messageKey、位置和路径的内部安全失败。
     */
    private static YamlUnsafeException unsafe(
            String messageKey,
            Mark mark,
            String nodePath) {
        return new YamlUnsafeException(messageKey, mark, nodePath);
    }

    /**
     * 单次解析的资源统计、图结构检查和 Canonical 构建上下文。
     */
    private static final class BuildContext {
        private final DocumentSource source;
        private final String schemaVersion;
        private final YamlFrontendLimits limits;
        private final Set<Node> visited = Collections.newSetFromMap(
                new IdentityHashMap<Node, Boolean>());
        private final Set<Node> active = Collections.newSetFromMap(
                new IdentityHashMap<Node, Boolean>());

        private int nodeCount;
        private long cumulativeNodePathChars;
        private long cumulativeScalarChars;

        private BuildContext(
                DocumentSource source,
                String schemaVersion,
                YamlFrontendLimits limits) {
            this.source = Objects.requireNonNull(source, "source");
            this.schemaVersion = Objects.requireNonNull(
                    schemaVersion,
                    "schemaVersion");
            this.limits = Objects.requireNonNull(limits, "limits");
        }

        /**
         * 校验单文档根并从根 key 构建唯一 Canonical 根。
         */
        private CanonicalDocumentNode buildDocument(Node document) {
            enter(document, "/");
            try {
                requireTag(document, Tag.MAP, "/");
                if (!(document instanceof MappingNode)) {
                    throw unsafe(
                            "yaml.frontend.document.root-mapping-required",
                            document.getStartMark(),
                            "/");
                }
                MappingNode rootMapping = (MappingNode) document;
                requireMappingSize(rootMapping, "/");
                if (rootMapping.getValue().size() != 1) {
                    throw unsafe(
                            "yaml.frontend.document.single-root-required",
                            document.getStartMark(),
                            "/");
                }
                NodeTuple rootTuple = rootMapping.getValue().get(0);
                String rootName = readKey(rootTuple.getKeyNode(), "/");
                if (isReservedKey(rootName)) {
                    throw unsafe(
                            "yaml.frontend.document.reserved-root",
                            rootTuple.getKeyNode().getStartMark(),
                            "/");
                }
                return buildElement(
                        rootName,
                        rootTuple.getValueNode(),
                        rootTuple.getKeyNode().getStartMark(),
                        "",
                        1);
            } finally {
                leave(document);
            }
        }

        /**
         * 构建一个 Canonical 节点，并在路径及集合分配前执行名称和预算检查。
         */
        private CanonicalDocumentNode buildElement(
                String name,
                Node body,
                Mark declarationMark,
                String parentPath,
                int depth) {
            String normalizedName = requireName(
                    name,
                    declarationMark,
                    parentPath);
            if (depth > limits.maxNestingDepth()) {
                throw unsafe(
                        "yaml.frontend.limit.nesting-depth",
                        declarationMark,
                        parentPath);
            }
            String nodePath = parentPath.isEmpty()
                    ? "/" + normalizedName
                    : parentPath + "/" + normalizedName;
            reserveNode(nodePath, declarationMark);

            Map<String, String> attributes =
                    new LinkedHashMap<String, String>();
            Optional<String> scalar = Optional.empty();
            List<CanonicalDocumentNode> children =
                    new ArrayList<CanonicalDocumentNode>();

            enter(body, nodePath);
            try {
                if (body instanceof ScalarNode) {
                    scalar = readScalarEntered((ScalarNode) body, nodePath);
                } else if (body instanceof MappingNode) {
                    MappingNode mapping = (MappingNode) body;
                    requireTag(mapping, Tag.MAP, nodePath);
                    requireMappingSize(mapping, nodePath);
                    NodeContent content = readMapping(
                            mapping,
                            nodePath,
                            depth);
                    attributes.putAll(content.attributes());
                    scalar = content.scalar();
                    children.addAll(content.children());
                } else if (body instanceof SequenceNode) {
                    throw unsafe(
                            "yaml.frontend.node.sequence-body-unsupported",
                            body.getStartMark(),
                            nodePath);
                } else {
                    throw unsafe(
                            "yaml.frontend.node.unsupported",
                            body.getStartMark(),
                            nodePath);
                }
            } finally {
                leave(body);
            }

            return new CanonicalDocumentNode(
                    normalizedName,
                    attributes,
                    scalar,
                    children,
                    sourceRef(declarationMark, nodePath),
                    DocumentFormat.YAML,
                    schemaVersion);
        }

        /**
         * 读取 Mapping body 中的属性、直接 scalar 和保持顺序的子节点。
         */
        private NodeContent readMapping(
                MappingNode mapping,
                String nodePath,
                int depth) {
            Map<String, String> attributes =
                    new LinkedHashMap<String, String>();
            Optional<String> scalar = Optional.empty();
            List<CanonicalDocumentNode> children =
                    new ArrayList<CanonicalDocumentNode>();
            Set<String> keys = new HashSet<String>();
            boolean attributesSeen = false;
            boolean textSeen = false;

            for (NodeTuple tuple : mapping.getValue()) {
                String key = readKey(tuple.getKeyNode(), nodePath);
                if (!keys.add(key)) {
                    throw unsafe(
                            "yaml.frontend.mapping.duplicate-key",
                            tuple.getKeyNode().getStartMark(),
                            nodePath);
                }
                if (MERGE_KEY.equals(key)) {
                    throw unsafe(
                            "yaml.frontend.mapping.merge-key",
                            tuple.getKeyNode().getStartMark(),
                            nodePath);
                }
                if (ATTRIBUTES_KEY.equals(key)) {
                    if (attributesSeen) {
                        throw unsafe(
                                "yaml.frontend.mapping.duplicate-attributes",
                                tuple.getKeyNode().getStartMark(),
                                nodePath);
                    }
                    attributesSeen = true;
                    attributes.putAll(readAttributes(
                            tuple.getValueNode(),
                            nodePath));
                } else if (TEXT_KEY.equals(key)) {
                    if (textSeen) {
                        throw unsafe(
                                "yaml.frontend.mapping.duplicate-text",
                                tuple.getKeyNode().getStartMark(),
                                nodePath);
                    }
                    textSeen = true;
                    scalar = readScalar(tuple.getValueNode(), nodePath);
                } else {
                    addChildren(
                            children,
                            key,
                            tuple.getKeyNode().getStartMark(),
                            tuple.getValueNode(),
                            nodePath,
                            depth + 1);
                }
            }
            return new NodeContent(attributes, scalar, children);
        }

        /**
         * 读取 `@attributes` Mapping，并拒绝复杂、重复或不可移植属性名称。
         */
        private Map<String, String> readAttributes(
                Node node,
                String nodePath) {
            enter(node, nodePath);
            try {
                requireTag(node, Tag.MAP, nodePath);
                if (!(node instanceof MappingNode)) {
                    throw unsafe(
                            "yaml.frontend.attributes.mapping-required",
                            node.getStartMark(),
                            nodePath);
                }
                MappingNode mapping = (MappingNode) node;
                requireMappingSize(mapping, nodePath);
                Map<String, String> attributes =
                        new LinkedHashMap<String, String>();
                for (NodeTuple tuple : mapping.getValue()) {
                    String key = requireName(
                            readKey(tuple.getKeyNode(), nodePath),
                            tuple.getKeyNode().getStartMark(),
                            nodePath);
                    if (isReservedKey(key) || MERGE_KEY.equals(key)) {
                        throw unsafe(
                                "yaml.frontend.attributes.invalid-key",
                                tuple.getKeyNode().getStartMark(),
                                nodePath);
                    }
                    if (attributes.containsKey(key)) {
                        throw unsafe(
                                "yaml.frontend.attributes.duplicate-key",
                                tuple.getKeyNode().getStartMark(),
                                nodePath);
                    }
                    attributes.put(
                            key,
                            readAttributeValue(
                                    tuple.getValueNode(),
                                    nodePath));
                }
                return attributes;
            } finally {
                leave(node);
            }
        }

        /**
         * 根据普通值或 Sequence 构建一个或多个同名子节点。
         */
        private void addChildren(
                List<CanonicalDocumentNode> children,
                String childName,
                Mark keyMark,
                Node value,
                String parentPath,
                int depth) {
            if (!(value instanceof SequenceNode)) {
                children.add(buildElement(
                        childName,
                        value,
                        keyMark,
                        parentPath,
                        depth));
                return;
            }

            enter(value, parentPath);
            try {
                SequenceNode sequence = (SequenceNode) value;
                requireTag(sequence, Tag.SEQ, parentPath);
                if (sequence.getValue().size()
                        > limits.maxSequenceItemsPerNode()) {
                    throw unsafe(
                            "yaml.frontend.limit.sequence-items",
                            sequence.getStartMark(),
                            parentPath);
                }
                for (Node item : sequence.getValue()) {
                    if (item instanceof SequenceNode) {
                        throw unsafe(
                                "yaml.frontend.sequence.nested-sequence",
                                item.getStartMark(),
                                parentPath);
                    }
                    children.add(buildElement(
                            childName,
                            item,
                            item.getStartMark(),
                            parentPath,
                            depth));
                }
            } finally {
                leave(value);
            }
        }

        /**
         * 读取字符串 Mapping key；复杂 key、非字符串 tag 和空白 key均失败。
         */
        private String readKey(Node node, String nodePath) {
            enter(node, nodePath);
            try {
                requireTag(node, Tag.STR, nodePath);
                if (!(node instanceof ScalarNode)) {
                    throw unsafe(
                            "yaml.frontend.mapping.string-key-required",
                            node.getStartMark(),
                            nodePath);
                }
                String key = ((ScalarNode) node).getValue();
                if (key == null || key.trim().isEmpty()) {
                    throw unsafe(
                            "yaml.frontend.mapping.blank-key",
                            node.getStartMark(),
                            nodePath);
                }
                return key;
            } finally {
                leave(node);
            }
        }

        /**
         * 读取允许 tag 的 scalar，并计入单节点及累计字符预算。
         */
        private Optional<String> readScalar(Node node, String nodePath) {
            enter(node, nodePath);
            try {
                if (!(node instanceof ScalarNode)) {
                    throw unsafe(
                            "yaml.frontend.scalar.required",
                            node.getStartMark(),
                            nodePath);
                }
                return readScalarEntered((ScalarNode) node, nodePath);
            } finally {
                leave(node);
            }
        }

        /**
         * 读取已经进入图检查作用域的 scalar。
         */
        private Optional<String> readScalarEntered(
                ScalarNode node,
                String nodePath) {
            requireAllowedScalarTag(node, nodePath);
            if (Tag.NULL.equals(node.getTag())) {
                return Optional.empty();
            }
            String value = node.getValue().trim();
            if (value.isEmpty()) {
                return Optional.empty();
            }
            reserveScalar(value.length(), node.getStartMark(), nodePath);
            return Optional.of(value);
        }

        /**
         * 读取属性 scalar；隐式 YAML null 映射为空字符串。
         */
        private String readAttributeValue(Node node, String nodePath) {
            enter(node, nodePath);
            try {
                if (!(node instanceof ScalarNode)) {
                    throw unsafe(
                            "yaml.frontend.attributes.scalar-required",
                            node.getStartMark(),
                            nodePath);
                }
                ScalarNode scalar = (ScalarNode) node;
                requireAllowedScalarTag(scalar, nodePath);
                if (Tag.NULL.equals(scalar.getTag())) {
                    return "";
                }
                String value = scalar.getValue().trim();
                reserveScalar(
                        value.length(),
                        scalar.getStartMark(),
                        nodePath);
                return value;
            } finally {
                leave(node);
            }
        }

        /**
         * 在创建 Canonical 节点前预留节点数和累计 nodePath 预算。
         */
        private void reserveNode(String nodePath, Mark mark) {
            if (nodeCount >= limits.maxNodeCount()) {
                throw unsafe(
                        "yaml.frontend.limit.node-count",
                        mark,
                        nodePath);
            }
            long total = checkedAdd(
                    cumulativeNodePathChars,
                    nodePath.length(),
                    "yaml.frontend.limit.node-path",
                    mark,
                    nodePath);
            if (total > limits.maxCumulativeNodePathChars()) {
                throw unsafe(
                        "yaml.frontend.limit.node-path",
                        mark,
                        nodePath);
            }
            nodeCount++;
            cumulativeNodePathChars = total;
        }

        /**
         * 在保存 scalar 前预留单值和累计字符预算。
         */
        private void reserveScalar(
                int length,
                Mark mark,
                String nodePath) {
            if (length > limits.maxScalarCharsPerNode()) {
                throw unsafe(
                        "yaml.frontend.limit.scalar-per-node",
                        mark,
                        nodePath);
            }
            long total = checkedAdd(
                    cumulativeScalarChars,
                    length,
                    "yaml.frontend.limit.scalar-total",
                    mark,
                    nodePath);
            if (total > limits.maxCumulativeScalarChars()) {
                throw unsafe(
                        "yaml.frontend.limit.scalar-total",
                        mark,
                        nodePath);
            }
            cumulativeScalarChars = total;
        }

        /**
         * 校验单 Mapping 的 entry 数，避免先扩容大集合再拒绝。
         */
        private void requireMappingSize(
                MappingNode mapping,
                String nodePath) {
            if (mapping.getValue().size()
                    > limits.maxMappingEntriesPerNode()) {
                throw unsafe(
                        "yaml.frontend.limit.mapping-entries",
                        mapping.getStartMark(),
                        nodePath);
            }
        }

        /**
         * 校验节点 tag 与冻结结构 tag 完全一致。
         */
        private static void requireTag(
                Node node,
                Tag expected,
                String nodePath) {
            if (!expected.equals(node.getTag())) {
                throw unsafe(
                        "yaml.frontend.tag.unsupported",
                        node.getStartMark(),
                        nodePath);
            }
        }

        /**
         * 允许隐式标准 scalar tag 与显式字符串，拒绝显式 typed tag。
         */
        private static void requireAllowedScalarTag(
                ScalarNode node,
                String nodePath) {
            Tag tag = node.getTag();
            if (!Tag.STR.equals(tag)
                    && !Tag.BOOL.equals(tag)
                    && !Tag.INT.equals(tag)
                    && !Tag.FLOAT.equals(tag)
                    && !Tag.NULL.equals(tag)
                    && !Tag.TIMESTAMP.equals(tag)) {
                throw unsafe(
                        "yaml.frontend.tag.unsupported",
                        node.getStartMark(),
                        nodePath);
            }
            if (!node.isResolved() && !Tag.STR.equals(tag)) {
                throw unsafe(
                        "yaml.frontend.scalar.explicit-typed-tag",
                        node.getStartMark(),
                        nodePath);
            }
        }

        /**
         * 进入 parser Node，并拒绝 anchor、alias、共享节点和递归图。
         */
        private void enter(Node node, String nodePath) {
            if (node == null) {
                throw unsafe(
                        "yaml.frontend.node.required",
                        null,
                        nodePath);
            }
            if (node.getAnchor() != null) {
                throw unsafe(
                        "yaml.frontend.anchor.unsupported",
                        node.getStartMark(),
                        nodePath);
            }
            if (active.contains(node) || visited.contains(node)) {
                throw unsafe(
                        "yaml.frontend.graph.shared-or-recursive",
                        node.getStartMark(),
                        nodePath);
            }
            active.add(node);
            visited.add(node);
        }

        /**
         * 离开当前递归节点；visited 保留以阻断后续共享引用。
         */
        private void leave(Node node) {
            active.remove(node);
        }

        /**
         * 创建一基 YAML 来源位置。
         */
        private SourceRef sourceRef(Mark mark, String nodePath) {
            int line = mark == null ? 0 : mark.getLine() + 1;
            int column = mark == null ? 0 : mark.getColumn() + 1;
            return new SourceRef(
                    source.sourceId(),
                    line,
                    column,
                    nodePath);
        }

        /**
         * 限制 Canonical 名称为可移植 ASCII NCName 子集，保证路径可逆且单行。
         */
        private static String requireName(
                String name,
                Mark mark,
                String parentPath) {
            if (name == null || name.isEmpty()) {
                throw unsafe(
                        "yaml.frontend.node.blank-name",
                        mark,
                        parentPath);
            }
            if (!isNameStart(name.charAt(0))) {
                throw unsafe(
                        "yaml.frontend.node.invalid-name",
                        mark,
                        parentPath);
            }
            for (int index = 1; index < name.length(); index++) {
                if (!isNamePart(name.charAt(index))) {
                    throw unsafe(
                            "yaml.frontend.node.invalid-name",
                            mark,
                            parentPath);
                }
            }
            return name;
        }

        /**
         * 判断 ASCII NCName 首字符。
         */
        private static boolean isNameStart(char value) {
            return value == '_'
                    || value >= 'A' && value <= 'Z'
                    || value >= 'a' && value <= 'z';
        }

        /**
         * 判断 ASCII NCName 后续字符。
         */
        private static boolean isNamePart(char value) {
            return isNameStart(value)
                    || value >= '0' && value <= '9'
                    || value == '.'
                    || value == '-';
        }

        /**
         * 执行 long 溢出安全加法；溢出与超限使用同一稳定失败边界。
         */
        private static long checkedAdd(
                long current,
                long addition,
                String messageKey,
                Mark mark,
                String nodePath) {
            if (addition < 0L || current > Long.MAX_VALUE - addition) {
                throw unsafe(messageKey, mark, nodePath);
            }
            return current + addition;
        }

        /**
         * 根节点名称不能占用节点 body 的保留 key。
         */
        private static boolean isReservedKey(String key) {
            return ATTRIBUTES_KEY.equals(key) || TEXT_KEY.equals(key);
        }
    }

    /**
     * Mapping body 的临时不可变解析结果，只在完整 Canonical 构建期间存在。
     */
    private static final class NodeContent {
        private final Map<String, String> attributes;
        private final Optional<String> scalar;
        private final List<CanonicalDocumentNode> children;

        private NodeContent(
                Map<String, String> attributes,
                Optional<String> scalar,
                List<CanonicalDocumentNode> children) {
            this.attributes = attributes;
            this.scalar = scalar;
            this.children = children;
        }

        private Map<String, String> attributes() {
            return attributes;
        }

        private Optional<String> scalar() {
            return scalar;
        }

        private List<CanonicalDocumentNode> children() {
            return children;
        }
    }

    /**
     * 内部受控失败，只保存稳定 messageKey、parser Mark 和 Canonical 路径。
     */
    private static final class YamlUnsafeException extends RuntimeException {
        private final String messageKey;
        private final Mark mark;
        private final String nodePath;

        private YamlUnsafeException(
                String messageKey,
                Mark mark,
                String nodePath) {
            super(messageKey, null, false, false);
            this.messageKey = messageKey;
            this.mark = mark;
            this.nodePath = nodePath;
        }

        private String messageKey() {
            return messageKey;
        }

        private Mark mark() {
            return mark;
        }

        private String nodePath() {
            return nodePath;
        }
    }
}
