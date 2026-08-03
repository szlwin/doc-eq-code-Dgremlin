package dec.core.compiler.symbol;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionSet;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 将 T06 RawDefinitionSet 转换为强类型 SymbolTable 的无状态 Builder。
 *
 * <p>Builder 同时维护 Raw lexical owner 与 canonical TypedKey。第一遍登记
 * 顶层和结构 owner，并延迟 RuleView；第二遍登记 Information 与 Produce。
 * 只有所有步骤完成且无错误时才发布完整表。</p>
 */
public final class SymbolTableBuilder {
    private static final String PASS = "symbol-registration";
    private static final SourceRef UNKNOWN_SOURCE =
            new SourceRef("<unknown-raw-source>", 0, 0, "/");

    private final SymbolBuilderLimits limits;

    /**
     * 创建使用 T07 生产预算的无状态 Builder。
     */
    public SymbolTableBuilder() {
        this(SymbolBuilderLimits.production());
    }

    /**
     * 创建使用显式小型预算的同包测试 Builder。
     */
    SymbolTableBuilder(SymbolBuilderLimits limits) {
        this.limits = Objects.requireNonNull(limits, "limits");
    }

    /**
     * 按两遍结构登记 TypedKey；任一失败都不发布部分 SymbolTable。
     */
    public SymbolBuildResult build(RawDefinitionSet definitions) {
        if (definitions == null) {
            return failed(singleDiagnostic(
                    DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                    "symbol.input.required",
                    null,
                    UNKNOWN_SOURCE,
                    Collections.<SourceRef>emptyList()));
        }
        List<RawDefinition> ordered = definitions.definitions();
        if (ordered.size() > limits.maxDefinitionCount()) {
            RawDefinition exceeded = ordered.get(limits.maxDefinitionCount());
            return failed(singleDiagnostic(
                    DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                    "symbol.limit.definition-count",
                    null,
                    exceeded.sourceRef(),
                    Collections.<SourceRef>emptyList()));
        }

        RegistrationState state = new RegistrationState();
        firstPass(ordered, state);
        registerRuleViews(state);
        secondPass(ordered, state);
        if (!state.diagnostics.isEmpty()) {
            return failed(state.diagnostics.values());
        }
        return SymbolBuildResult.built(new SymbolTable(state.entries));
    }

    /**
     * 第一遍登记顶层和结构 owner TypedKey，并暂存显式 owner 的 RuleView。
     */
    private static void firstPass(
            List<RawDefinition> definitions,
            RegistrationState state) {
        FirstPassContext context = new FirstPassContext();
        for (RawDefinition definition : definitions) {
            switch (definition.kind()) {
                case ROOT_CONFIG:
                    context.enterRoot(requiredName(definition));
                    break;
                case DATA_SOURCE:
                    registerRootOwned(
                            definition,
                            context.rootConfigLexicalName,
                            new DataSourceKey(requiredName(definition)),
                            state);
                    break;
                case CONNECTION:
                    registerRootOwned(
                            definition,
                            context.rootConfigLexicalName,
                            new ConnectionKey(requiredName(definition)),
                            state);
                    break;
                case DATA:
                    context.leaveOwnedDocuments();
                    register(definition,
                            new DataKey(requiredName(definition)), state);
                    break;
                case VIEW:
                    context.leaveOwnedDocuments();
                    register(definition,
                            new ViewKey(requiredName(definition)), state);
                    break;
                case SYSTEM:
                    String systemLexicalName = requiredName(definition);
                    context.enterSystem(
                            new SystemKey(systemLexicalName),
                            systemLexicalName);
                    register(definition, context.systemKey, state);
                    break;
                case RULE_VIEW:
                    state.deferredRuleViews.add(definition);
                    break;
                case BUSINESS_SCOPE:
                    String scopeLexicalName = requiredName(definition);
                    context.enterBusinessScope(
                            new BusinessScopeKey(scopeLexicalName),
                            scopeLexicalName);
                    register(definition, context.scopeKey, state);
                    break;
                case DIRECTORY:
                    String directoryLexicalName = requiredName(definition);
                    if (requireOwner(
                            definition,
                            context.scopeLexicalName,
                            state)) {
                        context.enterDirectory(
                                new DirectoryKey(
                                        context.scopeKey,
                                        directoryLexicalName),
                                directoryLexicalName);
                        register(definition, context.directoryKey, state);
                    }
                    break;
                case ACTION:
                    String actionLexicalName = requiredName(definition);
                    if (requireOwner(
                            definition,
                            context.directoryLexicalName,
                            state)) {
                        context.enterAction(
                                new ActionKey(
                                        context.directoryKey,
                                        actionLexicalName),
                                actionLexicalName);
                        register(definition, context.actionKey, state);
                    }
                    break;
                case INFORMATION:
                case PRODUCE:
                case RULE:
                case MODEL_ACCESS:
                    break;
                default:
                    addDiagnostic(state, ownerDiagnostic(definition));
                    break;
            }
        }
    }

