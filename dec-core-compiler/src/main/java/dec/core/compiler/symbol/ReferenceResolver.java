package dec.core.compiler.symbol;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;

/**
 * TASK-P1-T08 的可编译 TDD seam。
 *
 * <p>当前固定返回 not-implemented Diagnostic，使 RED 来自目标行为缺失，
 * 而不是缺类、编译或依赖错误。</p>
 */
public final class ReferenceResolver {
    private static final String PASS = "reference-resolution";
    private static final SourceRef UNKNOWN_SOURCE =
            new SourceRef("<unknown-reference-source>", 0, 0, "/");

    /**
     * 返回受控失败；Architecture Review 后再实现完整精确解析。
     */
    public ReferenceResolutionResult resolve(
            RawDefinitionSet definitions,
            SymbolTable symbolTable) {
        SourceRef sourceRef = UNKNOWN_SOURCE;
        if (definitions != null && !definitions.definitions().isEmpty()) {
            RawDefinition first = definitions.definitions().get(0);
            sourceRef = first.sourceRef();
        }
        Diagnostic diagnostic = new Diagnostic(
                DiagnosticCode.MIX_REF_UNKNOWN,
                DiagnosticSeverity.ERROR,
                "reference.not-implemented",
                null,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                "请实现 TASK-P1-T08 强类型引用解析",
                PASS);
        return ReferenceResolutionResult.failed(
                Collections.singletonList(diagnostic));
    }
}
