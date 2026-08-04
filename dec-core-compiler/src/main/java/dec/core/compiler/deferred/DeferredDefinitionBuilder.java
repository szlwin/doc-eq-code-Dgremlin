package dec.core.compiler.deferred;

import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.ImmutableDeferredRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 将后续阶段语义批量构造为完整 Deferred Registry。
 */
public final class DeferredDefinitionBuilder {
    private final DeferredClassificationPolicy policy;

    /** 使用冻结的默认分类策略。 */
    public DeferredDefinitionBuilder() {
        this(new DeferredClassificationPolicy());
    }

    /** 注入无状态分类策略，便于架构测试隔离。 */
    public DeferredDefinitionBuilder(DeferredClassificationPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    /**
     * 验证整个输入批次，并在全部输入完整时一次性发布不可变 Registry。
     *
     * <p>验证期间只在局部 Map 中暂存候选 Definition；任一 ERROR 都会丢弃候选，
     * 因而不会向调用方泄露部分 Registry。</p>
     */
    public DeferredClassificationResult build(
            List<DeferredClassificationInput> inputs) {
        if (inputs == null) {
            return DeferredClassificationResult.failed(Collections.singletonList(
                    DeferredDiagnostics.incomplete(null, "inputs")));
        }

        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        Map<DeferredKey, DeferredDefinition> definitions =
                new TreeMap<DeferredKey, DeferredDefinition>();

        for (DeferredClassificationInput input : inputs) {
            if (input == null) {
                diagnostics.add(DeferredDiagnostics.incomplete(
                        null, "input-null"));
                continue;
            }
            int before = diagnostics.size();
            validateRequiredFields(input, diagnostics);
            if (diagnostics.size() != before) {
                continue;
            }

            DeferredKey key = new DeferredKey(
                    input.ownerKey().get(),
                    input.kind().get(),
                    input.ordinal().get());
            if (definitions.containsKey(key)) {
                diagnostics.add(DeferredDiagnostics.incomplete(
                        input, "duplicate-key"));
                continue;
            }

            DeferredDefinition definition = new DeferredDefinition(
                    key,
                    policy.requiredStage(input.kind().get()),
                    input.reasonCode().get(),
                    input.sourceRef().get(),
                    input.body().get(),
                    input.resolvedReferences());
            definitions.put(key, definition);
        }

        if (!diagnostics.isEmpty()) {
            return DeferredClassificationResult.failed(diagnostics);
        }
        return DeferredClassificationResult.classified(
                new ImmutableDeferredRegistry(definitions));
    }

    /**
     * 按稳定顺序聚合一个输入的全部完整性问题。
     */
    private void validateRequiredFields(
            DeferredClassificationInput input,
            List<Diagnostic> diagnostics) {
        if (!input.ownerKey().isPresent()) {
            diagnostics.add(DeferredDiagnostics.incomplete(input, "owner"));
        }
        if (!input.kind().isPresent()) {
            diagnostics.add(DeferredDiagnostics.incomplete(input, "kind"));
        }
        if (!input.ordinal().isPresent() || input.ordinal().get() < 0) {
            diagnostics.add(DeferredDiagnostics.incomplete(input, "ordinal"));
        }

        boolean reasonPresent = input.reasonCode().isPresent()
                && !input.reasonCode().get().trim().isEmpty();
        if (!reasonPresent) {
            diagnostics.add(DeferredDiagnostics.incomplete(input, "reason"));
        } else if (input.kind().isPresent()
                && !policy.reasonCode(input.kind().get()).equals(
                        input.reasonCode().get())) {
            diagnostics.add(DeferredDiagnostics.incomplete(
                    input, "reason-policy"));
        }

        if (!input.sourceRef().isPresent()) {
            diagnostics.add(DeferredDiagnostics.incomplete(
                    input, "source-ref"));
        }
        if (!input.body().isPresent()) {
            diagnostics.add(DeferredDiagnostics.incomplete(input, "body"));
        }
        if (!input.resolvedReferencesProvided()) {
            diagnostics.add(DeferredDiagnostics.incomplete(
                    input, "resolved-references"));
        } else if (input.resolvedReferences().contains(null)) {
            diagnostics.add(DeferredDiagnostics.incomplete(
                    input, "resolved-reference-null"));
        }
        if (!input.unresolvedReferences().isEmpty()) {
            diagnostics.add(DeferredDiagnostics.incomplete(
                    input, "unresolved-reference"));
        }
    }
}
