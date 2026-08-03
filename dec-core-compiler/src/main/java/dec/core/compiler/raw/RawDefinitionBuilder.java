package dec.core.compiler.raw;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 将 Canonical 文档转换为 RawDefinitionSet 的无状态 Builder。
 *
 * <p>Builder 先验证整批文档的结构、资源边界和必填 lexical 事实，只有全部通过后
 * 才提取并冻结 RawDefinitionSet。任何失败都只返回 Diagnostic，不暴露部分集合。</p>
 */
public final class RawDefinitionBuilder {
    private static final String PASS = "raw-definition-builder";
    private static final SourceRef UNKNOWN_SOURCE =
            new SourceRef("<unknown-canonical-source>", 0, 0, "/");
    private static final Set<String> REFERENCE_ATTRIBUTES =
            unmodifiableSet(
                    "ref",
                    "data-source",
                    "ref-property",
                    "rel-value",
                    "data",
                    "view-ref",
                    "rule-ref",
                    "model-ref",
                    "system-ref",
                    "information-ref",
                    "property",
                    "view",
                    "rel");
    private static final Map<String, Set<String>> ORM_CONFIG_GRAMMAR =
            ormConfigGrammar();
    private static final Map<String, Set<String>> DATA_GRAMMAR =
            dataGrammar();
    private static final Map<String, Set<String>> VIEW_GRAMMAR =
            viewGrammar();
    private static final Map<String, Set<String>> SYSTEM_GRAMMAR =
            systemGrammar();
    private static final Map<String, Set<String>> RULE_GRAMMAR =
            ruleGrammar();
    private static final Map<String, Set<String>> BUSINESS_GRAMMAR =
            businessGrammar();

    private final RawBuilderLimits limits;

    /**
     * 创建使用 Design R24 冻结生产预算的无状态 Builder。
     */
    public RawDefinitionBuilder() {
        this(RawBuilderLimits.production());
    }

    /**
     * 创建使用显式小型预算的同包测试 Builder。
     *
     * @param limits Canonical 输入深度与节点数预算
     */
    RawDefinitionBuilder(RawBuilderLimits limits) {
        this.limits = Objects.requireNonNull(limits, "limits");
    }

    /**
     * 严格构建 RawDefinitionSet。
     *
     * @param documents 按稳定 Source 顺序提供的 Canonical 文档
     * @return BUILT 或不携带部分集合的 FAILED 结果
     */
    public RawBuildResult build(List<CanonicalDocumentNode> documents) {
        try {
            validateDocuments(documents);
            List<RawDefinition> definitions =
                    new ArrayList<RawDefinition>();
            Ordinal ordinal = new Ordinal();
            for (CanonicalDocumentNode document : documents) {
                extractDefinitions(
                        document.name(),
                        document,
                        LexicalContext.empty(),
                        definitions,
                        ordinal);
            }
            return RawBuildResult.built(new RawDefinitionSet(definitions));
        } catch (RawBuildFailure failure) {
            return failed(failure.messageKey(), failure.sourceRef());
        } catch (RuntimeException failure) {
            return failed("raw.build.failed", firstSourceRef(documents));
        }
    }

    /**
     * 第一阶段验证全部输入，避免边验证边发布部分 RawDefinition。
     */
    private void validateDocuments(List<CanonicalDocumentNode> documents) {
        if (documents == null || documents.isEmpty()) {
            throw failure("raw.input.required", UNKNOWN_SOURCE);
        }
        ValidationBudget budget = new ValidationBudget(limits);
        for (CanonicalDocumentNode document : documents) {
            if (document == null) {
                throw failure("raw.document.required", UNKNOWN_SOURCE);
            }
            Map<String, Set<String>> grammar = grammarForRoot(document.name());
            if (grammar == null) {
                throw failure(
                        "raw.document.root.unsupported",
                        document.sourceRef());
            }
            validateNode(
                    document.name(),
                    document,
                    grammar,
                    null,
                    1,
                    budget);
        }
    }

