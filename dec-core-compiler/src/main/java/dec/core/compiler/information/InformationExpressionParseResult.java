package dec.core.compiler.information;

import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 单个 expression parser 的不可变结果。
 */
public final class InformationExpressionParseResult {
    private final InformationExpressionAst ast;
    private final List<Diagnostic> diagnostics;

    /** 冻结 parser 结果。 */
    private InformationExpressionParseResult(
            InformationExpressionAst ast,
            List<Diagnostic> diagnostics) {
        this.ast = ast;
        List<Diagnostic> copy = new ArrayList<Diagnostic>(
                Objects.requireNonNull(diagnostics, "diagnostics"));
        Collections.sort(copy);
        this.diagnostics = Collections.unmodifiableList(copy);
    }

    /** 创建成功结果。 */
    public static InformationExpressionParseResult parsed(
            InformationExpressionAst ast) {
        return new InformationExpressionParseResult(
                Objects.requireNonNull(ast, "ast"),
                Collections.<Diagnostic>emptyList());
    }

    /** 创建失败结果。 */
    public static InformationExpressionParseResult failed(
            List<Diagnostic> diagnostics) {
        if (Objects.requireNonNull(diagnostics, "diagnostics").isEmpty()) {
            throw new IllegalArgumentException("failed parser result needs diagnostics");
        }
        return new InformationExpressionParseResult(null, diagnostics);
    }

    /** 返回成功 AST。 */
    public Optional<InformationExpressionAst> ast() {
        return Optional.ofNullable(ast);
    }

    /** 返回稳定排序 Diagnostic。 */
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }
}
