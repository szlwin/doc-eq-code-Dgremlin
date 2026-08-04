package dec.core.compiler.information;

import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;
import java.util.Objects;

/**
 * T09 Information expression 全批编译协调器 Architecture Skeleton。
 */
public final class InformationCompiler {
    private static final String PASS = "information-compilation";
    private static final SourceRef UNKNOWN_SOURCE =
            new SourceRef("<unknown-information-source>", 0, 0, "/");

    private final InformationExpressionParser parser;
    private final InformationReferenceResolver resolver;

    /**
     * 创建尚未接入具体 parser/resolver 的 Architecture Skeleton。
     */
    public InformationCompiler() {
        this(null, null);
    }

    /**
     * 创建显式注入 parser/resolver 的编译器边界。
     */
    public InformationCompiler(
            InformationExpressionParser parser,
            InformationReferenceResolver resolver) {
        this.parser = parser;
        this.resolver = resolver;
    }

    /**
     * 当前 Skeleton 只验证输入边界并返回稳定未实现 Diagnostic。
     */
    public InformationCompilationResult compile(
            RawDefinitionSet definitions,
            SymbolTable symbols) {
        Objects.requireNonNull(definitions, "definitions");
        Objects.requireNonNull(symbols, "symbols");
        Diagnostic diagnostic = new Diagnostic(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                DiagnosticSeverity.ERROR,
                "information.not-implemented",
                null,
                UNKNOWN_SOURCE,
                Collections.<SourceRef>emptyList(),
                "请完成 T09 Information expression 编译实现",
                PASS);
        return InformationCompilationResult.failed(
                Collections.singletonList(diagnostic));
    }
}
