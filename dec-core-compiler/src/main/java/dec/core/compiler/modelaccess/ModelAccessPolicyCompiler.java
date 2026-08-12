package dec.core.compiler.modelaccess;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.AccessCompilationStatus;
import dec.core.context.model.AccessOperation;
import dec.core.context.model.CompiledModelAccessRule;
import dec.core.context.model.CompiledTargetBinding;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.ModelAccessPolicyIndex;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.ModelPath;
import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.model.TargetKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** DEV-03 静态授权分类与精确 RuntimeBindingPlan 编译器。 */
public final class ModelAccessPolicyCompiler {
    private final ModelPathCompiler pathCompiler = new ModelPathCompiler();

    /**
     * 将 P1 结构 Binding 转成 P2 exact policy。任何路径/绑定错误都原子失败，不发布部分 Index。
     */
    public ModelAccessPolicyCompilationResult compile(
            ModelAccessCompilation compilation,
            SymbolTable symbols) {
        Objects.requireNonNull(compilation, "compilation");
        Objects.requireNonNull(symbols, "symbols");
        List<CompiledModelAccessRule> rules = new ArrayList<CompiledModelAccessRule>();
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

        for (ModelAccessBinding binding : compilation.bindings()) {
            RawDefinition sourceView;
            try {
                sourceView = symbols.require(binding.sourceModel());
            } catch (RuntimeException missing) {
                diagnostics.add(ModelAccessDiagnostics.sourceViewNotFound(
                        binding.sourceModel(), binding.sourceRef()));
                continue;
            }
            ModelPathCompilationResult paths = pathCompiler.compile(
                    binding.sourcePath(),
                    binding.accessMode(),
                    sourceView);
            if (!paths.compiled()) {
                diagnostics.addAll(paths.diagnostics());
                continue;
            }
            for (ModelPath path : paths.paths()) {
                ModelAccessRuleKey key = ModelAccessRuleKey.of(
                        binding.ownerSystem(),
                        TargetKey.of(binding.sourceModel()),
                        path,
                        operation(binding.accessMode()));
                RuntimeBindingPlan plan = RuntimeBindingPlan.exact(
                        TargetKey.of(binding.sourceModel()),
                        compiledTarget(binding));
                rules.add(CompiledModelAccessRule.of(
                        key,
                        AccessCompilationStatus.RUNTIME_GUARD_REQUIRED,
                        plan,
                        binding.sourceRef()));
            }
        }

        if (!diagnostics.isEmpty()) {
            Collections.sort(diagnostics);
            return ModelAccessPolicyCompilationResult.failed(diagnostics);
        }
        try {
            return ModelAccessPolicyCompilationResult.compiled(
                    ModelAccessPolicyIndex.of(rules));
        } catch (IllegalArgumentException duplicate) {
            return ModelAccessPolicyCompilationResult.failed(
                    Collections.singletonList(ModelAccessDiagnostics.create(
                            dec.core.context.model.DiagnosticCode.MIX_MODEL_ACCESS_AMBIGUOUS,
                            "modelaccess.policy.duplicate-key",
                            null,
                            compilation.bindings().isEmpty()
                                    ? null
                                    : compilation.bindings().get(0).sourceRef(),
                            "同一 System/Target/Path/Operation 只能形成一条精确授权规则")));
        }
    }

    /** READ/WRITE 必须保持正交，不从其它维度推断。 */
    private static AccessOperation operation(AccessMode mode) {
        return mode == AccessMode.READ ? AccessOperation.READ : AccessOperation.WRITE;
    }

    /** 把 P1 唯一解析结果冻结为 Context 中立 target binding。 */
    private static CompiledTargetBinding compiledTarget(ModelAccessBinding binding) {
        TargetPropertyPath target = binding.resolvedTarget();
        return target.kind() == TargetPropertyPath.Kind.TARGET_MAIN
                ? CompiledTargetBinding.targetMain(binding.targetView(), target.value())
                : CompiledTargetBinding.propertyPath(binding.targetView(), target.value());
    }
}
