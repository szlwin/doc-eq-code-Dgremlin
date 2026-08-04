package dec.core.compiler.modelaccess;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DeferredKind;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.NormalizedBody;
import dec.core.context.model.RequiredStage;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 将 T06 ModelAccess 结构事实编译为精确 Binding 与 P2 Deferred 的无状态协调器。
 */
public final class ModelAccessCompiler {
    private final ModelAccessSelectorResolver resolver;
    private final ModelAccessStructureValidator structureValidator;

    /** 创建使用生产 selector seam 的编译器。 */
    public ModelAccessCompiler() {
        this(new DefaultModelAccessSelectorResolver());
    }

    /** 创建可注入 selector 的编译器，供门禁和边界测试使用。 */
    public ModelAccessCompiler(ModelAccessSelectorResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.structureValidator = new ModelAccessStructureValidator();
    }

    /**
     * 编译完整 RawDefinitionSet；任一 ERROR 都不发布部分 Binding 或 Deferred。
     */
    public ModelAccessCompilationResult compile(
            RawDefinitionSet definitions,
            SymbolTable symbols) {
        if (definitions == null || symbols == null) {
            return ModelAccessCompilationResult.failed(
                    Collections.singletonList(
                            ModelAccessDiagnostics.inputRequired()));
        }
        // 完整快照门禁必须先于 owner、selector、resolver 和 Deferred 工作。
        if (!symbols.isBuiltFrom(definitions)) {
            return ModelAccessCompilationResult.failed(
                    Collections.singletonList(
                            ModelAccessDiagnostics.snapshotMismatch(definitions)));
        }

        Set<Diagnostic> diagnostics = new LinkedHashSet<Diagnostic>();
        List<ModelAccessBinding> bindings = new ArrayList<ModelAccessBinding>();
        Set<BindingIdentity> bindingIdentities =
                new LinkedHashSet<BindingIdentity>();
        Map<DeferredKey, DeferredDefinition> deferred =
                new TreeMap<DeferredKey, DeferredDefinition>();
        Map<SystemKey, Integer> ordinals = new LinkedHashMap<SystemKey, Integer>();

        for (RawDefinition definition
                : definitions.definitions(RawDefinitionKind.MODEL_ACCESS)) {
            compileDefinition(
                    definition,
                    symbols,
                    diagnostics,
                    bindings,
                    bindingIdentities,
                    deferred,
                    ordinals);
        }

        if (!diagnostics.isEmpty()) {
            return ModelAccessCompilationResult.failed(
                    ModelAccessDiagnostics.sorted(diagnostics));
        }
        return ModelAccessCompilationResult.compiled(
                new ModelAccessCompilation(
                        bindings,
                        new ImmutableDeferredRegistry(deferred)));
    }

    /** 编译单个 ModelAccess，并把阶段内临时事实写入批次累积器。 */
    private void compileDefinition(
            RawDefinition definition,
            SymbolTable symbols,
            Set<Diagnostic> diagnostics,
            List<ModelAccessBinding> bindings,
            Set<BindingIdentity> bindingIdentities,
            Map<DeferredKey, DeferredDefinition> deferred,
            Map<SystemKey, Integer> ordinals) {
        // 根结构门禁必须先于 owner、source View、resolver 与 Deferred 工作。
        List<Diagnostic> structureDiagnostics =
                structureValidator.validate(definition);
        if (!structureDiagnostics.isEmpty()) {
            diagnostics.addAll(structureDiagnostics);
            return;
        }

        SystemKey owner = ownerKey(definition, symbols, diagnostics);
        ViewKey sourceView = sourceViewKey(definition, symbols, diagnostics);
        if (owner == null || sourceView == null) {
            return;
        }

        WritePathOverlapIndex writePathIndex = new WritePathOverlapIndex();
        Set<DefinitionKey> resolvedReferences = new TreeSet<DefinitionKey>();
        resolvedReferences.add(sourceView);
        StringBuilder normalized = new StringBuilder();
        normalized.append("owner=").append(owner.name())
                .append(";source=").append(sourceView.name());

        for (RawNodeBody access : definition.body().children()) {
            compileAccess(
                    definition,
                    access,
                    owner,
                    sourceView,
                    symbols,
                    diagnostics,
                    bindings,
                    bindingIdentities,
                    resolvedReferences,
                    writePathIndex,
                    normalized);
        }

        int ordinal = nextOrdinal(owner, ordinals);
        DeferredKey key = new DeferredKey(
                owner,
                DeferredKind.MODEL_ACCESS,
                ordinal);
        DeferredDefinition value = new DeferredDefinition(
                key,
                RequiredStage.P2,
                "model-access-selector-binding",
                definition.sourceRef(),
                new NormalizedBody(
                        "model-access-binding/v1",
                        normalized.toString()),
                new ArrayList<DefinitionKey>(resolvedReferences));
        deferred.put(key, value);
    }

