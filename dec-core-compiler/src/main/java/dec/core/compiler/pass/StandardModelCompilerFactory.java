package dec.core.compiler.pass;

import dec.core.compiler.api.ModelCompiler;
import dec.core.compiler.source.SourcePolicy;
import java.util.Objects;

/**
 * 创建标准生产 ModelCompiler 的受控公共组装入口。
 */
public final class StandardModelCompilerFactory {
    private StandardModelCompilerFactory() {
        throw new AssertionError("No instances");
    }

    /**
     * 使用显式 Source 安全策略和稳定 Compiler 版本创建完整十阶段编译器。
     */
    public static ModelCompiler create(
            SourcePolicy sourcePolicy,
            String compilerVersion) {
        return new StandardModelCompiler(
                Objects.requireNonNull(sourcePolicy, "sourcePolicy"),
                requireText(compilerVersion));
    }

    /** 规范化版本文本，避免空版本进入 Digest 域。 */
    private static String requireText(String value) {
        String checked = Objects.requireNonNull(value, "compilerVersion").trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException("compilerVersion must not be blank");
        }
        return checked;
    }
}
