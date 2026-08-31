package dec.core.compiler.modelaccess;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.ModelAccessPolicyIndex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** P2 policy 编译结果；失败时没有部分 PolicyIndex。 */
public final class ModelAccessPolicyCompilationResult {
    private final ModelAccessPolicyIndex policyIndex;
    private final List<Diagnostic> diagnostics;

    private ModelAccessPolicyCompilationResult(
            ModelAccessPolicyIndex policyIndex,
            List<Diagnostic> diagnostics) {
        this.policyIndex = policyIndex;
        this.diagnostics = diagnostics;
    }

    /** 创建完整成功结果。 */
    public static ModelAccessPolicyCompilationResult compiled(
            ModelAccessPolicyIndex policyIndex) {
        return new ModelAccessPolicyCompilationResult(
                Objects.requireNonNull(policyIndex, "policyIndex"),
                Collections.<Diagnostic>emptyList());
    }

    /** 创建不携带部分 index 的失败结果。 */
    public static ModelAccessPolicyCompilationResult failed(
            List<Diagnostic> diagnostics) {
        List<Diagnostic> copy = new ArrayList<Diagnostic>(
                Objects.requireNonNull(diagnostics, "diagnostics"));
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("failed result needs diagnostics");
        }
        Collections.sort(copy);
        return new ModelAccessPolicyCompilationResult(
                null,
                Collections.unmodifiableList(copy));
    }

    public boolean compiled() {
        return policyIndex != null;
    }

    public Optional<ModelAccessPolicyIndex> policyIndex() {
        return Optional.ofNullable(policyIndex);
    }

    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }
}
