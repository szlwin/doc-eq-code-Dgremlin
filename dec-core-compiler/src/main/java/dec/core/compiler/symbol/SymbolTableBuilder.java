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
 * <p>Builder 先登记顶层和 owner TypedKey，再在同一不可变 RawDefinitionSet 上
 * 登记 Information 与 Produce。只有两遍全部完成且无错误时才发布完整表。</p>
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
        secondPass(ordered, state);
        if (!state.diagnostics.isEmpty()) {
            return failed(state.diagnostics);
        }
        return SymbolBuildResult.built(new SymbolTable(state.entries));
    }

    /**
     * 第一遍登记顶层和可作为 owner 的 TypedKey，并冻结 ordinal 到 Key 的映射。
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
                            context.rootConfigName,
                            new DataSourceKey(requiredName(definition)),
                            state);
                    break;
                case CONNECTION:
                    registerRootOwned(
                            definition,
                            context.rootConfigName,
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
                    context.enterSystem(new SystemKey(requiredName(definition)));
                    register(definition, context.system, state);
                    break;
                case RULE_VIEW:
                    if (requireOwner(definition, context.system == null
                            ? null : context.system.name(), state)) {
                        register(definition,
                                new RuleViewKey(
                                        context.system,
                                        requiredName(definition)),
                                state);
                    }
                    break;
                case BUSINESS_SCOPE:
                    context.enterBusinessScope(
                            new BusinessScopeKey(requiredName(definition)));
                    register(definition, context.scope, state);
                    break;
                case DIRECTORY:
                    if (requireOwner(definition, context.scope == null
                            ? null : context.scope.name(), state)) {
                        context.enterDirectory(new DirectoryKey(
                                context.scope,
                                requiredName(definition)));
                        register(definition, context.directory, state);
                    }
                    break;
                case ACTION:
                    if (requireOwner(definition, context.directory == null
                            ? null : context.directory.name(), state)) {
                        context.enterAction(new ActionKey(
                                context.directory,
                                requiredName(definition)));
                        register(definition, context.action, state);
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
     * 第二遍恢复第一遍登记的 owner Key，并登记 Information 与 Produce。
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
                    context.enterSystem(keyAtOrdinal(
                            definition,
                            SystemKey.class,
                            state));
                    break;
                case INFORMATION:
                    registerInformation(definition, context.system, state);
                    break;
                case RULE_VIEW:
                case RULE:
                case MODEL_ACCESS:
                    break;
                case BUSINESS_SCOPE:
                    context.enterBusinessScope(keyAtOrdinal(
                            definition,
                            BusinessScopeKey.class,
                            state));
                    break;
                case DIRECTORY:
                    context.enterDirectory(keyAtOrdinal(
                            definition,
                            DirectoryKey.class,
                            state));
                    break;
                case ACTION:
                    context.enterAction(keyAtOrdinal(
                            definition,
                            ActionKey.class,
                            state));
                    break;
                case PRODUCE:
                    registerProduce(
                            definition,
                            context.directory,
                            context.action,
                            state);
                    break;
                default:
                    addDiagnostic(state, ownerDiagnostic(definition));
                    break;
            }
        }
    }

    /**
     * 在当前 System owner 下登记 InformationKey。
     */
    private static void registerInformation(
            RawDefinition definition,
            SystemKey system,
            RegistrationState state) {
        if (requireOwner(definition,
                system == null ? null : system.name(), state)) {
            register(definition,
                    new InformationKey(system, requiredName(definition)),
                    state);
        }
    }

    /**
     * 在当前 Action owner 下登记以 sourceOrdinal 区分的 ProduceKey。
     */
    private static void registerProduce(
            RawDefinition definition,
            DirectoryKey directory,
            ActionKey action,
            RegistrationState state) {
        String expectedOwner = directory == null || action == null
                ? null
                : directory.name() + "/" + action.actionName();
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
                new ProduceKey(action, (int) ordinal),
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
     * 登记需要 ROOT_CONFIG owner 的定义。
     */
    private static void registerRootOwned(
            RawDefinition definition,
            String rootConfigName,
            DefinitionKey key,
            RegistrationState state) {
        if (requireOwner(definition, rootConfigName, state)) {
            register(definition, key, state);
        }
    }

    /**
     * 校验 Raw ownerToken 与当前结构 owner 完全一致。
     */
    private static boolean requireOwner(
            RawDefinition definition,
            String expectedOwner,
            RegistrationState state) {
        if (expectedOwner == null
                || !definition.ownerToken().isPresent()
                || !expectedOwner.equals(definition.ownerToken().get())) {
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
     * 向当前调用的 Diagnostic 集合加入新事实，并去除完全相同的重复诊断。
     */
    private static void addDiagnostic(
            RegistrationState state,
            Diagnostic diagnostic) {
        if (!state.diagnostics.contains(diagnostic)) {
            state.diagnostics.add(diagnostic);
        }
    }

    /**
     * 读取 T06 已强制存在的 definition name。
     */
    private static String requiredName(RawDefinition definition) {
        if (!definition.name().isPresent()) {
            throw new IllegalArgumentException(
                    "symbol definition name is required for " + definition.kind());
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
     * 创建 owner 上下文失配的 fail-closed 诊断。
     */
    private static Diagnostic ownerDiagnostic(RawDefinition definition) {
        return new Diagnostic(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                DiagnosticSeverity.ERROR,
                "symbol.owner.context.invalid",
                null,
                definition.sourceRef(),
                Collections.<SourceRef>emptyList(),
                "请修复 RawDefinition ownerToken 与结构 owner 的对应关系",
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
        private final List<Diagnostic> diagnostics =
                new ArrayList<Diagnostic>();
    }

    /**
     * 第一遍按 sourceOrdinal 维护的 owner 上下文。
     */
    private static final class FirstPassContext {
        private String rootConfigName;
        private SystemKey system;
        private BusinessScopeKey scope;
        private DirectoryKey directory;
        private ActionKey action;

        private void enterRoot(String name) {
            rootConfigName = name;
            system = null;
            scope = null;
            directory = null;
            action = null;
        }

        private void enterSystem(SystemKey key) {
            rootConfigName = null;
            system = key;
            scope = null;
            directory = null;
            action = null;
        }

        private void enterBusinessScope(BusinessScopeKey key) {
            rootConfigName = null;
            system = null;
            scope = key;
            directory = null;
            action = null;
        }

        private void enterDirectory(DirectoryKey key) {
            directory = key;
            action = null;
        }

        private void enterAction(ActionKey key) {
            action = key;
        }

        private void leaveOwnedDocuments() {
            rootConfigName = null;
            system = null;
            scope = null;
            directory = null;
            action = null;
        }
    }

    /**
     * 第二遍恢复的 System 与 Business owner 上下文。
     */
    private static final class SecondPassContext {
        private SystemKey system;
        private BusinessScopeKey scope;
        private DirectoryKey directory;
        private ActionKey action;

        private void enterSystem(SystemKey key) {
            system = key;
            scope = null;
            directory = null;
            action = null;
        }

        private void enterBusinessScope(BusinessScopeKey key) {
            system = null;
            scope = key;
            directory = null;
            action = null;
        }

        private void enterDirectory(DirectoryKey key) {
            directory = key;
            action = null;
        }

        private void enterAction(ActionKey key) {
            action = key;
        }

        private void leaveOwnedDocuments() {
            system = null;
            scope = null;
            directory = null;
            action = null;
        }
    }
}
