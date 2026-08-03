package dec.core.compiler.symbol;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.raw.RawReference;
import dec.core.context.model.ActionKey;
import dec.core.context.model.BusinessScopeKey;
import dec.core.context.model.ConnectionKey;
import dec.core.context.model.DataKey;
import dec.core.context.model.DataSourceKey;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.DirectoryKey;
import dec.core.context.model.InformationKey;
import dec.core.context.model.ProduceKey;
import dec.core.context.model.RuleViewKey;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 在完整 T07 SymbolTable 上执行 P1 精确强类型引用解析。
 *
 * <p>Resolver 先建立全部只读索引，再扫描 RawDefinitionSet，因此支持跨文件
 * 前向引用。成功解析只使用构造出的期望 TypedKey 调用 SymbolTable.find；
 * lexical 索引只用于区分 unknown 与 type mismatch，绝不参与成功匹配。</p>
 */
public final class ReferenceResolver {
    private static final String PASS = "reference-resolution";
    private static final SourceRef UNKNOWN_SOURCE =
            new SourceRef("<unknown-reference-source>", 0, 0, "/");

    /**
     * 解析全部 T08 范围引用；任一 ERROR 都不发布部分引用集合。
     */
    public ReferenceResolutionResult resolve(
            RawDefinitionSet definitions,
            SymbolTable symbolTable) {
        if (definitions == null || symbolTable == null) {
            return ReferenceResolutionResult.failed(Collections.singletonList(
                    diagnostic(
                            DiagnosticCode.MIX_REF_UNKNOWN,
                            "reference.input.required",
                            null,
                            UNKNOWN_SOURCE,
                            Collections.<SourceRef>emptyList(),
                            "请提供完整 RawDefinitionSet 与 SymbolTable")));
        }

        ResolutionState state = new ResolutionState(symbolTable);
        indexSymbols(state);
        indexDataProperties(state);
        indexSystemViewDeclarations(definitions, state);
        for (RawDefinition definition : definitions.definitions()) {
            resolveDefinition(definition, state);
        }
        if (!state.diagnostics.isEmpty()) {
            return ReferenceResolutionResult.failed(
                    new ArrayList<Diagnostic>(state.diagnostics));
        }
        return ReferenceResolutionResult.resolved(
                new ResolvedReferenceSet(
                        new ArrayList<ResolvedReference>(state.references)));
    }

    /**
     * 将 SymbolTable 的稳定 Key/Definition 对齐关系转换为单次解析索引。
     */
    private static void indexSymbols(ResolutionState state) {
        List<DefinitionKey> keys = state.symbolTable.keys();
        List<RawDefinition> definitions = state.symbolTable.definitions();
        for (int index = 0; index < keys.size(); index++) {
            DefinitionKey key = keys.get(index);
            RawDefinition definition = definitions.get(index);
            state.sourceKeys.put(definition.sourceOrdinal(), key);
            state.definitionsByKey.put(key, definition);
            String lexical = lexicalName(key);
            if (lexical != null) {
                List<DefinitionKey> values = state.keysByLexical.get(lexical);
                if (values == null) {
                    values = new ArrayList<DefinitionKey>();
                    state.keysByLexical.put(lexical, values);
                }
                values.add(key);
            }
        }
    }

    /**
     * 从 Data Raw body 收集区分大小写的属性名称，供 View 精确校验。
     */
    private static void indexDataProperties(ResolutionState state) {
        for (Map.Entry<DefinitionKey, RawDefinition> entry
                : state.definitionsByKey.entrySet()) {
            if (!(entry.getKey() instanceof DataKey)) {
                continue;
            }
            Set<String> properties = new HashSet<String>();
            collectPropertyNames(entry.getValue().body(), properties);
            state.dataProperties.put((DataKey) entry.getKey(), properties);
        }
    }

    /** 递归收集 Data body 内所有 property@name。 */
    private static void collectPropertyNames(
            RawNodeBody node,
            Set<String> properties) {
        if ("property".equals(node.name())) {
            String name = node.attributes().get("name");
            if (name != null) {
                properties.add(name);
            }
        }
        for (RawNodeBody child : node.children()) {
            collectPropertyNames(child, properties);
        }
    }