    /**
     * 递归校验资源、父子白名单和全部 lexical 事实。
     */
    private static void validateNode(
            String rootName,
            CanonicalDocumentNode node,
            Map<String, Set<String>> grammar,
            CanonicalDocumentNode parent,
            int depth,
            ValidationBudget budget) {
        budget.reserve(node, depth);
        validateReferenceFacts(rootName, node);
        validateDefinitionFacts(rootName, node, parent);
        Set<String> allowedChildren = grammar.get(node.name());
        if (allowedChildren == null) {
            throw failure("raw.structure.unknown", node.sourceRef());
        }
        for (CanonicalDocumentNode child : node.children()) {
            if (!allowedChildren.contains(child.name())) {
                throw failure("raw.structure.unknown", child.sourceRef());
            }
            validateNode(
                    rootName,
                    child,
                    grammar,
                    node,
                    depth + 1,
                    budget);
        }
    }

    /**
     * 校验会形成 RawDefinition 的节点是否包含冻结的 name/owner 属性。
     */
    private static void validateDefinitionFacts(
            String rootName,
            CanonicalDocumentNode node,
            CanonicalDocumentNode parent) {
        RawDefinitionKind kind = kindOf(rootName, node.name());
        if (kind == null) {
            return;
        }
        switch (kind) {
            case ROOT_CONFIG:
            case DATA_SOURCE:
            case CONNECTION:
            case DATA:
            case VIEW:
            case SYSTEM:
            case INFORMATION:
            case RULE:
            case BUSINESS_SCOPE:
            case DIRECTORY:
            case ACTION:
                requireAttribute(node, "name", "raw.definition.name.required");
                break;
            case MODEL_ACCESS:
                requireAttribute(node, "model-ref", "raw.definition.name.required");
                break;
            case RULE_VIEW:
                requireAttribute(node, "name", "raw.definition.name.required");
                requireAttribute(node, "system", "raw.definition.owner.required");
                break;
            case PRODUCE:
                break;
            default:
                throw new IllegalStateException("unexpected Raw kind: " + kind);
        }
        if (kind == RawDefinitionKind.RULE
                && (parent == null
                || !"rule-view-info".equals(parent.name()))) {
            throw failure("raw.definition.owner.required", node.sourceRef());
        }
    }

    /**
     * 第一阶段验证当前节点声明的全部未解析 reference target。
     *
     * <p>PRODUCE 的 `ref` 同时承担可选 name；R24 明确纯空白时映射为 absent，
     * 因此只有该精确位置跳过通用 reference 必填规则。</p>
     */
    private static void validateReferenceFacts(
            String rootName,
            CanonicalDocumentNode node) {
        for (Map.Entry<String, String> entry : node.attributes().entrySet()) {
            if (!isReferenceAttribute(entry.getKey())) {
                continue;
            }
            String target = entry.getValue();
            if (target == null || target.trim().isEmpty()) {
                if (isOptionalBlankProduceReference(
                        rootName,
                        node,
                        entry.getKey())) {
                    continue;
                }
                throw failure(
                        "raw.reference.target.required",
                        node.sourceRef());
            }
        }
    }

    /**
     * 判断当前空白引用是否为 PRODUCE 可选 `ref` 的精确例外。
     */
    private static boolean isOptionalBlankProduceReference(
            String rootName,
            CanonicalDocumentNode node,
            String attributeName) {
        return "business-config".equals(rootName)
                && "produce".equals(node.name())
                && "ref".equals(attributeName);
    }

    /**
     * 第二阶段按输入文档顺序和定义先序提取全部 RawDefinition。
     */
    private static void extractDefinitions(
            String rootName,
            CanonicalDocumentNode node,
            LexicalContext context,
            List<RawDefinition> definitions,
            Ordinal ordinal) {
        LexicalContext effective = context.enter(rootName, node);
        RawDefinitionKind kind = kindOf(rootName, node.name());
        if (kind != null) {
            definitions.add(createDefinition(
                    rootName,
                    node,
                    kind,
                    effective,
                    ordinal.next()));
        }
        for (CanonicalDocumentNode child : node.children()) {
            extractDefinitions(
                    rootName,
                    child,
                    effective,
                    definitions,
                    ordinal);
        }
    }