    /** 编译单个 read/write 节点并解析其全部 ref。 */
    private void compileAccess(
            RawDefinition definition,
            RawNodeBody access,
            SystemKey owner,
            ViewKey sourceView,
            SymbolTable symbols,
            Set<Diagnostic> diagnostics,
            List<ModelAccessBinding> bindings,
            Set<BindingIdentity> bindingIdentities,
            Set<DefinitionKey> resolvedReferences,
            WritePathOverlapIndex writePathIndex,
            StringBuilder normalized) {
        AccessMode mode = accessMode(access, diagnostics);
        SharedModelPath sourcePath = sourcePath(access, diagnostics);
        if (mode == null || sourcePath == null) {
            return;
        }
        if (mode == AccessMode.WRITE && writePathIndex.add(sourcePath)) {
            diagnostics.add(ModelAccessDiagnostics.writeOverlap(
                    definition.sourceRef()));
        }

        normalized.append(';')
                .append(mode.name().toLowerCase())
                .append('(')
                .append(sourcePath.value())
                .append(")=[");
        boolean first = true;
        for (RawNodeBody ref : access.children()) {
            if (!"ref".equals(ref.name())) {
                diagnostics.add(ModelAccessDiagnostics.structureInvalid(
                        ref.sourceRef()));
                continue;
            }
            ViewKey targetView = targetViewKey(ref, diagnostics);
            SystemViewSelector selector = selector(ref, diagnostics);
            if (targetView == null || selector == null) {
                continue;
            }
            ModelAccessResolution resolution;
            try {
                resolution = resolver.resolve(
                        owner,
                        sourcePath,
                        targetView,
                        selector,
                        symbols);
            } catch (RuntimeException failure) {
                diagnostics.add(ModelAccessDiagnostics.structureInvalid(
                        ref.sourceRef()));
                continue;
            }
            diagnostics.addAll(resolution.diagnostics());
            if (!resolution.target().isPresent()) {
                continue;
            }

            TargetPropertyPath target = resolution.target().get();
            ModelAccessBinding binding = new ModelAccessBinding(
                    owner,
                    sourceView,
                    sourcePath,
                    mode,
                    targetView,
                    selector,
                    target,
                    ref.sourceRef());
            BindingIdentity identity = new BindingIdentity(binding);
            if (!bindingIdentities.add(identity)) {
                diagnostics.add(ModelAccessDiagnostics.duplicateBinding(
                        targetView,
                        ref.sourceRef()));
                continue;
            }
            bindings.add(binding);
            resolvedReferences.add(targetView);
            if (!first) {
                normalized.append(',');
            }
            normalized.append(targetView.name())
                    .append('#')
                    .append(target.toString());
            first = false;
        }
        normalized.append(']');
    }

    /** 安全构造并校验当前 System owner。 */
    private static SystemKey ownerKey(
            RawDefinition definition,
            SymbolTable symbols,
            Set<Diagnostic> diagnostics) {
        if (!definition.ownerToken().isPresent()) {
            diagnostics.add(ModelAccessDiagnostics.ownerInvalid(
                    definition.sourceRef()));
            return null;
        }
        try {
            SystemKey owner = new SystemKey(definition.ownerToken().get());
            Optional<RawDefinition> registered = symbols.find(owner);
            if (!registered.isPresent()
                    || registered.get().kind() != RawDefinitionKind.SYSTEM) {
                diagnostics.add(ModelAccessDiagnostics.ownerInvalid(
                        definition.sourceRef()));
                return null;
            }
            return owner;
        } catch (IllegalArgumentException failure) {
            diagnostics.add(ModelAccessDiagnostics.ownerInvalid(
                    definition.sourceRef()));
            return null;
        }
    }