    /**
     * 在解析 RuleView 前建立每个 System 显式声明的 ViewKey 集合。
     */
    private static void indexSystemViewDeclarations(
            RawDefinitionSet definitions,
            ResolutionState state) {
        for (RawDefinition definition : definitions.definitions(
                RawDefinitionKind.SYSTEM)) {
            DefinitionKey key = state.sourceKeys.get(definition.sourceOrdinal());
            if (!(key instanceof SystemKey)) {
                continue;
            }
            Set<ViewKey> views = new HashSet<ViewKey>();
            collectSystemViews(definition.body(), views);
            if (views.isEmpty()) {
                for (RawReference reference : definition.references()) {
                    if (isViewReference(reference.role())) {
                        views.add(new ViewKey(reference.target()));
                    }
                }
            }
            state.systemViews.put((SystemKey) key, views);
        }
    }

    /** 递归收集 System body 中 view-ref 的 name/ref lexical。 */
    private static void collectSystemViews(
            RawNodeBody node,
            Set<ViewKey> views) {
        if ("view-ref".equals(node.name())) {
            String target = referenceTarget(node);
            if (target != null) {
                views.add(new ViewKey(target));
            }
        }
        for (RawNodeBody child : node.children()) {
            collectSystemViews(child, views);
        }
    }

    /** 按 RawDefinitionKind 派发冻结的 T08 引用策略。 */
    private static void resolveDefinition(
            RawDefinition definition,
            ResolutionState state) {
        DefinitionKey sourceKey = state.sourceKeys.get(
                definition.sourceOrdinal());
        switch (definition.kind()) {
            case CONNECTION:
                resolveConnection(definition, sourceKey, state);
                break;
            case VIEW:
                resolveView(definition, sourceKey, state);
                break;
            case SYSTEM:
                resolveSystem(definition, sourceKey, state);
                break;
            case RULE_VIEW:
                resolveRuleView(definition, sourceKey, state);
                break;
            case ACTION:
                resolveAction(definition, sourceKey, state);
                break;
            case DIRECTORY:
                resolveDirectory(definition, sourceKey, state);
                break;
            case PRODUCE:
                resolveProduce(definition, sourceKey, state);
                break;
            case ROOT_CONFIG:
            case DATA_SOURCE:
            case DATA:
            case INFORMATION:
            case RULE:
            case BUSINESS_SCOPE:
            case MODEL_ACCESS:
                break;
            default:
                addDiagnostic(state, diagnostic(
                        DiagnosticCode.MIX_REF_UNKNOWN,
                        "reference.kind.unsupported",
                        sourceKey,
                        definition.sourceRef(),
                        Collections.<SourceRef>emptyList(),
                        "请为该 RawDefinitionKind 冻结引用策略"));
                break;
        }
    }

    /** 解析 Connection 到 DataSourceKey。 */
    private static void resolveConnection(
            RawDefinition definition,
            DefinitionKey sourceKey,
            ResolutionState state) {
        for (RawReference reference : definition.references()) {
            if (reference.role().contains("data-source")
                    && reference.role().endsWith("@ref")) {
                resolveExact(
                        sourceKey,
                        reference.role(),
                        reference.target(),
                        new DataSourceKey(reference.target()),
                        reference.sourceRef(),
                        state);
            }
        }
    }

    /**
     * 解析 View target-main、嵌套 data 与当前 Data 内的 ref-property。
     */
    private static void resolveView(
            RawDefinition definition,
            DefinitionKey sourceKey,
            ResolutionState state) {
        String targetMain = definition.attributes().get("target-main");
        DataKey rootData = null;
        if (targetMain != null) {
            rootData = (DataKey) resolveExact(
                    sourceKey,
                    "@target-main",
                    targetMain,
                    new DataKey(targetMain),
                    definition.sourceRef(),
                    state);
        }
        boolean bodyHasPropertyReference = containsAttribute(
                definition.body(),
                "ref-property");
        resolveViewNode(definition.body(), rootData, sourceKey, state);
        if (!bodyHasPropertyReference) {
            for (RawReference reference : definition.references()) {
                if (reference.role().endsWith("@ref-property")) {
                    resolveProperty(
                            sourceKey,
                            reference.role(),
                            reference.target(),
                            rootData,
                            reference.sourceRef(),
                            state);
                }
            }
        }
    }

