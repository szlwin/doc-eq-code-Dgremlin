package dec.core.compiler.api;

import java.util.Objects;

/**
 * 不可变的 Compiler 选项与单调时钟截止时间边界。
 */
public final class CompilationOptions {
    private final String schemaVersion;
    private final String optionsVersion;
    private final long deadlineNanos;

    /**
     * 冻结 Schema、选项版本以及绝对单调时钟截止时间。
     *
     * @param schemaVersion 解释输入源时使用的 Schema 合同版本
     * @param optionsVersion 参与语义身份计算的规范化选项版本
     * @param deadlineNanos 绝对单调时钟截止时间，或 {@link Long#MAX_VALUE}
     */
    public CompilationOptions(
            String schemaVersion,
            String optionsVersion,
            long deadlineNanos) {
        if (deadlineNanos < 0L) {
            throw new IllegalArgumentException("deadlineNanos must be >= 0");
        }
        this.schemaVersion = ApiContracts.requireText(schemaVersion, "schemaVersion");
        this.optionsVersion = ApiContracts.requireText(optionsVersion, "optionsVersion");
        this.deadlineNanos = deadlineNanos;
    }

    /**
     * 返回解释输入源时使用的 Schema 合同版本。
     */
    public String schemaVersion() {
        return schemaVersion;
    }

    /**
     * 返回参与语义身份计算的规范化选项版本。
     */
    public String optionsVersion() {
        return optionsVersion;
    }

    /**
     * 返回注入单调时钟域中的绝对截止时间。
     */
    public long deadlineNanos() {
        return deadlineNanos;
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
        return deadlineNanos == that.deadlineNanos
                && schemaVersion.equals(that.schemaVersion)
                && optionsVersion.equals(that.optionsVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemaVersion, optionsVersion, deadlineNanos);
    }

    @Override
    public String toString() {
        return "CompilationOptions{"
                + "schemaVersion='" + schemaVersion + '\''
                + ", optionsVersion='" + optionsVersion + '\''
                + ", deadlineNanos=" + deadlineNanos
                + '}';
    }
}