    /**
     * RuleView 延迟登记接缝。
     *
     * <p>Skeleton 已确保 RuleView 不再读取最近 System，但显式 owner 的存在性
     * 查找仍保持受控 RED，待独立 Skeleton Review 通过后实现。</p>
     */
    private static void registerRuleViews(RegistrationState state) {
        for (RawDefinition definition : state.deferredRuleViews) {
            addDiagnostic(state, new Diagnostic(
                    DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                    DiagnosticSeverity.ERROR,
                    "symbol.rule-view.registration.not-implemented",
                    null,
                    definition.sourceRef(),
                    Collections.<SourceRef>emptyList(),
                    "请在 Architecture Skeleton Review 后实现 RuleView 显式 owner 登记",
                    PASS));
        }
    }

    /**
     * 第二遍恢复第一遍登记的 owner Key，并使用原始 lexical 登记子定义。
     */
    private static void secondPass(
            List<RawDefinition> definitions,
            RegistrationState state) {
        SecondPassContext context = new SecondPassContext();
        for (RawDefinition definition : definitions) {
            switch (definition.kind()) {
                case ROOT_CONFIG:
                case DATA_SOURCE:
                case CONNECTION:
                case DATA:
                case VIEW:
                    context.leaveOwnedDocuments();
                    break;
                case SYSTEM:
                    context.enterSystem(
                            keyAtOrdinal(
                                    definition,
                                    SystemKey.class,
                                    state),
                            requiredName(definition));
                    break;
                case INFORMATION:
                    registerInformation(
                            definition,
                            context.systemKey,
                            context.systemLexicalName,
                            state);
                    break;
                case RULE_VIEW:
                case RULE:
                case MODEL_ACCESS:
                    break;
                case BUSINESS_SCOPE:
                    context.enterBusinessScope(
                            keyAtOrdinal(
                                    definition,
                                    BusinessScopeKey.class,
                                    state),
                            requiredName(definition));
                    break;
                case DIRECTORY:
                    context.enterDirectory(
                            keyAtOrdinal(
                                    definition,
                                    DirectoryKey.class,
                                    state),
                            requiredName(definition));
                    break;
                case ACTION:
                    context.enterAction(
                            keyAtOrdinal(
                                    definition,
                                    ActionKey.class,
                                    state),
                            requiredName(definition));
                    break;
                case PRODUCE:
                    registerProduce(
                            definition,
                            context.directoryKey,
                            context.directoryLexicalName,
                            context.actionKey,
                            context.actionLexicalName,
                            state);
                    break;
                default:
                    addDiagnostic(state, ownerDiagnostic(definition));
                    break;
            }
        }
    }

    /**
     * 在当前 System 下使用原始 lexical owner 校验并登记 InformationKey。
     */
    private static void registerInformation(
            RawDefinition definition,
            SystemKey systemKey,
            String systemLexicalName,
            RegistrationState state) {
        if (requireOwner(definition, systemLexicalName, state)) {
            register(definition,
                    new InformationKey(
                            systemKey,
                            requiredName(definition)),
                    state);
        }
    }

    /**
     * 使用原始 Directory/Action lexical composite 校验并登记 ProduceKey。
     */
    private static void registerProduce(
            RawDefinition definition,
            DirectoryKey directoryKey,
            String directoryLexicalName,
            ActionKey actionKey,
            String actionLexicalName,
            RegistrationState state) {
        String expectedOwner = directoryKey == null
                || actionKey == null
                || directoryLexicalName == null
                || actionLexicalName == null
                ? null
                : directoryLexicalName + "/" + actionLexicalName;
        if (!requireOwner(definition, expectedOwner, state)) {
            return;
        }
        long ordinal = definition.sourceOrdinal();
        if (ordinal < 0 || ordinal > Integer.MAX_VALUE) {
            addDiagnostic(state, singleDiagnostic(
                    DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                    "symbol.source-ordinal.out-of-range",
                    null,
                    definition.sourceRef(),
                    Collections.<SourceRef>emptyList()).get(0));
            return;
        }
        register(definition,
                new ProduceKey(actionKey, (int) ordinal),
                state);
    }

    /**
     * 从第一遍 ordinal 映射恢复精确类型的 owner Key。
     */
    private static <T extends DefinitionKey> T keyAtOrdinal(
            RawDefinition definition,
            Class<T> expectedType,
            RegistrationState state) {
        DefinitionKey key = state.keysByOrdinal.get(definition.sourceOrdinal());
        if (!expectedType.isInstance(key)) {
            addDiagnostic(state, ownerDiagnostic(definition));
            return null;
        }
        return expectedType.cast(key);
    }