    /**
     * 递归恢复 View 节点当前 Data owner，禁止跨 Data 搜索 property。
     */
    private static void resolveViewNode(
            RawNodeBody node,
            DataKey inheritedData,
            DefinitionKey sourceKey,
            ResolutionState state) {
        DataKey currentData = inheritedData;
        String dataTarget = node.attributes().get("data");
        if (dataTarget != null) {
            currentData = (DataKey) resolveExact(
                    sourceKey,
                    node.sourceRef().nodePath() + "@data",
                    dataTarget,
                    new DataKey(dataTarget),
                    node.sourceRef(),
                    state);
        }
        String propertyTarget = node.attributes().get("ref-property");
        if (propertyTarget != null) {
            resolveProperty(
                    sourceKey,
                    node.sourceRef().nodePath() + "@ref-property",
                    propertyTarget,
                    currentData,
                    node.sourceRef(),
                    state);
        }
        for (RawNodeBody child : node.children()) {
            resolveViewNode(child, currentData, sourceKey, state);
        }
    }

    /** 精确校验属性属于当前绑定 Data，并以 DataKey 发布 TypedKey 事实。 */
    private static void resolveProperty(
            DefinitionKey sourceKey,
            String role,
            String property,
            DataKey dataKey,
            SourceRef sourceRef,
            ResolutionState state) {
        if (dataKey == null) {
            addDiagnostic(state, diagnostic(
                    DiagnosticCode.MIX_REF_UNKNOWN,
                    "reference.owner.invalid",
                    null,
                    sourceRef,
                    Collections.<SourceRef>emptyList(),
                    "请先为 View 节点声明精确 Data owner"));
            return;
        }
        Set<String> properties = state.dataProperties.get(dataKey);
        if (properties == null || !properties.contains(property)) {
            addDiagnostic(state, diagnostic(
                    DiagnosticCode.MIX_REF_UNKNOWN,
                    "reference.property.unknown",
                    dataKey,
                    sourceRef,
                    relatedRef(dataKey, state),
                    "请使用当前 Data 中区分大小写的完整属性名称"));
            return;
        }
        addReference(state, new ResolvedReference(
                sourceKey,
                role,
                property,
                dataKey,
                sourceRef));
    }

    /** 解析 System 显式 Data/View 声明。 */
    private static void resolveSystem(
            RawDefinition definition,
            DefinitionKey sourceKey,
            ResolutionState state) {
        boolean bodyHasTargets = resolveSystemNode(
                definition.body(),
                sourceKey,
                state);
        if (!bodyHasTargets) {
            for (RawReference reference : definition.references()) {
                if (isDataReference(reference.role())) {
                    resolveExact(sourceKey, reference.role(), reference.target(),
                            new DataKey(reference.target()), reference.sourceRef(), state);
                } else if (isViewReference(reference.role())) {
                    resolveExact(sourceKey, reference.role(), reference.target(),
                            new ViewKey(reference.target()), reference.sourceRef(), state);
                }
            }
        }
    }

    /** 递归解析 System body 中 data-ref/view-ref。 */
    private static boolean resolveSystemNode(
            RawNodeBody node,
            DefinitionKey sourceKey,
            ResolutionState state) {
        boolean found = false;
        if ("data-ref".equals(node.name()) || "view-ref".equals(node.name())) {
            String target = referenceTarget(node);
            if (target != null) {
                String attribute = node.attributes().containsKey("ref")
                        ? "ref" : "name";
                if ("data-ref".equals(node.name())) {
                    resolveExact(sourceKey,
                            node.sourceRef().nodePath() + "@" + attribute,
                            target,
                            new DataKey(target),
                            node.sourceRef(),
                            state);
                } else {
                    resolveExact(sourceKey,
                            node.sourceRef().nodePath() + "@" + attribute,
                            target,
                            new ViewKey(target),
                            node.sourceRef(),
                            state);
                }
                found = true;
            }
        }
        for (RawNodeBody child : node.children()) {
            found = resolveSystemNode(child, sourceKey, state) || found;
        }
        return found;
    }

