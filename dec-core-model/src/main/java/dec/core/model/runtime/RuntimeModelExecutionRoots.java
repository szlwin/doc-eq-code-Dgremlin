package dec.core.model.runtime;

import dec.core.context.EngineContext;
import dec.core.context.config.exception.DataNotDefineException;
import dec.core.context.config.model.config.ConfigConstanst;
import dec.core.context.data.ModelData;
import dec.core.context.data.ModelDataFactory;
import dec.core.context.model.CompiledModelAccessRule;
import dec.core.context.model.CompiledViewMaterializationPlan;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.runtime.RuntimeExecutionFrameId;
import dec.core.context.runtime.RuntimeResolutionOwnerId;
import dec.core.model.container.Container;
import dec.core.model.container.ContainerFactory;
import dec.core.model.container.ModelLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** RuntimeModelExecutionRoot 的 production factory；不提供 Container/ModelData 注入 overload。 */
public final class RuntimeModelExecutionRoots {
    private static final AtomicLong ROOT_SEQUENCE = new AtomicLong();
    private RuntimeModelExecutionRoots() { }

    /** 创建只捕获一个 EngineContext 和受控 Container kind 的生产 root。 */
    public static RuntimeModelExecutionRoot production(EngineContext context, ProductionContainerKind kind) {
        return new ProductionRuntimeModelExecutionRoot(context, kind);
    }

    /** DEV-05 concrete implementation：严格固定 L01→L07；任何 pre-scope 失败都不 mint Handle/Scope。 */
    private static final class ProductionRuntimeModelExecutionRoot implements RuntimeModelExecutionRoot {
        private final EngineContext context;
        private final ProductionContainerKind kind;
        private final long rootId = ROOT_SEQUENCE.incrementAndGet();
        private final List<RuntimeModelHandle> handles = new ArrayList<RuntimeModelHandle>();
        private RuntimeModelAccessScope scope;
        private boolean closed;

        private ProductionRuntimeModelExecutionRoot(EngineContext context, ProductionContainerKind kind) {
            this.context = Objects.requireNonNull(context, "context");
            this.kind = Objects.requireNonNull(kind, "kind");
        }

        /** 按 R30 L01-L07 顺序完成 trusted load；只有 L06 成功后才创建 Handle。 */
        @Override
        public synchronized RuntimeModelLoadResult load(RuntimeModelLoadRequest request) {
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

        /** 只在至少一个 trusted Handle 完成 L07 后 mint Scope；新 load 会使旧 Scope 失活。 */
        @Override
        public synchronized RuntimeModelScopeResult accessScope() {
            if (closed) return RuntimeModelScopeResult.failed(RuntimeModelScopeFailureCode.EXECUTION_CLOSED);
            if (handles.isEmpty()) return RuntimeModelScopeResult.failed(RuntimeModelScopeFailureCode.NO_TRUSTED_MODEL);
            if (scope == null) {
                RuntimeModelFrame frame = new RuntimeModelFrame(
                        RuntimeExecutionFrameId.of("model-frame-" + rootId),
                        RuntimeResolutionOwnerId.of("model-owner-" + rootId),
                        null,
                        handles);
                scope = new RuntimeModelAccessScope(frame);
            }
            if (!scope.active()) return RuntimeModelScopeResult.failed(RuntimeModelScopeFailureCode.SCOPE_INACTIVE);
            return RuntimeModelScopeResult.available(scope);
        }

        /** 关闭 root 同时让已经交出的 Scope 失活；后续 load/accessScope 都 fail closed。 */
        @Override
        public synchronized void close() {
            if (closed) return;
            closed = true;
            if (scope != null) scope.deactivate();
        }

        /** L02：只接受 captured Context 已发布规则中的 exact RuntimeBindingPlan。 */
        private boolean containsExactPlan(RuntimeBindingPlan plan) {
            for (ModelAccessRuleKey key : context.modelAccessPolicyIndex().keys()) {
                Optional<CompiledModelAccessRule> rule = context.modelAccessPolicyIndex().find(key);
                if (rule.isPresent() && rule.get().runtimeBindingPlan().equals(plan)) return true;
            }
            return false;
        }

        /** L03：只按 exact target ViewKey 读取 captured materialization aggregate。 */
        private CompiledViewMaterializationPlan exactMaterialization(RuntimeBindingPlan plan) {
            return context.viewMaterializationIndex().find(plan.compiledTargetBinding().targetViewKey()).orElse(null);
        }

        /** L04：只调用 typed ModelDataFactory；任何物化异常都折叠为稳定 pre-scope failure。 */
        private ModelData materialize(CompiledViewMaterializationPlan materialization, Object originObject) {
            try {
                return ModelDataFactory.getInstance().createData(materialization, originObject);
            } catch (DataNotDefineException | RuntimeException failure) {
                return null;
            }
        }

        /** L05-L06：只使用 3-arg ModelLoader，并由 MODEL ContainerFactory 创建受控 production Container。 */
        private Container loadIntoProductionContainer(RuntimeModelLoadRequest request, ModelData modelData) {
            String containerType = kind == ProductionContainerKind.COMMIT
                    ? ConfigConstanst.CONTAINER_TYPE_COMMIT
                    : ConfigConstanst.CONTAINER_TYPE_SYN;
            Container container = ContainerFactory.getContainer(containerType);
            if (container == null) return null;
            try {
                ModelLoader loader = new ModelLoader().load(request.ruleName(), modelData, request.connectionName());
                Container accepted = container.load(loader);
                return accepted == null ? null : container;
            } catch (RuntimeException failure) {
                return null;
            }
        }

        /** L07：把 L04 创建且 L05/L06 已绑定的同一个 ModelData 引用冻结进 Handle。 */
        private RuntimeModelLoadResult freezeTrustedHandle(
                RuntimeBindingPlan plan,
                CompiledViewMaterializationPlan materialization,
                ModelData modelData,
                Container container) {
            RuntimeModelHandle handle = new RuntimeModelHandle(
                    new RuntimeModelProvenance(plan, materialization.viewKey()), modelData, container);
            handles.add(handle);
            if (scope != null) {
                scope.deactivate();
                scope = null;
            }
            return RuntimeModelLoadResult.loaded(handle);
        }
    }
}
