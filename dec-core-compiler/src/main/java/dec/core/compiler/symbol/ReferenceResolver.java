package dec.core.compiler.symbol;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.raw.RawReference;
import dec.core.context.model.DataKey;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
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
 * TASK-P1-T08 的真实 Architecture Skeleton。
 *
 * <p>当前版本已建立所有只读索引和单次调用状态，但 Role Policy 仍固定返回
 * not-implemented Diagnostic，用于证明架构接缝不会提前伪造 GREEN。</p>
 */
public final class ReferenceResolver {
    private static final String PASS = "reference-resolution";
    private static final SourceRef UNKNOWN_SOURCE =
            new SourceRef("<unknown-reference-source>", 0, 0, "/");

    /**
     * 建立完整索引后保留受控失败，下一阶段再接入精确 Role Policy。
     */
    public ReferenceResolutionResult resolve(
            RawDefinitionSet definitions,
            SymbolTable symbolTable) {
        if (definitions == null || symbolTable == null) {
            return ReferenceResolutionResult.failed(Collections.singletonList(
                    diagnostic(UNKNOWN_SOURCE, "reference.input.required")));
        }
        ResolutionState state = new ResolutionState(symbolTable);
        indexSymbols(state);
        indexDataProperties(state);
        indexSystemViewDeclarations(definitions, state);

        SourceRef sourceRef = definitions.definitions().isEmpty()
                ? UNKNOWN_SOURCE
                : definitions.definitions().get(0).sourceRef();
        state.diagnostics.add(diagnostic(
                sourceRef,
                "reference.role-policy.not-implemented"));
        return ReferenceResolutionResult.failed(
                new ArrayList<Diagnostic>(state.diagnostics));
    }

    /**
     * 对齐 SymbolTable 的稳定 Key/Definition 序列，建立来源和失败分类索引。
     */
    private static void indexSymbols(ResolutionState state) {
        List<DefinitionKey> keys = state.symbolTable.keys();
        List<RawDefinition> definitions = state.symbolTable.definitions();
        for (int index = 0; index < keys.size(); index++) {
            DefinitionKey key = keys.get(index);
            RawDefinition definition = definitions.get(index);
            state.sourceKeys.put(definition.sourceOrdinal(), key);
            state.definitionsByKey.put(key, definition);
            String lexical = definition.name().isPresent()
                    ? definition.name().get().trim()
                    : key.canonical();
            List<DefinitionKey> candidates = state.keysByLexical.get(lexical);
            if (candidates == null) {
                candidates = new ArrayList<DefinitionKey>();
                state.keysByLexical.put(lexical, candidates);
            }
            candidates.add(key);
        }
    }

    /**
     * 从 Data Raw body 收集区分大小写的属性名称，禁止建立平行 PropertyKey。
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

    /** 递归收集 Data body 内 property@name。 */
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
     * 在 Role Policy 运行前建立每个 System 显式声明的 ViewKey 集合。
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
                    if (reference.role().contains("view-info/view-ref")) {
                        views.add(new ViewKey(reference.target()));
                    }
                }
            }
            state.systemViews.put((SystemKey) key, views);
        }
    }

    /** 递归收集 System body 中 view-ref 的 ref/name。 */
    private static void collectSystemViews(
            RawNodeBody node,
            Set<ViewKey> views) {
        if ("view-ref".equals(node.name())) {
            String target = node.attributes().get("ref");
            if (target == null) {
                target = node.attributes().get("name");
            }
            if (target != null) {
                views.add(new ViewKey(target));
            }
        }
        for (RawNodeBody child : node.children()) {
            collectSystemViews(child, views);
        }
    }

    /** 创建 Architecture Skeleton 的稳定受控 ERROR。 */
    private static Diagnostic diagnostic(
            SourceRef sourceRef,
            String messageKey) {
        return new Diagnostic(
                DiagnosticCode.MIX_REF_UNKNOWN,
                DiagnosticSeverity.ERROR,
                messageKey,
                null,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                "请完成 TASK-P1-T08 精确 Role Policy",
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