    /**
     * 根据 RuleView 自身 ownerToken 解析 System 与 declared View 边界。
     */
    private static void resolveRuleView(
            RawDefinition definition,
            DefinitionKey sourceKey,
            ResolutionState state) {
        if (!definition.ownerToken().isPresent()) {
            addDiagnostic(state, diagnostic(
                    DiagnosticCode.MIX_REF_RULE_SYSTEM_MISMATCH,
                    "reference.rule-system.mismatch",
                    sourceKey,
                    definition.sourceRef(),
                    Collections.<SourceRef>emptyList(),
                    "请声明 RuleView 所属 System"));
            return;
        }
        String ownerToken = definition.ownerToken().get();
        SystemKey systemKey = new SystemKey(ownerToken);
        DefinitionKey resolvedSystem = resolveExact(
                sourceKey,
                "@system-owner",
                ownerToken,
                systemKey,
                definition.sourceRef(),
                state);

        String viewToken = definition.attributes().get("view-ref");
        SourceRef viewSource = definition.sourceRef();
        if (viewToken == null) {
            for (RawReference reference : definition.references()) {
                if (reference.role().endsWith("@view-ref")) {
                    viewToken = reference.target();
                    viewSource = reference.sourceRef();
                    break;
                }
            }
        }
        if (viewToken == null) {
            return;
        }
        ViewKey viewKey = new ViewKey(viewToken);
        if (!state.symbolTable.find(viewKey).isPresent()) {
            addMissingDiagnostic(sourceKey, viewToken, viewKey, viewSource, state);
            return;
        }
        Set<ViewKey> declared = state.systemViews.get(systemKey);
        if (resolvedSystem == null || declared == null || !declared.contains(viewKey)) {
            addDiagnostic(state, diagnostic(
                    DiagnosticCode.MIX_REF_RULE_SYSTEM_MISMATCH,
                    "reference.rule-system.mismatch",
                    viewKey,
                    viewSource,
                    relatedRef(viewKey, state),
                    "请确保 RuleView.system 与该 System 的 view-info 一致"));
            return;
        }
        addReference(state, new ResolvedReference(
                sourceKey,
                "@view-ref",
                viewToken,
                viewKey,
                viewSource));
    }

    /** 解析 Action 的 System 与同 owner RuleView。 */
    private static void resolveAction(
            RawDefinition definition,
            DefinitionKey sourceKey,
            ResolutionState state) {
        RawReference systemReference = findReference(definition, "@system-ref");
        RawReference ruleReference = findReference(definition, "@rule-ref");
        SystemKey systemKey = null;
        if (systemReference != null) {
            systemKey = new SystemKey(systemReference.target());
            if (resolveExact(
                    sourceKey,
                    systemReference.role(),
                    systemReference.target(),
                    systemKey,
                    systemReference.sourceRef(),
                    state) == null) {
                systemKey = null;
            }
        }
        if (ruleReference == null) {
            return;
        }
        if (systemKey == null) {
            addDiagnostic(state, diagnostic(
                    DiagnosticCode.MIX_REF_RULE_SYSTEM_MISMATCH,
                    "reference.rule-system.mismatch",
                    sourceKey,
                    ruleReference.sourceRef(),
                    Collections.<SourceRef>emptyList(),
                    "请先声明可解析的 Action system-ref"));
            return;
        }
        RuleViewKey expected = new RuleViewKey(
                systemKey,
                ruleReference.target());
        if (state.symbolTable.find(expected).isPresent()) {
            addReference(state, new ResolvedReference(
                    sourceKey,
                    ruleReference.role(),
                    ruleReference.target(),
                    expected,
                    ruleReference.sourceRef()));
        } else if (hasRuleViewName(ruleReference.target(), state)) {
            addDiagnostic(state, diagnostic(
                    DiagnosticCode.MIX_REF_RULE_SYSTEM_MISMATCH,
                    "reference.rule-system.mismatch",
                    expected,
                    ruleReference.sourceRef(),
                    Collections.<SourceRef>emptyList(),
                    "请使用指定 System 拥有的 RuleView"));
        } else {
            addMissingDiagnostic(
                    sourceKey,
                    ruleReference.target(),
                    expected,
                    ruleReference.sourceRef(),
                    state);
        }
    }

    /** 解析 Directory 的 qualified Information 与同 Scope rel。 */
    private static void resolveDirectory(
            RawDefinition definition,
            DefinitionKey sourceKey,
            ResolutionState state) {
        for (RawReference reference : definition.references()) {
            if (reference.role().endsWith("@information-ref")) {
                resolveInformation(sourceKey, reference, state);
            } else if (reference.role().endsWith("@rel")) {
                if (!(sourceKey instanceof DirectoryKey)) {
                    addOwnerDiagnostic(sourceKey, reference.sourceRef(), state);
                    continue;
                }
                DirectoryKey sourceDirectory = (DirectoryKey) sourceKey;
                DirectoryKey target = new DirectoryKey(
                        sourceDirectory.owner(),
                        reference.target());
                resolveExact(sourceKey, reference.role(), reference.target(),
                        target, reference.sourceRef(), state);
            }
        }
    }

