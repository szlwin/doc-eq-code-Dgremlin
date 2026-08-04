package dec.core.compiler.information;

import dec.core.context.model.InformationKey;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 已完成 owner 与精确 Key 绑定、但尚未求值的 expression 事实。
 */
public final class ResolvedInformationExpression
        implements Comparable<ResolvedInformationExpression> {
    private final InformationKey owner;
    private final InformationExpressionAst ast;
    private final List<InformationKey> dependencies;
    private final SourceRef sourceRef;

    /** 冻结一个已解析 expression。 */
    public ResolvedInformationExpression(
            InformationKey owner,
            InformationExpressionAst ast,
            List<InformationKey> dependencies,
            SourceRef sourceRef) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.ast = Objects.requireNonNull(ast, "ast");
        List<InformationKey> copy = new ArrayList<InformationKey>(
                Objects.requireNonNull(dependencies, "dependencies"));
        Collections.sort(copy);
        this.dependencies = Collections.unmodifiableList(copy);
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
    }

    /** 返回 expression owner。 */
    public InformationKey owner() {
        return owner;
    }

    /** 返回不可变 AST。 */
    public InformationExpressionAst ast() {
        return ast;
    }

    /** 返回稳定排序依赖。 */
    public List<InformationKey> dependencies() {
        return dependencies;
    }

    /** 返回定义来源位置。 */
    public SourceRef sourceRef() {
        return sourceRef;
    }

    @Override
    public int compareTo(ResolvedInformationExpression other) {
        return owner.compareTo(Objects.requireNonNull(other, "other").owner);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResolvedInformationExpression)) {
            return false;
        }
        ResolvedInformationExpression that =
                (ResolvedInformationExpression) other;
        return owner.equals(that.owner)
                && ast.equals(that.ast)
                && dependencies.equals(that.dependencies)
                && sourceRef.equals(that.sourceRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, ast, dependencies, sourceRef);
    }
}
