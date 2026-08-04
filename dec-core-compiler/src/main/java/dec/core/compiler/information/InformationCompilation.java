package dec.core.compiler.information;

import dec.core.context.model.DeferredRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * T09 全批成功后发布的不可变 expression 与 Deferred 快照。
 */
public final class InformationCompilation {
    private final List<ResolvedInformationExpression> expressions;
    private final DeferredRegistry deferredRegistry;

    /** 冻结全部 expression 与 Deferred Registry。 */
    public InformationCompilation(
            List<ResolvedInformationExpression> expressions,
            DeferredRegistry deferredRegistry) {
        List<ResolvedInformationExpression> copy =
                new ArrayList<ResolvedInformationExpression>(
                        Objects.requireNonNull(expressions, "expressions"));
        Collections.sort(copy);
        this.expressions = Collections.unmodifiableList(copy);
        this.deferredRegistry = Objects.requireNonNull(
                deferredRegistry,
                "deferredRegistry");
    }

    /** 返回稳定排序且不可修改的 expression。 */
    public List<ResolvedInformationExpression> expressions() {
        return expressions;
    }

    /** 返回不可变 P3 Deferred Registry。 */
    public DeferredRegistry deferredRegistry() {
        return deferredRegistry;
    }

    /** 返回已编译 expression 数量。 */
    public int size() {
        return expressions.size();
    }
}