    /** 解析 Produce 中存在的 qualified information-ref。 */
    private static void resolveProduce(
            RawDefinition definition,
            DefinitionKey sourceKey,
            ResolutionState state) {
        for (RawReference reference : definition.references()) {
            if (reference.role().endsWith("@information-ref")) {
                resolveInformation(sourceKey, reference, state);
            }
        }
    }

    /** 将 system.name 精确转换为 InformationKey。 */
    private static void resolveInformation(
            DefinitionKey sourceKey,
            RawReference reference,
            ResolutionState state) {
        int separator = reference.target().indexOf('.');
        if (separator <= 0 || separator == reference.target().length() - 1) {
            addOwnerDiagnostic(sourceKey, reference.sourceRef(), state);
            return;
        }
        String system = reference.target().substring(0, separator);
        String information = reference.target().substring(separator + 1);
        InformationKey expected = new InformationKey(
                new SystemKey(system),
                information);
        resolveExact(sourceKey, reference.role(), reference.target(),
                expected, reference.sourceRef(), state);
    }

    /**
     * 使用精确 TypedKey 查询目标；缺失时只用 lexical 索引分类失败原因。
     */
    private static DefinitionKey resolveExact(
            DefinitionKey sourceKey,
            String role,
            String targetToken,
            DefinitionKey expectedKey,
            SourceRef sourceRef,
            ResolutionState state) {
        if (sourceKey == null) {
            addOwnerDiagnostic(null, sourceRef, state);
            return null;
        }
        if (state.symbolTable.find(expectedKey).isPresent()) {
            addReference(state, new ResolvedReference(
                    sourceKey,
                    role,
                    targetToken,
                    expectedKey,
                    sourceRef));
            return expectedKey;
        }
        addMissingDiagnostic(
                sourceKey,
                targetToken,
                expectedKey,
                sourceRef,
                state);
        return null;
    }

    /** 根据现有同 lexical Key 区分 unknown、type mismatch 与 owner mismatch。 */
    private static void addMissingDiagnostic(
            DefinitionKey sourceKey,
            String targetToken,
            DefinitionKey expectedKey,
            SourceRef sourceRef,
            ResolutionState state) {
        String lexical = lexicalName(expectedKey);
        List<DefinitionKey> candidates = state.keysByLexical.get(
                lexical == null ? targetToken : lexical);
        String messageKey = "reference.unknown";
        if (candidates != null && !candidates.isEmpty()) {
            boolean sameType = false;
            for (DefinitionKey candidate : candidates) {
                if (candidate.getClass().equals(expectedKey.getClass())) {
                    sameType = true;
                    break;
                }
            }
            messageKey = sameType
                    ? "reference.owner.invalid"
                    : "reference.type.mismatch";
        }
        addDiagnostic(state, diagnostic(
                DiagnosticCode.MIX_REF_UNKNOWN,
                messageKey,
                expectedKey,
                sourceRef,
                candidates == null || candidates.isEmpty()
                        ? Collections.<SourceRef>emptyList()
                        : relatedRef(candidates.get(0), state),
                "请使用精确存在且类型与 owner 一致的 TypedKey"));
    }

    /** 添加 owner 上下文不完整 Diagnostic。 */
    private static void addOwnerDiagnostic(
            DefinitionKey sourceKey,
            SourceRef sourceRef,
            ResolutionState state) {
        addDiagnostic(state, diagnostic(
                DiagnosticCode.MIX_REF_UNKNOWN,
                "reference.owner.invalid",
                sourceKey,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                "请提供完整且限定的引用 owner"));
    }

    /** 返回指定 Key 的定义来源，供错误关联，不参与成功查找。 */
    private static List<SourceRef> relatedRef(
            DefinitionKey key,
            ResolutionState state) {
        RawDefinition definition = state.definitionsByKey.get(key);
        return definition == null
                ? Collections.<SourceRef>emptyList()
                : Collections.singletonList(definition.sourceRef());
    }

    /** 判断是否存在其他 System 下同名 RuleView。 */
    private static boolean hasRuleViewName(
            String name,
            ResolutionState state) {
        List<DefinitionKey> values = state.keysByLexical.get(name.trim());
        if (values == null) {
            return false;
        }
        for (DefinitionKey value : values) {
            if (value instanceof RuleViewKey) {
                return true;
            }
        }
        return false;
    }

    /** 查找定义中的第一个精确后缀引用。 */
    private static RawReference findReference(
            RawDefinition definition,
            String suffix) {
        for (RawReference reference : definition.references()) {
            if (reference.role().endsWith(suffix)) {
                return reference;
            }
        }
        return null;
    }

