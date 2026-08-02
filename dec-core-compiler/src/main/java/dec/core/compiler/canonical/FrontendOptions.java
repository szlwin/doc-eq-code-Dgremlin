package dec.core.compiler.canonical;

import java.util.Objects;

/**
 * 单次 Frontend 解析使用的不可变 Schema 选项。
 */
public final class FrontendOptions {
    private final String schemaVersion;

    /**
     * 创建 Frontend 选项并拒绝空白 Schema 版本。
     *
     * @param schemaVersion 当前文档必须遵循的 Schema 版本
     */
    public FrontendOptions(String schemaVersion) {
        Objects.requireNonNull(schemaVersion, "schemaVersion");
        String normalized = schemaVersion.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("schemaVersion must not be blank");
        }
        this.schemaVersion = normalized;
    }

    /**
     * 返回当前文档必须遵循的 Schema 版本。
     */
    public String schemaVersion() {
        return schemaVersion;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof FrontendOptions
                && schemaVersion.equals(((FrontendOptions) other).schemaVersion));
    }

    @Override
    public int hashCode() {
        return schemaVersion.hashCode();
    }

    @Override
    public String toString() {
        return "FrontendOptions{schemaVersion='" + schemaVersion + "'}";
    }
}
