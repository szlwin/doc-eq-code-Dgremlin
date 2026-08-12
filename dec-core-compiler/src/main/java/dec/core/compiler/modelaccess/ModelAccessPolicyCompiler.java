package dec.core.compiler.modelaccess;

import dec.core.compiler.symbol.SymbolTable;

/**
 * 将已经解析完成的 P1 ModelAccess Binding 转换为 P2 exact authorization facts。
 * Skeleton 只冻结调用拓扑，具体规则生成在 concrete step 完成。
 */
public final class ModelAccessPolicyCompiler {
    private final ModelPathCompiler modelPathCompiler;

    public ModelAccessPolicyCompiler() {
        this(new ModelPathCompiler());
    }

    /** 允许测试注入同一无状态 ModelPath 编译 seam。 */
    public ModelAccessPolicyCompiler(ModelPathCompiler modelPathCompiler) {
        this.modelPathCompiler = java.util.Objects.requireNonNull(
                modelPathCompiler,
                "modelPathCompiler");
    }

    /**
     * 原子编译完整 binding 批次；Skeleton 阶段显式拒绝执行，禁止发布空壳 policy。
     */
    public ModelAccessPolicyCompilationResult compile(
            ModelAccessCompilation compilation,
            SymbolTable symbols) {
        throw new UnsupportedOperationException(
                "DEV-03 concrete policy compilation is not implemented");
    }
}
