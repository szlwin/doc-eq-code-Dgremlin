package dec.core.model.runtime;

import dec.core.context.EngineContext;
import dec.core.context.data.ModelData;
import dec.core.context.model.CompiledViewMaterializationPlan;
import dec.core.context.model.RuntimeBindingPlan;
import dec.core.model.container.Container;
import java.util.Objects;

/** RuntimeModelExecutionRoot 的 production factory；不提供 Container/ModelData 注入 overload。 */
public final class RuntimeModelExecutionRoots {
    private RuntimeModelExecutionRoots() { }

    /** 创建只捕获一个 EngineContext 和受控 Container kind 的生产 root。 */
    public static RuntimeModelExecutionRoot production(EngineContext context, ProductionContainerKind kind) {
        return new ProductionRuntimeModelExecutionRoot(context, kind);
    }

    /** DEV-05 architecture skeleton：固定 L01→L07 顺序与 pre-scope fail-closed 边界。 */
    private static final class ProductionRuntimeModelExecutionRoot implements RuntimeModelExecutionRoot {
        private final EngineContext context;
        private final ProductionContainerKind kind;
        private boolean closed;
        private ProductionRuntimeModelExecutionRoot(EngineContext context, ProductionContainerKind kind) {
            this.context = Objects.requireNonNull(context, "context");
            this.kind = Objects.requireNonNull(kind, "kind");
        }
        @Override
        public RuntimeModelLoadResult load(RuntimeModelLoadRequest request) {
            Objects.requireNonNull(request, "request");
            if (closed) return RuntimeModelLoadResult.failed(RuntimeModelLoadFailureCode.EXECUTION_CLOSED);
            RuntimeBindingPlan plan = request.runtimeBindingPlan();
            if (!containsExactPlan(plan)) return RuntimeModelLoadResult.failed(RuntimeModelLoadFailureCode.PLAN_NOT_IN_CAPTURED_CONTEXT);
            CompiledViewMaterializationPlan materialization = exactMaterialization(plan);
            if (materialization == null) return RuntimeModelLoadResult.failed(RuntimeModelLoadFailureCode.MATERIALIZATION_DESCRIPTOR_NOT_FOUND);
            ModelData modelData = materialize(materialization, request.originObject());
            if (modelData == null) return RuntimeModelLoadResult.failed(RuntimeModelLoadFailureCode.ORIGIN_NOT_MATERIALIZABLE);
            Container container = loadIntoProductionContainer(request, modelData);
            if (container == null) return RuntimeModelLoadResult.failed(RuntimeModelLoadFailureCode.CONTAINER_LOAD_REJECTED);
            return freezeTrustedHandle(plan, materialization, modelData, container);
        }
        @Override
        public RuntimeModelScopeResult accessScope() { throw new UnsupportedOperationException("ARCHITECTURE_SKELETON: scope minting is implemented after review"); }
        @Override
        public void close() { closed = true; }
        /** L02：只接受 captured Context 已发布规则中的 exact RuntimeBindingPlan。 */
        private boolean containsExactPlan(RuntimeBindingPlan plan) { throw new UnsupportedOperationException("ARCHITECTURE_SKELETON: exact plan membership not implemented"); }
        /** L03：只按 exact target ViewKey 读取 captured materialization aggregate。 */
        private CompiledViewMaterializationPlan exactMaterialization(RuntimeBindingPlan plan) { throw new UnsupportedOperationException("ARCHITECTURE_SKELETON: materialization lookup not implemented"); }
        /** L04：只调用 typed ModelDataFactory；失败不得产生 Handle/Scope。 */
        private ModelData materialize(CompiledViewMaterializationPlan materialization, Object originObject) { throw new UnsupportedOperationException("ARCHITECTURE_SKELETON: typed materialization not implemented"); }
        /** L05-L06：只允许 3-arg ModelLoader + MODEL ContainerFactory；caller 无注入点。 */
        private Container loadIntoProductionContainer(RuntimeModelLoadRequest request, ModelData modelData) { throw new UnsupportedOperationException("ARCHITECTURE_SKELETON: production container loading not implemented"); }
        /** L07：只在 L01-L06 全成功后把同一 ModelData 引用冻结进 MODEL minted Handle。 */
        private RuntimeModelLoadResult freezeTrustedHandle(RuntimeBindingPlan plan, CompiledViewMaterializationPlan materialization, ModelData modelData, Container container) { throw new UnsupportedOperationException("ARCHITECTURE_SKELETON: trusted handle minting not implemented"); }
    }
}