    /**
     * 从一个已验证的语义节点创建不可变 RawDefinition。
     */
    private static RawDefinition createDefinition(
            String rootName,
            CanonicalDocumentNode node,
            RawDefinitionKind kind,
            LexicalContext context,
            long sourceOrdinal) {
        return new RawDefinition(
                kind,
                sourceOrdinal,
                node.sourceRef(),
                ownerToken(kind, node, context),
                definitionName(kind, node),
                node.attributes(),
                extractReferences(rootName, node),
                copyBody(node),
                node.format(),
                node.schemaVersion());
    }

    /**
     * 按 R24 冻结规则返回未经规范化的 owner lexical token。
     */
    private static Optional<String> ownerToken(
            RawDefinitionKind kind,
            CanonicalDocumentNode node,
            LexicalContext context) {
        switch (kind) {
            case DATA_SOURCE:
            case CONNECTION:
                return requiredOptional(
                        context.rootConfigName,
                        node,
                        "raw.definition.owner.required");
            case INFORMATION:
            case MODEL_ACCESS:
                return requiredOptional(
                        context.systemName,
                        node,
                        "raw.definition.owner.required");
            case RULE_VIEW:
                return Optional.of(attribute(node, "system"));
            case RULE:
                return Optional.of(
                        required(context.ruleViewSystem, node)
                                + "/"
                                + required(context.ruleViewName, node));
            case DIRECTORY:
                return requiredOptional(
                        context.businessScopeName,
                        node,
                        "raw.definition.owner.required");
            case ACTION:
                return requiredOptional(
                        context.directoryName,
                        node,
                        "raw.definition.owner.required");
            case PRODUCE:
                return Optional.of(
                        required(context.directoryName, node)
                                + "/"
                                + required(context.actionName, node));
            default:
                return Optional.empty();
        }
    }

    /**
     * 按 Kind 从冻结属性中读取未经规范化的 definition name。
     */
    private static Optional<String> definitionName(
            RawDefinitionKind kind,
            CanonicalDocumentNode node) {
        switch (kind) {
            case MODEL_ACCESS:
                return Optional.of(attribute(node, "model-ref"));
            case PRODUCE:
                return optionalAttribute(node, "ref");
            default:
                return Optional.of(attribute(node, "name"));
        }
    }

    /**
     * 提取当前定义 lexical scope 的引用；遇到嵌套定义时停止下钻。
     */
    private static List<RawReference> extractReferences(
            String rootName,
            CanonicalDocumentNode definitionNode) {
        List<RawReference> references = new ArrayList<RawReference>();
        collectReferences(
                rootName,
                definitionNode,
                definitionNode,
                "",
                references);
        return references;
    }

    /**
     * 保持节点先序和属性稳定顺序收集原始引用。
     */
    private static void collectReferences(
            String rootName,
            CanonicalDocumentNode definitionNode,
            CanonicalDocumentNode current,
            String relativePath,
            List<RawReference> references) {
        for (Map.Entry<String, String> entry : current.attributes().entrySet()) {
            if (isReferenceAttribute(entry.getKey())) {
                String target = entry.getValue();
                if ((target == null || target.trim().isEmpty())
                        && isOptionalBlankProduceReference(
                                rootName,
                                current,
                                entry.getKey())) {
                    continue;
                }
                String role = relativePath + "@" + entry.getKey();
                references.add(new RawReference(
                        role,
                        target,
                        current.sourceRef()));
            }
        }
        for (CanonicalDocumentNode child : current.children()) {
            if (current != definitionNode
                    && kindOf(rootName, current.name()) != null) {
                return;
            }
            if (kindOf(rootName, child.name()) != null) {
                continue;
            }
            String childPath = relativePath + "/" + child.name();
            collectReferences(
                    rootName,
                    definitionNode,
                    child,
                    childPath,
                    references);
        }
    }

