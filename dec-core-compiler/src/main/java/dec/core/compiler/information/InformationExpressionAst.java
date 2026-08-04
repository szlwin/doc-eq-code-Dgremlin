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

    /** 冻结一个经过 parser 校验的 AST 节点。 */
    private InformationExpressionAst(
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

    /** 创建一个尚未解析为 TypedKey 的限定引用节点。 */
    static InformationExpressionAst reference(String target) {
        String required = requireText(target, "target");
        return new InformationExpressionAst(
                Kind.REFERENCE,
                required,
                null,
                null,
                "ref(" + required + ")",
                Collections.singletonList(required));
    }

    /** 创建保持左结合顺序的 and 节点。 */
    static InformationExpressionAst and(
            InformationExpressionAst left,
            InformationExpressionAst right) {
        return binary(Kind.AND, "and", left, right);
    }

    /** 创建保持左结合顺序的 or 节点。 */
    static InformationExpressionAst or(
            InformationExpressionAst left,
            InformationExpressionAst right) {
        return binary(Kind.OR, "or", left, right);
    }

    /** 创建二元节点并冻结前序引用顺序。 */
    private static InformationExpressionAst binary(
            Kind kind,
            String operator,
            InformationExpressionAst left,
            InformationExpressionAst right) {
        InformationExpressionAst requiredLeft =
                Objects.requireNonNull(left, "left");
        InformationExpressionAst requiredRight =
                Objects.requireNonNull(right, "right");
        List<String> references = new ArrayList<String>(
                requiredLeft.references.size() + requiredRight.references.size());
        references.addAll(requiredLeft.references);
        references.addAll(requiredRight.references);
        return new InformationExpressionAst(
                kind,
                null,
                requiredLeft,
                requiredRight,
                operator + "(" + requiredLeft.canonical
                        + "," + requiredRight.canonical + ")",
                references);
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
            copy.add(requireText(value, "reference"));
        }
        return Collections.unmodifiableList(copy);
    }

    /** 校验文本非空白并保留 parser 已切分的 lexical 值。 */
    private static String requireText(String value, String name) {
        String required = Objects.requireNonNull(value, name);
        if (required.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return required;
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
