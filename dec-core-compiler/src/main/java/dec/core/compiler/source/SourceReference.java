package dec.core.compiler.source;

import java.util.Objects;

/**
 * 调用方提供的不可变 Source 引用。
 */
public final class SourceReference {
    private final String value;

    /**
     * 创建 Source 引用，并拒绝空值和空白文本。
     *
     * @param value Provider 可解释的 Source 引用文本
     */
    public SourceReference(String value) {
        Objects.requireNonNull(value, "value");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("value must not be blank");
        }
        this.value = normalized;
    }

    /**
     * 返回 Provider 可解释的规范化引用文本。
     */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof SourceReference
                && value.equals(((SourceReference) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "SourceReference{value='" + value + "'}";
    }
}