    /**
     * 登记需要 ROOT_CONFIG lexical owner 的定义。
     */
    private static void registerRootOwned(
            RawDefinition definition,
            String rootConfigLexicalName,
            DefinitionKey key,
            RegistrationState state) {
        if (requireOwner(definition, rootConfigLexicalName, state)) {
            register(definition, key, state);
        }
    }

    /**
     * 校验 Raw ownerToken 与原始父定义 lexical name 完全一致。
     */
    private static boolean requireOwner(
            RawDefinition definition,
            String expectedOwnerLexical,
            RegistrationState state) {
        if (expectedOwnerLexical == null
                || !definition.ownerToken().isPresent()
                || !expectedOwnerLexical.equals(
                        definition.ownerToken().get())) {
            addDiagnostic(state, ownerDiagnostic(definition));
            return false;
        }
        return true;
    }

    /**
     * 在写入 TreeMap 前检查重复，首定义永不被后写覆盖。
     */
    private static void register(
            RawDefinition definition,
            DefinitionKey key,
            RegistrationState state) {
        RawDefinition first = state.entries.get(key);
        if (first != null) {
            addDiagnostic(state, duplicateDiagnostic(
                    key,
                    first,
                    definition));
        } else {
            state.entries.put(key, definition);
        }
        state.keysByOrdinal.put(definition.sourceOrdinal(), key);
    }

    /**
     * 向当前调用的 DiagnosticAccumulator 报告新事实。
     */
    private static void addDiagnostic(
            RegistrationState state,
            Diagnostic diagnostic) {
        state.diagnostics.add(diagnostic);
    }

    /**
     * 读取 T06 已强制存在且保留原始 lexical 的 definition name。
     */
    private static String requiredName(RawDefinition definition) {
        if (!definition.name().isPresent()) {
            throw new IllegalArgumentException(
                    "symbol definition name is required for "
                            + definition.kind());
        }
        return definition.name().get();
    }

    /**
     * 创建包含首定义和重复定义来源的稳定重复诊断。
     */
    private static Diagnostic duplicateDiagnostic(
            DefinitionKey key,
            RawDefinition first,
            RawDefinition duplicate) {
        return new Diagnostic(
                DiagnosticCode.MIX_SYMBOL_DUPLICATE,
                DiagnosticSeverity.ERROR,
                "symbol.duplicate",
                key,
                duplicate.sourceRef(),
                Collections.singletonList(first.sourceRef()),
                "请删除同一 TypedKey 的重复定义",
                PASS);
    }

    /**
     * 创建结构 owner lexical 上下文失配的 fail-closed 诊断。
     */
    private static Diagnostic ownerDiagnostic(RawDefinition definition) {
        return new Diagnostic(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                DiagnosticSeverity.ERROR,
                "symbol.owner.context.invalid",
                null,
                definition.sourceRef(),
                Collections.<SourceRef>emptyList(),
                "请修复 RawDefinition ownerToken 与原始父定义 lexical name 的对应关系",
                PASS);
    }

    /**
     * 创建单个稳定诊断列表。
     */
    private static List<Diagnostic> singleDiagnostic(
            DiagnosticCode code,
            String messageKey,
            DefinitionKey key,
            SourceRef sourceRef,
            List<SourceRef> relatedRefs) {
        return Collections.singletonList(new Diagnostic(
                code,
                DiagnosticSeverity.ERROR,
                messageKey,
                key,
                sourceRef,
                relatedRefs,
                "请修复 Symbol 注册输入或资源边界",
                PASS));
    }

    /**
     * 创建不携带部分 SymbolTable 的失败结果。
     */
    private static SymbolBuildResult failed(List<Diagnostic> diagnostics) {
        return SymbolBuildResult.failed(diagnostics);
    }

    /**
     * 单次 build 调用拥有的局部注册状态。
     */
    private static final class RegistrationState {
        private final Map<DefinitionKey, RawDefinition> entries =
                new TreeMap<DefinitionKey, RawDefinition>();
        private final Map<Long, DefinitionKey> keysByOrdinal =
                new TreeMap<Long, DefinitionKey>();
        private final List<RawDefinition> deferredRuleViews =
                new ArrayList<RawDefinition>();
        private final DiagnosticAccumulator diagnostics =
                new DiagnosticAccumulator();
    }

