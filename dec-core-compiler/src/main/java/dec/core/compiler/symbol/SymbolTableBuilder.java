package dec.core.compiler.symbol;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
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
 * <p>结构 owner 使用 T06 保留的原始 lexical name 校验，TypedKey 独立执行
 * Context canonical 规则。RuleView 根据自身显式 ownerToken 在完整 System
 * 集合中登记，不依赖文档扫描顺序。</p>
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
     * 完成两遍 Symbol 注册；任一 ERROR 都不发布部分 SymbolTable。
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
        return SymbolBuildResult.built(new SymbolTable(state.entries, definitions));
    }

    /**
     * 第一遍登记顶层与结构 owner Key，并收集待显式解析 owner 的 RuleView。
     */
    private static void firstPass(
            List<RawDefinition> definitions,
            RegistrationState state) {
        OwnerContext context = new OwnerContext();
        for (RawDefinition definition : definitions) {
            switch (definition.kind()) {
                case ROOT_CONFIG:
                    context.enterRoot(requiredName(definition));
                    break;
                case DATA_SOURCE:
                    registerRootOwned(
                            definition,
                            context.rootLexicalName,
                            new DataSourceKey(requiredName(definition)),
                            state);
                    break;
                case CONNECTION:
                    registerRootOwned(
                            definition,
                            context.rootLexicalName,
                            new ConnectionKey(requiredName(definition)),
                            state);
                    break;
                case DATA:
                    context.clear();
                    register(definition,
                            new DataKey(requiredName(definition)), state);
                    break;
                case VIEW:
                    context.clear();
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
                    context.enterScope(
                            new BusinessScopeKey(scopeLexicalName),
                            scopeLexicalName);
                    register(definition, context.scopeKey, state);
                    break;
                case DIRECTORY:
                    registerDirectory(definition, context, state);
                    break;
                case ACTION:
                    registerAction(definition, context, state);
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
     * 使用原始 Scope lexical 校验并登记 Directory，同时阻止无效上下文泄漏。
     */
    private static void registerDirectory(
            RawDefinition definition,
            OwnerContext context,
            RegistrationState state) {
        String lexicalName = requiredName(definition);
        if (!requireOwner(definition, context.scopeLexicalName, state)
                || context.scopeKey == null) {
            context.clearDirectory();
            return;
        }
        DirectoryKey key = new DirectoryKey(
                context.scopeKey,
                lexicalName);
        context.enterDirectory(key, lexicalName);
        register(definition, key, state);
    }

    /**
     * 使用原始 Directory lexical 校验并登记 Action，同时阻止无效上下文泄漏。
     */
    private static void registerAction(
            RawDefinition definition,
            OwnerContext context,
            RegistrationState state) {
        String lexicalName = requiredName(definition);
        if (!requireOwner(
                definition,
                context.directoryLexicalName,
                state)
                || context.directoryKey == null) {
            context.clearAction();
            return;
        }
        ActionKey key = new ActionKey(
                context.directoryKey,
                lexicalName);
        context.enterAction(key, lexicalName);
        register(definition, key, state);
    }

    /**
     * 在全部 System 登记完成后，根据 RuleView 自身 ownerToken 登记身份。
     */
    private static void registerRuleViews(RegistrationState state) {
        for (RawDefinition definition : state.deferredRuleViews) {
            if (!definition.ownerToken().isPresent()) {
                addDiagnostic(state, ownerDiagnostic(definition));
                continue;
            }
            SystemKey targetSystem = new SystemKey(
                    definition.ownerToken().get());
            RawDefinition ownerDefinition = state.entries.get(targetSystem);
            if (ownerDefinition == null
                    || ownerDefinition.kind() != RawDefinitionKind.SYSTEM) {
                addDiagnostic(state, missingSystemDiagnostic(
                        definition,
                        targetSystem));
                continue;
            }
            register(definition,
                    new RuleViewKey(
                            targetSystem,
                            requiredName(definition)),
                    state);
        }
    }

    /**
     * 第二遍恢复 Key 与同一 RawDefinition 的 lexical name，登记 Information/Produce。
     */
    private static void secondPass(
            List<RawDefinition> definitions,
            RegistrationState state) {
        OwnerContext context = new OwnerContext();
        for (RawDefinition definition : definitions) {
            switch (definition.kind()) {
                case ROOT_CONFIG:
                case DATA_SOURCE:
                case CONNECTION:
                case DATA:
                case VIEW:
                    context.clear();
                    break;
                case SYSTEM:
                    SystemKey systemKey = keyAtOrdinal(
                            definition,
                            SystemKey.class,
                            state);
                    context.enterSystem(
                            systemKey,
                            systemKey == null
                                    ? null
                                    : requiredName(definition));
                    break;
                case INFORMATION:
                    registerInformation(definition, context, state);
                    break;
                case RULE_VIEW:
                case RULE:
                case MODEL_ACCESS:
                    break;
                case BUSINESS_SCOPE:
                    BusinessScopeKey scopeKey = keyAtOrdinal(
                            definition,
                            BusinessScopeKey.class,
                            state);
                    context.enterScope(
                            scopeKey,
                            scopeKey == null
                                    ? null
                                    : requiredName(definition));
                    break;
                case DIRECTORY:
                    DirectoryKey directoryKey = keyAtOrdinal(
                            definition,
                            DirectoryKey.class,
                            state);
                    context.enterDirectory(
                            directoryKey,
                            directoryKey == null
                                    ? null
                                    : requiredName(definition));
                    break;
                case ACTION:
                    ActionKey actionKey = keyAtOrdinal(
                            definition,
                            ActionKey.class,
                            state);
                    context.enterAction(
                            actionKey,
                            actionKey == null
                                    ? null
                                    : requiredName(definition));
                    break;
                case PRODUCE:
                    registerProduce(definition, context, state);
                    break;
                default:
                    addDiagnostic(state, ownerDiagnostic(definition));
                    break;
            }
        }
    }

    /**
     * 使用原始 System lexical owner 校验并创建 canonical InformationKey。
     */
    private static void registerInformation(
            RawDefinition definition,
            OwnerContext context,
            RegistrationState state) {
        if (context.systemKey == null
                || context.systemLexicalName == null) {
            addDiagnostic(state, ownerDiagnostic(definition));
            return;
        }
        if (!requireOwner(
                definition,
                context.systemLexicalName,
                state)) {
            return;
        }
        register(definition,
                new InformationKey(
                        context.systemKey,
                        requiredName(definition)),
                state);
    }

    /**
     * 使用原始 Directory/Action composite owner 校验并登记 ProduceKey。
     */
    private static void registerProduce(
            RawDefinition definition,
            OwnerContext context,
            RegistrationState state) {
        String expectedOwner = context.directoryKey == null
                || context.actionKey == null
                || context.directoryLexicalName == null
                || context.actionLexicalName == null
                ? null
                : context.directoryLexicalName
                        + "/"
                        + context.actionLexicalName;
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
                new ProduceKey(
                        context.actionKey,
                        (int) ordinal),
                state);
    }

    /**
     * 从第一遍 ordinal 映射恢复精确类型的 TypedKey。
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
     * 登记需要 ROOT_CONFIG 原始 lexical owner 的定义。
     */
    private static void registerRootOwned(
            RawDefinition definition,
            String rootLexicalName,
            DefinitionKey key,
            RegistrationState state) {
        if (requireOwner(definition, rootLexicalName, state)) {
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
     * 在 TreeMap 写入前检查重复，首定义永不被后写覆盖。
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
     * 以一次哈希集合 add 向当前调用报告 Diagnostic。
     */
    private static void addDiagnostic(
            RegistrationState state,
            Diagnostic diagnostic) {
        state.diagnostics.add(diagnostic);
    }

    /**
     * 读取 T06 已强制存在并保留原始 lexical 的 definition name。
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
                "请修复 ownerToken 与原始父定义 lexical name 的对应关系",
                PASS);
    }

    /**
     * 创建 RuleView 显式 System owner 不存在的稳定诊断。
     */
    private static Diagnostic missingSystemDiagnostic(
            RawDefinition definition,
            SystemKey targetSystem) {
        return new Diagnostic(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                DiagnosticSeverity.ERROR,
                "symbol.owner.system.missing",
                targetSystem,
                definition.sourceRef(),
                Collections.<SourceRef>emptyList(),
                "请声明 RuleView ownerToken 指向的 System",
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
     * 同时保存 canonical TypedKey 与原始 lexical owner 的调用级上下文。
     */
    private static final class OwnerContext {
        private String rootLexicalName;
        private SystemKey systemKey;
        private String systemLexicalName;
        private BusinessScopeKey scopeKey;
        private String scopeLexicalName;
        private DirectoryKey directoryKey;
        private String directoryLexicalName;
        private ActionKey actionKey;
        private String actionLexicalName;

        /**
         * 进入 ROOT_CONFIG 并清空其他结构 owner。
         */
        private void enterRoot(String lexicalName) {
            clear();
            rootLexicalName = lexicalName;
        }

        /**
         * 进入 System 并同时保存 Key 与原始 lexical name。
         */
        private void enterSystem(SystemKey key, String lexicalName) {
            clear();
            systemKey = key;
            systemLexicalName = lexicalName;
        }

        /**
         * 进入 BusinessScope 并同时保存 Key 与原始 lexical name。
         */
        private void enterScope(
                BusinessScopeKey key,
                String lexicalName) {
            clear();
            scopeKey = key;
            scopeLexicalName = lexicalName;
        }

        /**
         * 进入 Directory，并清空旧 Action 上下文。
         */
        private void enterDirectory(
                DirectoryKey key,
                String lexicalName) {
            directoryKey = key;
            directoryLexicalName = lexicalName;
            clearAction();
        }

        /**
         * 进入 Action 并保存 Produce 所需的原始 lexical name。
         */
        private void enterAction(ActionKey key, String lexicalName) {
            actionKey = key;
            actionLexicalName = lexicalName;
        }

        /**
         * 清空 Directory 与其下游 Action，阻止无效 owner 泄漏。
         */
        private void clearDirectory() {
            directoryKey = null;
            directoryLexicalName = null;
            clearAction();
        }

        /**
         * 清空 Action 上下文，阻止后续 Produce 使用陈旧 owner。
         */
        private void clearAction() {
            actionKey = null;
            actionLexicalName = null;
        }

        /**
         * 清空全部 owner 上下文，避免跨独立文档状态泄漏。
         */
        private void clear() {
            rootLexicalName = null;
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
