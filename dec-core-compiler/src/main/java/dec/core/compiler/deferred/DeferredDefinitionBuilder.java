package dec.core.compiler.deferred;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 将后续阶段语义批量构造为完整 Deferred Registry。
 *
 * <p>Architecture Skeleton 只冻结 API 与原子失败边界，具体分类行为由 Development 阶段实现。</p>
 */
public final class DeferredDefinitionBuilder {
    private final DeferredClassificationPolicy policy;

    public DeferredDefinitionBuilder() {
        this(new DeferredClassificationPolicy());
    }

    public DeferredDefinitionBuilder(DeferredClassificationPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    /**
     * Skeleton 阶段统一返回受控失败，确保 RED 来自业务合同而非编译错误。
     */
    public DeferredClassificationResult build(
            List<DeferredClassificationInput> inputs) {
        Objects.requireNonNull(inputs, "inputs");
        return DeferredClassificationResult.failed(Collections.singletonList(
                DeferredDiagnostics.incomplete(
                        inputs.isEmpty() ? null : inputs.get(0),
                        "classifier-not-implemented")));
    }
}
