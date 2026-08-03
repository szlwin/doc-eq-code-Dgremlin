package dec.core.compiler.symbol;

import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;

/**
 * 将 T06 RawDefinitionSet 转换为强类型 SymbolTable 的无状态 Builder。
 *
 * <p>TDD seam 仅提供可编译入口和稳定失败边界，真实两遍注册在后续
 * Architecture Skeleton Review 通过后实现。</p>
 */
public final class SymbolTableBuilder {
    private static final String PASS = "symbol-registration";
    private static final SourceRef UNKNOWN_SOURCE =
            new SourceRef("<unknown-raw-source>", 0, 0, "/");

    /**
     * 当前 TDD seam 明确返回未实现失败，使 RED 来自行为断言而非缺类。
     *
     * @param definitions T06 完整 RawDefinitionSet
     * @return 当前固定为不携带部分表的 FAILED 结果
     */
    public SymbolBuildResult build(RawDefinitionSet definitions) {
        SourceRef sourceRef = definitions != null
                && !definitions.definitions().isEmpty()
                ? definitions.definitions().get(0).sourceRef()
                : UNKNOWN_SOURCE;
        Diagnostic diagnostic = new Diagnostic(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                DiagnosticSeverity.ERROR,
                "symbol.registration.not-implemented",
                null,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                "请完成 TASK-P1-T07 两遍 Symbol 注册",
                PASS);
        return SymbolBuildResult.failed(
                Collections.singletonList(diagnostic));
    }
}