    /** 安全构造并校验共享模型 View。 */
    private static ViewKey sourceViewKey(
            RawDefinition definition,
            SymbolTable symbols,
            Set<Diagnostic> diagnostics) {
        String lexical = definition.attributes().get("model-ref");
        ViewKey key = safeViewKey(lexical);
        if (key == null) {
            diagnostics.add(ModelAccessDiagnostics.sourceViewNotFound(
                    null,
                    definition.sourceRef()));
            return null;
        }
        Optional<RawDefinition> registered = symbols.find(key);
        if (!registered.isPresent()
                || registered.get().kind() != RawDefinitionKind.VIEW) {
            diagnostics.add(ModelAccessDiagnostics.sourceViewNotFound(
                    key,
                    definition.sourceRef()));
            return null;
        }
        return key;
    }

    /** 解析 read/write 模式。 */
    private static AccessMode accessMode(
            RawNodeBody access,
            Set<Diagnostic> diagnostics) {
        if ("read".equals(access.name())) {
            return AccessMode.READ;
        }
        if ("write".equals(access.name())) {
            return AccessMode.WRITE;
        }
        diagnostics.add(ModelAccessDiagnostics.structureInvalid(
                access.sourceRef()));
        return null;
    }

    /** 解析精确共享模型源路径。 */
    private static SharedModelPath sourcePath(
            RawNodeBody access,
            Set<Diagnostic> diagnostics) {
        try {
            return new SharedModelPath(access.attributes().get("path"));
        } catch (IllegalArgumentException failure) {
            diagnostics.add(ModelAccessDiagnostics.structureInvalid(
                    access.sourceRef()));
            return null;
        }
    }

    /** 解析 ref@view。 */
    private static ViewKey targetViewKey(
            RawNodeBody ref,
            Set<Diagnostic> diagnostics) {
        ViewKey key = safeViewKey(ref.attributes().get("view"));
        if (key == null) {
            diagnostics.add(ModelAccessDiagnostics.structureInvalid(
                    ref.sourceRef()));
        }
        return key;
    }

    /** 解析 ref@property。 */
    private static SystemViewSelector selector(
            RawNodeBody ref,
            Set<Diagnostic> diagnostics) {
        try {
            return new SystemViewSelector(ref.attributes().get("property"));
        } catch (IllegalArgumentException failure) {
            diagnostics.add(ModelAccessDiagnostics.structureInvalid(
                    ref.sourceRef()));
            return null;
        }
    }

    /** 安全构造 ViewKey，不泄漏调用方输入异常。 */
    private static ViewKey safeViewKey(String lexical) {
        try {
            return lexical == null ? null : new ViewKey(lexical);
        } catch (IllegalArgumentException failure) {
            return null;
        }
    }

    /** 返回当前 owner 的稳定 Deferred ordinal，并递增下次值。 */
    private static int nextOrdinal(
            SystemKey owner,
            Map<SystemKey, Integer> ordinals) {
        Integer current = ordinals.get(owner);
        int ordinal = current == null ? 0 : current;
        ordinals.put(owner, ordinal + 1);
        return ordinal;
    }

    /**
     * 不含 SourceRef 的 Binding 身份，确保重复声明即使位置不同也会被拒绝。
     */
    private static final class BindingIdentity {
        private final SystemKey owner;
        private final ViewKey sourceView;
        private final SharedModelPath sourcePath;
        private final AccessMode mode;
        private final ViewKey targetView;
        private final SystemViewSelector selector;
        private final TargetPropertyPath target;

        private BindingIdentity(ModelAccessBinding binding) {
            this.owner = binding.ownerSystem();
            this.sourceView = binding.sourceModel();
            this.sourcePath = binding.sourcePath();
            this.mode = binding.accessMode();
            this.targetView = binding.targetView();
            this.selector = binding.selector();
            this.target = binding.resolvedTarget();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof BindingIdentity)) {
                return false;
            }
            BindingIdentity that = (BindingIdentity) other;
            return owner.equals(that.owner)
                    && sourceView.equals(that.sourceView)
                    && sourcePath.equals(that.sourcePath)
                    && mode == that.mode
                    && targetView.equals(that.targetView)
                    && selector.equals(that.selector)
                    && target.equals(that.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    owner,
                    sourceView,
                    sourcePath,
                    mode,
                    targetView,
                    selector,
                    target);
        }
    }
}