    /**
     * 将 Canonical 子树完整复制为不含 parser 对象的 RawNodeBody。
     */
    private static RawNodeBody copyBody(CanonicalDocumentNode node) {
        List<RawNodeBody> children = new ArrayList<RawNodeBody>();
        for (CanonicalDocumentNode child : node.children()) {
            children.add(copyBody(child));
        }
        return new RawNodeBody(
                node.name(),
                node.attributes(),
                node.scalar(),
                children,
                node.sourceRef());
    }

    /**
     * 判断属性是否属于尚未解析的 Raw reference 白名单。
     */
    private static boolean isReferenceAttribute(String name) {
        return REFERENCE_ATTRIBUTES.contains(name) || name.endsWith("-ref");
    }

    /**
     * 根据文档根和节点名称识别语义定义类别。
     */
    private static RawDefinitionKind kindOf(String rootName, String nodeName) {
        if ("orm-config".equals(rootName)) {
            if ("orm-config".equals(nodeName)) {
                return RawDefinitionKind.ROOT_CONFIG;
            }
            if ("orm-datasource".equals(nodeName)) {
                return RawDefinitionKind.DATA_SOURCE;
            }
            if ("orm-connection".equals(nodeName)) {
                return RawDefinitionKind.CONNECTION;
            }
        } else if ("orm-data-mapping".equals(rootName)
                && "data".equals(nodeName)) {
            return RawDefinitionKind.DATA;
        } else if ("orm-view-mapping".equals(rootName)
                && "view".equals(nodeName)) {
            return RawDefinitionKind.VIEW;
        } else if ("systems".equals(rootName)) {
            if ("system".equals(nodeName)) {
                return RawDefinitionKind.SYSTEM;
            }
            if ("information".equals(nodeName)) {
                return RawDefinitionKind.INFORMATION;
            }
            if ("model-access".equals(nodeName)) {
                return RawDefinitionKind.MODEL_ACCESS;
            }
        } else if ("orm-rule-mapping".equals(rootName)) {
            if ("rule-view-info".equals(nodeName)) {
                return RawDefinitionKind.RULE_VIEW;
            }
            if ("rule".equals(nodeName)) {
                return RawDefinitionKind.RULE;
            }
        } else if ("business-config".equals(rootName)) {
            if ("business-config".equals(nodeName)) {
                return RawDefinitionKind.BUSINESS_SCOPE;
            }
            if ("directory".equals(nodeName)) {
                return RawDefinitionKind.DIRECTORY;
            }
            if ("action".equals(nodeName)) {
                return RawDefinitionKind.ACTION;
            }
            if ("produce".equals(nodeName)) {
                return RawDefinitionKind.PRODUCE;
            }
        }
        return null;
    }

    /**
     * 返回指定文档根的完整父子语法。
     */
    private static Map<String, Set<String>> grammarForRoot(String rootName) {
        if ("orm-config".equals(rootName)) {
            return ORM_CONFIG_GRAMMAR;
        }
        if ("orm-data-mapping".equals(rootName)) {
            return DATA_GRAMMAR;
        }
        if ("orm-view-mapping".equals(rootName)) {
            return VIEW_GRAMMAR;
        }
        if ("systems".equals(rootName)) {
            return SYSTEM_GRAMMAR;
        }
        if ("orm-rule-mapping".equals(rootName)) {
            return RULE_GRAMMAR;
        }
        if ("business-config".equals(rootName)) {
            return BUSINESS_GRAMMAR;
        }
        return null;
    }

    /**
     * 读取必填属性；只用 trim 判断空白并返回原始 lexical token。
     */
    private static String attribute(CanonicalDocumentNode node, String name) {
        String value = node.attributes().get(name);
        if (value == null || value.trim().isEmpty()) {
            throw failure("raw.definition.attribute.required", node.sourceRef());
        }
        return value;
    }

