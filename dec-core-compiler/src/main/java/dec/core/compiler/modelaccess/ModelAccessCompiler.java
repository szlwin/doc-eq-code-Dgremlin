package dec.core.compiler.modelaccess;

import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.symbol.SymbolTable;
import java.util.Collections;
import java.util.Objects;

/**
 * T10 ModelAccess 全批编译协调器；Architecture Skeleton 先建立稳定入口与快照门禁。
 */
public final class ModelAccessCompiler {
    private final ModelAccessSelectorResolver resolver;

    /** 创建使用生产 selector seam 的编译器。 */
    public ModelAccessCompiler() {
        this(new DefaultModelAccessSelectorResolver());
    }

    /** 创建可注入 selector 的编译器，供门禁和边界测试使用。 */
    public ModelAccessCompiler(ModelAccessSelectorResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    /**
     * 编译完整 RawDefinitionSet；当前 Skeleton 只启用输入门禁，保持业务受控 RED。
     */
    public ModelAccessCompilationResult compile(
            RawDefinitionSet definitions,
            SymbolTable symbols) {
        if (definitions == null || symbols == null) {
            RawDefinitionSet safe = definitions == null
                    ? new RawDefinitionSet(Collections.emptyList())
                    : definitions;
            return ModelAccessCompilationResult.failed(Collections.singletonList(
                    ModelAccessDiagnostics.notImplemented(safe)));
        }
        if (!symbols.isBuiltFrom(definitions)) {
            return ModelAccessCompilationResult.failed(Collections.singletonList(
                    ModelAccessDiagnostics.snapshotMismatch(definitions)));
        }
        return ModelAccessCompilationResult.failed(Collections.singletonList(
                ModelAccessDiagnostics.notImplemented(definitions)));
    }
}
