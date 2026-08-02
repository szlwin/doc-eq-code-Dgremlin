package dec.core.compiler.api;

import java.util.Objects;

/**
 * 参与编译语义身份的不可变选项。
 */
public final class CompilationOptions {
    private final String schemaVersion;
    private final String optionsDigest;

    /**
     * 冻结 Schema 版本和规范化选项摘要。
     *
     * <p>Deadline 属于执行预算，不参与语义身份，必须由 CompilationRequest 单独持有。</p>
     *
     * @param schemaVersion 解释输入源时使用的 Schema 合同版本
     * @param optionsDigest 参与发布事实的规范化选项摘要
     */
    public CompilationOptions(String schemaVersion, String optionsDigest) {
        this.schemaVersion = ApiContracts.requireText(schemaVersion, "schemaVersion");
        this.optionsDigest = ApiContracts.requireText(optionsDigest, "optionsDigest");
    }

    /**
     * 返回解释输入源时使用的 Schema 合同版本。
     */
    public String schemaVersion() {
        return schemaVersion;
    }

    /**
     * 返回参与语义身份和 Published 事实的规范化选项摘要。
     */
    public String optionsDigest() {
        return optionsDigest;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CompilationOptions)) {
            return false;
        }
        CompilationOptions that = (CompilationOptions) other;
        return schemaVersion.equals(that.schemaVersion)
                && optionsDigest.equals(that.optionsDigest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemaVersion, optionsDigest);
    }

    @Override
    public String toString() {
        return "CompilationOptions{"
                + "schemaVersion='" + schemaVersion + '\''
                + ", optionsDigest='" + optionsDigest + '\''
                + '}';
    }
}
