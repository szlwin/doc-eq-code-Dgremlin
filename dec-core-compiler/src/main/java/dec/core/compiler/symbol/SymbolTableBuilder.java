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
 * <p>Architecture Skeleton 已冻结调用级状态、第一遍 owner 注册、重复诊断和
 * 第二遍接缝；第二遍行为暂时保持受控 RED，待独立 Skeleton Review 通过后实现。</p>
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
        if (state.diagnostics.isEmpty()) {
            secondPass(ordered, state);
        }
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
                    context.rootConfigName = requiredName(definition);
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
                    register(definition,
                            new DataKey(requiredName(definition)), state);
                    break;
                case VIEW:
                    register(definition,
                            new ViewKey(requiredName(definition)), state);
                    break;
                case SYSTEM:
                    context.system = new SystemKey(requiredName(definition));
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
                    context.scope = new BusinessScopeKey(requiredName(definition));
                    context.directory = null;
                    context.action = null;
                    register(definition, context.scope, state);
                    break;
                case DIRECTORY:
                    if (requireOwner(definition, context.scope == null
                            ? null : context.scope.name(), state)) {
                        context.directory = new DirectoryKey(
                                context.scope,
                                requiredName(definition));
                        context.action = null;
                        register(definition, context.directory, state);
                    }
                    break;
                case ACTION:
                    if (requireOwner(definition, context.directory == null
                            ? null : context.directory.name(), state)) {
                        context.action = new ActionKey(
                                context.directory,
                                requiredName(definition));
                        register(definition, context.action, state);
                    }
                    break;
                case INFORMATION:
                case PRODUCE:
                case RULE:
                case MODEL_ACCESS:
                    break;
                default:
                    state.diagnostics.add(ownerDiagnostic(definition));
                    break;
            }
        }
    }

    /**
     * 第二遍接缝将在 GREEN 阶段登记 Information 与 Produce。
     *
     * <p>Skeleton 保留显式失败，证明结构存在但行为尚未被伪造为完成。</p>
     */
    private static void secondPass(
            List<RawDefinition> definitions,
            RegistrationState state) {
        SourceRef sourceRef = definitions.isEmpty()
                ? UNKNOWN_SOURCE
                : definitions.get(0).sourceRef();
        state.diagnostics.add(new Diagnostic(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                DiagnosticSeverity.ERROR,
                "symbol.second-pass.not-implemented",
                null,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                "请在 Architecture Skeleton Review 通过后实现第二遍注册",
                PASS));
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
            state.diagnostics.add(ownerDiagnostic(definition));
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
            state.diagnostics.add(duplicateDiagnostic(
                    key,
                    first,
                    definition));
        } else {
            state.entries.put(key, definition);
        }
        state.keysByOrdinal.put(definition.sourceOrdinal(), key);
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
    }
}
