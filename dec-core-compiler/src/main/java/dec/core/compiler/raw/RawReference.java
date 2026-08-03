package dec.core.compiler.raw;

import dec.core.context.model.SourceRef;
import java.util.Objects;

/**
 * 尚未执行 TypedKey 或目标解析的原始引用事实。
 */
public final class RawReference {
    private final String role;
    private final String target;
    private final SourceRef sourceRef;

    /**
     * 创建一个只保存 lexical role、target 和来源位置的引用。
     *
     * <p>role 与 target 只使用 trim 判断空白，保存时保留原始字符串，
     * 避免 T06 在 TypedKey 阶段前提前规范化来源事实。</p>
     */
    public RawReference(String role, String target, SourceRef sourceRef) {
        this.role = requireText(role, "role");
        this.target = requireText(target, "target");
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
    }

    /** 返回引用在定义 body 中的相对角色。 */
    public String role() {
        return role;
    }

    /** 返回尚未解析且未经规范化的目标文本。 */
    public String target() {
        return target;
    }

    /** 返回声明引用的 Canonical 来源位置。 */
    public SourceRef sourceRef() {
        return sourceRef;
    }

    /**
     * 校验文本非空白并返回原始 lexical 值。
     */
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
        if (!(other instanceof RawReference)) {
            return false;
        }
        RawReference that = (RawReference) other;
        return role.equals(that.role)
                && target.equals(that.target)
                && sourceRef.equals(that.sourceRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, target, sourceRef);
    }

    @Override
    public String toString() {
        return "RawReference{" + role + "->" + target + "@" + sourceRef + '}';
    }
}