    private static boolean isDataReference(String role) {
        return role.contains("data-info/data-ref")
                || role.endsWith("/data-ref@ref")
                || role.endsWith("/data-ref@name");
    }

    private static boolean isViewReference(String role) {
        return role.contains("view-info/view-ref")
                || role.endsWith("/view-ref@ref")
                || role.endsWith("/view-ref@name");
    }

    /** 从 data-ref/view-ref 节点读取 ref 或 name。 */
    private static String referenceTarget(RawNodeBody node) {
        String target = node.attributes().get("ref");
        return target == null ? node.attributes().get("name") : target;
    }

    /** 判断 Raw body 是否包含指定引用属性。 */
    private static boolean containsAttribute(
            RawNodeBody node,
            String attribute) {
        if (node.attributes().containsKey(attribute)) {
            return true;
        }
        for (RawNodeBody child : node.children()) {
            if (containsAttribute(child, attribute)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 提取 Key 的 lexical 查询名；owner-scoped Key 保留限定或局部名称语义。
     */
    private static String lexicalName(DefinitionKey key) {
        if (key instanceof DataSourceKey) {
            return ((DataSourceKey) key).name();
        }
        if (key instanceof ConnectionKey) {
            return ((ConnectionKey) key).name();
        }
        if (key instanceof DataKey) {
            return ((DataKey) key).name();
        }
        if (key instanceof ViewKey) {
            return ((ViewKey) key).name();
        }
        if (key instanceof SystemKey) {
            return ((SystemKey) key).name();
        }
        if (key instanceof RuleViewKey) {
            return ((RuleViewKey) key).name();
        }
        if (key instanceof InformationKey) {
            InformationKey information = (InformationKey) key;
            return information.owner().name() + "." + information.name();
        }
        if (key instanceof BusinessScopeKey) {
            return ((BusinessScopeKey) key).name();
        }
        if (key instanceof DirectoryKey) {
            return ((DirectoryKey) key).name();
        }
        if (key instanceof ActionKey) {
            return ((ActionKey) key).actionName();
        }
        if (key instanceof ProduceKey) {
            return key.canonical();
        }
        return null;
    }

    /** 使用 Set 阻断完全相同引用重复发布。 */
    private static void addReference(
            ResolutionState state,
            ResolvedReference reference) {
        state.references.add(Objects.requireNonNull(reference, "reference"));
    }

    /** 使用 LinkedHashSet 完整聚合并去重 Diagnostic。 */
    private static void addDiagnostic(
            ResolutionState state,
            Diagnostic diagnostic) {
        state.diagnostics.add(Objects.requireNonNull(diagnostic, "diagnostic"));
    }

    /** 创建 reference-resolution 阶段的稳定 ERROR Diagnostic。 */
    private static Diagnostic diagnostic(
            DiagnosticCode code,
            String messageKey,
            DefinitionKey definitionKey,
            SourceRef sourceRef,
            List<SourceRef> relatedRefs,
            String recoveryHint) {
        return new Diagnostic(
                code,
                DiagnosticSeverity.ERROR,
                messageKey,
                definitionKey,
                sourceRef,
                relatedRefs,
                recoveryHint,
                PASS);
    }

    /** 单次 resolve 私有状态，不跨调用或线程共享。 */
    private static final class ResolutionState {
        private final SymbolTable symbolTable;
        private final Map<Long, DefinitionKey> sourceKeys =
                new HashMap<Long, DefinitionKey>();
        private final Map<DefinitionKey, RawDefinition> definitionsByKey =
                new HashMap<DefinitionKey, RawDefinition>();
        private final Map<String, List<DefinitionKey>> keysByLexical =
                new HashMap<String, List<DefinitionKey>>();
        private final Map<SystemKey, Set<ViewKey>> systemViews =
                new HashMap<SystemKey, Set<ViewKey>>();
        private final Map<DataKey, Set<String>> dataProperties =
                new HashMap<DataKey, Set<String>>();
        private final Set<ResolvedReference> references =
                new LinkedHashSet<ResolvedReference>();
        private final Set<Diagnostic> diagnostics =
                new LinkedHashSet<Diagnostic>();

        private ResolutionState(SymbolTable symbolTable) {
            this.symbolTable = Objects.requireNonNull(symbolTable, "symbolTable");
        }
    }
}
