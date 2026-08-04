package dec.core.compiler.information;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 不执行求值的不可变 Information expression AST。
 */
public final class InformationExpressionAst {
    /** AST 节点类别。 */
    public enum Kind {
        REFERENCE,
        AND,
        OR
    }

    private final Kind kind;
    private final String reference;
    private final InformationExpressionAst left;
    private final InformationExpressionAst right;
    private final String canonical;
    private final List<String> references;

    /**
     * 冻结一个 AST 节点；具体 parser 负责保证节点组合合法。
     */
    InformationExpressionAst(
            Kind kind,
            String reference,
            InformationExpressionAst left,
            InformationExpressionAst right,
            String canonical,
            List<String> references) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.reference = reference;
        this.left = left;
        this.right = right;
        this.canonical = Objects.requireNonNull(canonical, "canonical");
        this.references = immutableReferences(references);
    }

    /** 返回节点类别。 */
    public Kind kind() {
        return kind;
    }

    /** 返回 REFERENCE 节点的原始限定引用。 */
    public String reference() {
        return reference;
    }

    /** 返回二元节点左子树。 */
    public InformationExpressionAst left() {
        return left;
    }

    /** 返回二元节点右子树。 */
    public InformationExpressionAst right() {
        return right;
    }

    /** 返回稳定 canonical AST 文本。 */
    public String canonical() {
        return canonical;
    }

    /** 返回按 AST 前序遍历冻结的 lexical 引用。 */
    public List<String> references() {
        return references;
    }

    /** 对引用列表执行防御性复制。 */
    private static List<String> immutableReferences(List<String> values) {
        Objects.requireNonNull(values, "references");
        List<String> copy = new ArrayList<String>(values.size());
        for (String value : values) {
            copy.add(Objects.requireNonNull(value, "references contains null"));
        }
        return Collections.unmodifiableList(copy);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof InformationExpressionAst)) {
            return false;
        }
        InformationExpressionAst that = (InformationExpressionAst) other;
        return kind == that.kind
                && Objects.equals(reference, that.reference)
                && Objects.equals(left, that.left)
                && Objects.equals(right, that.right)
                && canonical.equals(that.canonical)
                && references.equals(that.references);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, reference, left, right, canonical, references);
    }

    @Override
    public String toString() {
        return canonical;
    }
}
