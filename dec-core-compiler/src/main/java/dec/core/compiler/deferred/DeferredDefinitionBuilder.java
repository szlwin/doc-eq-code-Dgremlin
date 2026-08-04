package dec.core.compiler.deferred;

import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.ImmutableDeferredRegistry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 将后续阶段语义批量构造为完整 Deferred Registry。
 *
 * <p>Architecture Skeleton 只冻结 API 与原子发布边界，具体批量分类行为由 Development 阶段实现。</p>
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
     * Skeleton 仅对第一个字段完整的输入发布一个受控样本，其余场景返回稳定失败。
     *
     * <p>这样可以验证 Registry、不变集合和 API seam，同时确保完整批量分类、
     * Diagnostic 聚合、重复检测与空批次合同继续保持 RED。</p>
     */
    public DeferredClassificationResult build(
            List<DeferredClassificationInput> inputs) {
        Objects.requireNonNull(inputs, "inputs");
        if (inputs.isEmpty() || !isSkeletonComplete(inputs.get(0))) {
            return DeferredClassificationResult.failed(Collections.singletonList(
                    DeferredDiagnostics.incomplete(
                            inputs.isEmpty() ? null : inputs.get(0),
                            "classifier-not-implemented")));
        }

        DeferredClassificationInput input = inputs.get(0);
        DeferredKey key = new DeferredKey(
                input.ownerKey().get(),
                input.kind().get(),
                input.ordinal().get());
        DeferredDefinition definition = new DeferredDefinition(
                key,
                policy.requiredStage(input.kind().get()),
                input.reasonCode().get(),
                input.sourceRef().get(),
                input.body().get(),
                input.resolvedReferences());
        Map<DeferredKey, DeferredDefinition> values =
                new TreeMap<DeferredKey, DeferredDefinition>();
        values.put(key, definition);
        return DeferredClassificationResult.classified(
                new ImmutableDeferredRegistry(values));
    }

    /** 判断 Skeleton 样本是否足以安全构造一个 DeferredDefinition。 */
    private static boolean isSkeletonComplete(DeferredClassificationInput input) {
        return input != null
                && input.ownerKey().isPresent()
                && input.kind().isPresent()
                && input.ordinal().isPresent()
                && input.ordinal().get() >= 0
                && input.reasonCode().isPresent()
                && !input.reasonCode().get().trim().isEmpty()
                && input.sourceRef().isPresent()
                && input.body().isPresent()
                && input.resolvedReferencesProvided()
                && !input.resolvedReferences().contains(null)
                && input.unresolvedReferences().isEmpty();
    }
}