    /**
     * 读取可选属性；纯空白视为 absent，其他值保持原始 lexical token。
     */
    private static Optional<String> optionalAttribute(
            CanonicalDocumentNode node,
            String name) {
        String value = node.attributes().get(name);
        if (value == null || value.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    /**
     * 使用指定 messageKey 校验属性存在，验证阶段不改变原值。
     */
    private static void requireAttribute(
            CanonicalDocumentNode node,
            String name,
            String messageKey) {
        String value = node.attributes().get(name);
        if (value == null || value.trim().isEmpty()) {
            throw failure(messageKey, node.sourceRef());
        }
    }

    /**
     * 将必填 owner 上下文转换为保留原值的 Optional。
     */
    private static Optional<String> requiredOptional(
            String value,
            CanonicalDocumentNode node,
            String messageKey) {
        if (value == null || value.trim().isEmpty()) {
            throw failure(messageKey, node.sourceRef());
        }
        return Optional.of(value);
    }

    /**
     * 读取必填 owner 上下文并保留原始 lexical token。
     */
    private static String required(String value, CanonicalDocumentNode node) {
        if (value == null || value.trim().isEmpty()) {
            throw failure("raw.definition.owner.required", node.sourceRef());
        }
        return value;
    }

    /**
     * 创建不携带部分集合的稳定失败结果。
     */
    private static RawBuildResult failed(String messageKey, SourceRef sourceRef) {
        SourceRef stableRef = sourceRef == null ? UNKNOWN_SOURCE : sourceRef;
        Diagnostic diagnostic = new Diagnostic(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                DiagnosticSeverity.ERROR,
                messageKey,
                null,
                stableRef,
                Collections.<SourceRef>emptyList(),
                "请修复 Canonical 结构、资源边界或 RawDefinition lexical 事实",
                PASS);
        return RawBuildResult.failed(Collections.singletonList(diagnostic));
    }

    private static SourceRef firstSourceRef(
            List<CanonicalDocumentNode> documents) {
        return documents != null
                && !documents.isEmpty()
                && documents.get(0) != null
                ? documents.get(0).sourceRef()
                : UNKNOWN_SOURCE;
    }

    private static RawBuildFailure failure(
            String messageKey,
            SourceRef sourceRef) {
        return new RawBuildFailure(messageKey, sourceRef);
    }

    private static Map<String, Set<String>> ormConfigGrammar() {
        Map<String, Set<String>> grammar = new HashMap<String, Set<String>>();
        grammar.put("orm-config", unmodifiableSet(
                "orm-datasource-info",
                "orm-data-file-info",
                "orm-view-file-info",
                "system-file-info",
                "business-file-info",
                "orm-connection-info"));
        grammar.put("orm-datasource-info", unmodifiableSet("orm-datasource"));
        grammar.put("orm-datasource", unmodifiableSet("name"));
        grammar.put("name", Collections.<String>emptySet());
        grammar.put("orm-data-file-info", unmodifiableSet("orm-file"));
        grammar.put("orm-view-file-info", unmodifiableSet("orm-file"));
        grammar.put("orm-file", Collections.<String>emptySet());
        grammar.put("system-file-info", unmodifiableSet("system-file"));
        grammar.put("system-file", Collections.<String>emptySet());
        grammar.put("business-file-info", unmodifiableSet("business-file"));
        grammar.put("business-file", Collections.<String>emptySet());
        grammar.put("orm-connection-info", unmodifiableSet("orm-connection"));
        grammar.put("orm-connection", unmodifiableSet("data-source-info"));
        grammar.put("data-source-info", unmodifiableSet("data-source"));
        grammar.put("data-source", Collections.<String>emptySet());
        return Collections.unmodifiableMap(grammar);
    }

    private static Map<String, Set<String>> dataGrammar() {
        Map<String, Set<String>> grammar = new HashMap<String, Set<String>>();
        grammar.put("orm-data-mapping", unmodifiableSet("data"));
        grammar.put("data", unmodifiableSet("property-info", "table-info"));
        grammar.put("property-info", unmodifiableSet("property"));
        grammar.put("property", Collections.<String>emptySet());
        grammar.put("table-info", unmodifiableSet("table"));
        grammar.put("table", unmodifiableSet("column"));
        grammar.put("column", Collections.<String>emptySet());
        return Collections.unmodifiableMap(grammar);
    }

    private static Map<String, Set<String>> viewGrammar() {
        Map<String, Set<String>> grammar = new HashMap<String, Set<String>>();
        grammar.put("orm-view-mapping", unmodifiableSet("view"));
        grammar.put("view", unmodifiableSet("property-info"));
        grammar.put("property-info", unmodifiableSet("property"));
        grammar.put("property", unmodifiableSet("property"));
        return Collections.unmodifiableMap(grammar);
    }

    private static Map<String, Set<String>> systemGrammar() {
        Map<String, Set<String>> grammar = new HashMap<String, Set<String>>();
        grammar.put("systems", unmodifiableSet("system"));
        grammar.put("system", unmodifiableSet(
                "data-info",
                "view-info",
                "rule-file-info",
                "information-info",
                "model-access-info"));
        grammar.put("data-info", unmodifiableSet("data-ref"));
        grammar.put("data-ref", Collections.<String>emptySet());
        grammar.put("view-info", unmodifiableSet("view-ref"));
        grammar.put("view-ref", Collections.<String>emptySet());
        grammar.put("rule-file-info", unmodifiableSet("rule-file"));
        grammar.put("rule-file", Collections.<String>emptySet());
        grammar.put("information-info", unmodifiableSet("information"));
        grammar.put("information", unmodifiableSet("change-data"));
        grammar.put("change-data", Collections.<String>emptySet());
        grammar.put("model-access-info", unmodifiableSet("model-access"));
        grammar.put("model-access", unmodifiableSet("read", "write"));
        grammar.put("read", unmodifiableSet("ref"));
        grammar.put("write", unmodifiableSet("ref"));
        grammar.put("ref", Collections.<String>emptySet());
        return Collections.unmodifiableMap(grammar);
    }

    private static Map<String, Set<String>> ruleGrammar() {
        Map<String, Set<String>> grammar = new HashMap<String, Set<String>>();
        grammar.put("orm-rule-mapping", unmodifiableSet("rule-view-info"));
        grammar.put("rule-view-info", unmodifiableSet("rule"));
        grammar.put("rule", unmodifiableSet("customer-process"));
        grammar.put("customer-process", Collections.<String>emptySet());
        return Collections.unmodifiableMap(grammar);
    }

    private static Map<String, Set<String>> businessGrammar() {
        Map<String, Set<String>> grammar = new HashMap<String, Set<String>>();
        grammar.put("business-config", unmodifiableSet("directory-info"));
        grammar.put("directory-info", unmodifiableSet("directory"));
        grammar.put("directory", unmodifiableSet(
                "subdirectory-info",
                "dependency-info",
                "action-info",
                "change-info"));
        grammar.put("subdirectory-info", unmodifiableSet("subdirectory"));
        grammar.put("subdirectory", unmodifiableSet("back"));
        grammar.put("back", unmodifiableSet("action-info"));
        grammar.put("dependency-info", unmodifiableSet("dependency"));
        grammar.put("dependency", Collections.<String>emptySet());
        grammar.put("action-info", unmodifiableSet("action"));
        grammar.put("action", unmodifiableSet("produce-info"));
        grammar.put("produce-info", unmodifiableSet("produce"));
        grammar.put("produce", Collections.<String>emptySet());
        grammar.put("change-info", Collections.<String>emptySet());
        return Collections.unmodifiableMap(grammar);
    }

    private static Set<String> unmodifiableSet(String... values) {
        return Collections.unmodifiableSet(
                new HashSet<String>(Arrays.asList(values)));
    }

    /**
     * 单次验证使用的节点与深度预算。
     */
    private static final class ValidationBudget {
        private final RawBuilderLimits limits;
        private int nodeCount;

        private ValidationBudget(RawBuilderLimits limits) {
            this.limits = limits;
        }

        /**
         * 在进入节点的其他递归逻辑前预留深度和节点数。
         */
        private void reserve(CanonicalDocumentNode node, int depth) {
            if (depth > limits.maxCanonicalDepth()) {
                throw failure("raw.limit.depth", node.sourceRef());
            }
            if (nodeCount >= limits.maxCanonicalNodeCount()) {
                throw failure("raw.limit.node-count", node.sourceRef());
            }
            nodeCount++;
        }
    }

    /**
     * 单次构建的连续 ordinal 计数器，只存在于 build 调用栈内。
     */
    private static final class Ordinal {
        private long value;

        private long next() {
            return value++;
        }
    }

    /**
     * 先序遍历携带的 lexical owner 上下文。
     */
    private static final class LexicalContext {
        private final String rootConfigName;
        private final String systemName;
        private final String ruleViewSystem;
        private final String ruleViewName;
        private final String businessScopeName;
        private final String directoryName;
        private final String actionName;

        private LexicalContext(
                String rootConfigName,
                String systemName,
                String ruleViewSystem,
                String ruleViewName,
                String businessScopeName,
                String directoryName,
                String actionName) {
            this.rootConfigName = rootConfigName;
            this.systemName = systemName;
            this.ruleViewSystem = ruleViewSystem;
            this.ruleViewName = ruleViewName;
            this.businessScopeName = businessScopeName;
            this.directoryName = directoryName;
            this.actionName = actionName;
        }

        private static LexicalContext empty() {
            return new LexicalContext(null, null, null, null, null, null, null);
        }

        /**
         * 进入当前节点并冻结后代所需的原始 owner lexical token。
         */
        private LexicalContext enter(
                String rootName,
                CanonicalDocumentNode node) {
            String nextRoot = rootConfigName;
            String nextSystem = systemName;
            String nextRuleSystem = ruleViewSystem;
            String nextRuleView = ruleViewName;
            String nextBusiness = businessScopeName;
            String nextDirectory = directoryName;
            String nextAction = actionName;
            if ("orm-config".equals(rootName)
                    && "orm-config".equals(node.name())) {
                nextRoot = attribute(node, "name");
            } else if ("systems".equals(rootName)
                    && "system".equals(node.name())) {
                nextSystem = attribute(node, "name");
            } else if ("orm-rule-mapping".equals(rootName)
                    && "rule-view-info".equals(node.name())) {
                nextRuleSystem = attribute(node, "system");
                nextRuleView = attribute(node, "name");
            } else if ("business-config".equals(rootName)
                    && "business-config".equals(node.name())) {
                nextBusiness = attribute(node, "name");
            } else if ("business-config".equals(rootName)
                    && "directory".equals(node.name())) {
                nextDirectory = attribute(node, "name");
                nextAction = null;
            } else if ("business-config".equals(rootName)
                    && "action".equals(node.name())) {
                nextAction = attribute(node, "name");
            }
            return new LexicalContext(
                    nextRoot,
                    nextSystem,
                    nextRuleSystem,
                    nextRuleView,
                    nextBusiness,
                    nextDirectory,
                    nextAction);
        }
    }

    /**
     * 内部受控失败，只保存稳定 messageKey 和 SourceRef。
     */
    private static final class RawBuildFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private final String messageKey;
        private final SourceRef sourceRef;

        private RawBuildFailure(String messageKey, SourceRef sourceRef) {
            super(messageKey, null, false, false);
            this.messageKey = Objects.requireNonNull(messageKey, "messageKey");
            this.sourceRef = sourceRef == null ? UNKNOWN_SOURCE : sourceRef;
        }

        private String messageKey() {
            return messageKey;
        }

        private SourceRef sourceRef() {
            return sourceRef;
        }
    }
}