    /**
     * 第一遍维护的 canonical Key 与原始 lexical owner 上下文。
     */
    private static final class FirstPassContext {
        private String rootConfigLexicalName;
        private SystemKey systemKey;
        private String systemLexicalName;
        private BusinessScopeKey scopeKey;
        private String scopeLexicalName;
        private DirectoryKey directoryKey;
        private String directoryLexicalName;
        private ActionKey actionKey;
        private String actionLexicalName;

        /**
         * 进入 ROOT_CONFIG 文档并清空其他 owner 上下文。
         */
        private void enterRoot(String lexicalName) {
            rootConfigLexicalName = lexicalName;
            systemKey = null;
            systemLexicalName = null;
            scopeKey = null;
            scopeLexicalName = null;
            directoryKey = null;
            directoryLexicalName = null;
            actionKey = null;
            actionLexicalName = null;
        }

        /**
         * 进入 System，并同时保存 canonical Key 与原始 lexical name。
         */
        private void enterSystem(
                SystemKey key,
                String lexicalName) {
            rootConfigLexicalName = null;
            systemKey = key;
            systemLexicalName = lexicalName;
            scopeKey = null;
            scopeLexicalName = null;
            directoryKey = null;
            directoryLexicalName = null;
            actionKey = null;
            actionLexicalName = null;
        }

        /**
         * 进入 BusinessScope，并同时保存 Key 与原始 lexical name。
         */
        private void enterBusinessScope(
                BusinessScopeKey key,
                String lexicalName) {
            rootConfigLexicalName = null;
            systemKey = null;
            systemLexicalName = null;
            scopeKey = key;
            scopeLexicalName = lexicalName;
            directoryKey = null;
            directoryLexicalName = null;
            actionKey = null;
            actionLexicalName = null;
        }

        /**
         * 进入 Directory，并保存供 Action/Produce 使用的原始 lexical name。
         */
        private void enterDirectory(
                DirectoryKey key,
                String lexicalName) {
            directoryKey = key;
            directoryLexicalName = lexicalName;
            actionKey = null;
            actionLexicalName = null;
        }

        /**
         * 进入 Action，并保存供 Produce composite owner 使用的 lexical name。
         */
        private void enterAction(
                ActionKey key,
                String lexicalName) {
            actionKey = key;
            actionLexicalName = lexicalName;
        }

        /**
         * 离开任何有结构 owner 的文档上下文。
         */
        private void leaveOwnedDocuments() {
            rootConfigLexicalName = null;
            systemKey = null;
            systemLexicalName = null;
            scopeKey = null;
            scopeLexicalName = null;
            directoryKey = null;
            directoryLexicalName = null;
            actionKey = null;
            actionLexicalName = null;
        }
    }

    /**
     * 第二遍恢复的 canonical Key 与原始 lexical owner 上下文。
     */
    private static final class SecondPassContext {
        private SystemKey systemKey;
        private String systemLexicalName;
        private BusinessScopeKey scopeKey;
        private String scopeLexicalName;
        private DirectoryKey directoryKey;
        private String directoryLexicalName;
        private ActionKey actionKey;
        private String actionLexicalName;

        /**
         * 恢复 System Key 和同一 RawDefinition 的原始 lexical name。
         */
        private void enterSystem(
                SystemKey key,
                String lexicalName) {
            systemKey = key;
            systemLexicalName = lexicalName;
            scopeKey = null;
            scopeLexicalName = null;
            directoryKey = null;
            directoryLexicalName = null;
            actionKey = null;
            actionLexicalName = null;
        }

        /**
         * 恢复 BusinessScope Key 和原始 lexical name。
         */
        private void enterBusinessScope(
                BusinessScopeKey key,
                String lexicalName) {
            systemKey = null;
            systemLexicalName = null;
            scopeKey = key;
            scopeLexicalName = lexicalName;
            directoryKey = null;
            directoryLexicalName = null;
            actionKey = null;
            actionLexicalName = null;
        }

        /**
         * 恢复 Directory Key 和原始 lexical name。
         */
        private void enterDirectory(
                DirectoryKey key,
                String lexicalName) {
            directoryKey = key;
            directoryLexicalName = lexicalName;
            actionKey = null;
            actionLexicalName = null;
        }

        /**
         * 恢复 Action Key 和原始 lexical name。
         */
        private void enterAction(
                ActionKey key,
                String lexicalName) {
            actionKey = key;
            actionLexicalName = lexicalName;
        }

        /**
         * 清空所有 owner 上下文，避免跨独立文档泄漏。
         */
        private void leaveOwnedDocuments() {
            systemKey = null;
            systemLexicalName = null;
            scopeKey = null;
            scopeLexicalName = null;
            directoryKey = null;
            directoryLexicalName = null;
            actionKey = null;
            actionLexicalName = null;
        }
    }
}
