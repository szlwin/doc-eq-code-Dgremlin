package dec.core.compiler.pass;

import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.PublicationRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 按 DESIGN-R38 固定顺序协调十个 CompilerPass。
 */
public final class CompilerPipeline {
    public static final String SOURCE_GRAPH_VALIDATION_PASS =
            "SourceGraphValidationPass";
    public static final String STRUCTURAL_VALIDATION_PASS =
            "StructuralValidationPass";
    public static final String SYMBOL_REGISTRATION_PASS =
            "SymbolRegistrationPass";
    public static final String REFERENCE_RESOLUTION_PASS =
            "ReferenceResolutionPass";
    public static final String INFORMATION_OWNERSHIP_PASS =
            "InformationOwnershipPass";
    public static final String MODEL_ACCESS_BINDING_PASS =
            "ModelAccessBindingPass";
    public static final String DEFERRED_CLASSIFICATION_PASS =
            "DeferredClassificationPass";
    public static final String P1_SEMANTIC_VALIDATION_PASS =
            "P1SemanticValidationPass";
    public static final String DIGEST_PASS = "DigestPass";
    public static final String PUBLICATION_PASS = "PublicationPass";

    private static final List<String> FIXED_PASS_ORDER =
            Collections.unmodifiableList(Arrays.asList(
                    SOURCE_GRAPH_VALIDATION_PASS,
                    STRUCTURAL_VALIDATION_PASS,
                    SYMBOL_REGISTRATION_PASS,
                    REFERENCE_RESOLUTION_PASS,
                    INFORMATION_OWNERSHIP_PASS,
                    MODEL_ACCESS_BINDING_PASS,
                    DEFERRED_CLASSIFICATION_PASS,
                    P1_SEMANTIC_VALIDATION_PASS,
                    DIGEST_PASS,
                    PUBLICATION_PASS));

    private final List<CompilerPass> passes;

    /**
     * 防御性复制并验证十个 Pass 的数量、名称和顺序。
     */
    public CompilerPipeline(List<CompilerPass> passes) {
        List<CompilerPass> copy = new ArrayList<CompilerPass>(
                Objects.requireNonNull(passes, "passes"));
        validateFixedOrder(copy);
        this.passes = Collections.unmodifiableList(copy);
    }

    /** 返回 DESIGN-R38 冻结的 Pass 名称顺序。 */
    public static List<String> fixedPassOrder() {
        return FIXED_PASS_ORDER;
    }

    /** 返回防御性复制后的只读 Pass 列表。 */
    public List<CompilerPass> passes() {
        return passes;
    }

    /**
     * 创建新 Session 并执行 Pipeline。
     *
     * <p>当前架构 checkpoint 只建立稳定失败边界；有效 RED 后补齐真实顺序执行。</p>
     */
    public PipelineExecutionResult execute(
            CompilationRequest request,
            PublicationRequest publicationRequest) {
        CompilationSession session = new CompilationSession(
                Objects.requireNonNull(request, "request"),
                Objects.requireNonNull(publicationRequest, "publicationRequest"));
        session.addDiagnostics(Collections.singletonList(
                PipelineDiagnostics.notImplemented()));
        session.transitionTo(CompilationSessionState.FAILED);
        return new PipelineExecutionResult(session);
    }

    /** 在执行任何 Pass 前验证完整固定顺序。 */
    private static void validateFixedOrder(List<CompilerPass> values) {
        if (values.size() != FIXED_PASS_ORDER.size()) {
            throw new IllegalArgumentException("pipeline requires exactly ten passes");
        }
        for (int index = 0; index < values.size(); index++) {
            CompilerPass pass = Objects.requireNonNull(
                    values.get(index),
                    "passes contains null");
            String actual = Objects.requireNonNull(pass.name(), "pass name").trim();
            if (!FIXED_PASS_ORDER.get(index).equals(actual)) {
                throw new IllegalArgumentException(
                        "unexpected pass at index " + index + ": " + actual);
            }
        }
    }
}
