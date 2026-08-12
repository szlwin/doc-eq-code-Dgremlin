package dec.core.compiler.modelaccess;

import dec.core.compiler.raw.RawDefinition;

/**
 * P2 source ModelPath 编译 seam。
 * Skeleton 仅冻结输入/输出与 fail-closed 边界，具体 shape/wildcard 算法在 concrete step 实现。
 */
public final class ModelPathCompiler {
    public ModelPathCompiler() {
    }

    /**
     * 将 source read/write path 编译成有限 exact ModelPath 集合。
     * Skeleton 阶段显式拒绝执行，禁止伪造已实现结果。
     */
    public ModelPathCompilationResult compile(
            SharedModelPath sourcePath,
            AccessMode mode,
            RawDefinition sourceView) {
        throw new UnsupportedOperationException(
                "DEV-03 concrete ModelPath compilation is not implemented");
    }
}
