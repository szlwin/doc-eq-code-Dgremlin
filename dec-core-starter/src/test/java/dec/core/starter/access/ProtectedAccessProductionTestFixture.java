package dec.core.starter.access;

import dec.core.context.EngineContext;
import dec.core.context.data.ModelData;
import dec.core.context.model.AccessCompilationStatus;
import dec.core.context.model.AccessOperation;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledMaterializationNode;
import dec.core.context.model.CompiledModelAccessRule;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.CompiledTargetBinding;
import dec.core.context.model.CompiledViewMaterializationIndex;
import dec.core.context.model.CompiledViewMaterializationPlan;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.ModelAccessPolicyIndex;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.ModelPath;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import dec.core.context.model.TargetKey;
import dec.core.context.model.ViewKey;
import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.ProtectedInvocationId;
import dec.core.context.runtime.RuntimeCollectionCursorId;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.model.container.Container;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;
import dec.core.model.container.listener.ContainerListener;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import dec.core.model.runtime.RuntimeModelAccessScope;
import dec.core.model.runtime.RuntimeModelFrame;
import dec.core.model.runtime.RuntimeModelHandle;
import dec.core.model.runtime.RuntimeModelProvenance;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Test-only trusted Scope fixture. Production source gains no MODEL constructor or ModelData injection seam. */
final class ProtectedAccessProductionTestFixture {
    final ViewKey view = new ViewKey("OrderInfo");
    final TargetKey targetKey = TargetKey.of(view);
    final ModelPath path = ModelPath.of("amount");
    final SystemKey owner = new SystemKey("sales");
    final RuntimeBindingPlan plan = RuntimeBindingPlan.exact(
            targetKey, CompiledTargetBinding.propertyPath(view, "amount"));
    final ModelAccessRuleKey readKey = ModelAccessRuleKey.of(owner, targetKey, path, AccessOperation.READ);
    final ModelAccessRuleKey writeKey = ModelAccessRuleKey.of(owner, targetKey, path, AccessOperation.WRITE);
    final Data data = new Data();
    final Effect effect = new Effect();
    final EngineContext context;
    final RuntimeModelAccessScope scope;

    ProtectedAccessProductionTestFixture() throws Exception {
        data.put("amount", Long.valueOf(10L));
        context = context(true);
        RuntimeModelProvenance provenance = construct(
                RuntimeModelProvenance.class,
                new Class<?>[] {RuntimeBindingPlan.class, ViewKey.class},
                new Object[] {plan, view});
        RuntimeModelHandle handle = construct(
                RuntimeModelHandle.class,
                new Class<?>[] {RuntimeModelProvenance.class, ModelData.class, Container.class},
                new Object[] {provenance, data, effect});
        RuntimeModelFrame frame = construct(
                RuntimeModelFrame.class,
                new Class<?>[] {
                    dec.core.context.runtime.RuntimeExecutionFrameId.class,
                    dec.core.context.runtime.RuntimeResolutionOwnerId.class,
                    RuntimeCollectionCursorId.class,
                    java.util.List.class
                },
                new Object[] {
                    dec.core.context.runtime.RuntimeExecutionFrameId.of("prod-frame"),
                    dec.core.context.runtime.RuntimeResolutionOwnerId.of("prod-owner"),
                    null,
                    Collections.singletonList(handle)
                });
        scope = construct(
                RuntimeModelAccessScope.class,
                new Class<?>[] {RuntimeModelFrame.class},
                new Object[] {frame});
    }

    EngineContext foreignContext() {
        return context(false);
    }

    ProtectedAccessComposition createComposition() {
        ProtectedAccessCompositionResult result =
                ProtectedAccessRuntimeFactory.production(context).create(scope);
        if (!result.created()) {
            throw new IllegalStateException("composition creation failed: " + result.failure().get().code());
        }
        return result.composition().get();
    }

    ProtectedAccessInvocation read(String id) {
        return ProtectedAccessInvocation.of(
                ProtectedInvocationId.of(id),
                readKey,
                scope.frame().frameId(),
                scope.frame().ownerResolutionId(),
                scope.frame().cursorId());
    }

    ProtectedAccessInvocation write(String id, RuntimeFactValue value) {
        return ProtectedAccessInvocation.write(
                ProtectedInvocationId.of(id),
                writeKey,
                scope.frame().frameId(),
                scope.frame().ownerResolutionId(),
                scope.frame().cursorId(),
                value);
    }

    private EngineContext context(boolean includePolicies) {
        CompiledViewMaterializationPlan materialization = CompiledViewMaterializationPlan.of(
                view, Collections.singletonList(CompiledMaterializationNode.of(path)));
        ModelAccessPolicyIndex policies = includePolicies
                ? ModelAccessPolicyIndex.of(Arrays.asList(rule(readKey, 1), rule(writeKey, 2)))
                : ModelAccessPolicyIndex.empty();
        return new EngineContext(new CompiledModelSet(
                PublishedSourceManifest.empty(),
                CompiledViewMaterializationIndex.of(Collections.singletonList(materialization)),
                policies,
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                Collections.emptyList(),
                new DigestPair("prod-source", "prod-semantic"),
                "prod-compiler", "prod-schema", "prod-options"));
    }

    private CompiledModelAccessRule rule(ModelAccessRuleKey key, int line) {
        return CompiledModelAccessRule.of(
                key,
                AccessCompilationStatus.RUNTIME_GUARD_REQUIRED,
                plan,
                new SourceRef("test:dev08", line, 1, "model-access"));
    }

    private static <T> T construct(
            Class<T> type,
            Class<?>[] parameterTypes,
            Object[] values) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(values);
    }

    static final class Data extends ModelData {
        private static final long serialVersionUID = 1L;
        Data() { super(); }
        void put(String key, Object value) { addKey(key); addData(key, value); }
    }

    static final class Effect implements Container {
        private final AtomicInteger executeCount = new AtomicInteger();
        volatile boolean success = true;
        volatile CountDownLatch entered;
        volatile CountDownLatch release;

        void block() {
            entered = new CountDownLatch(1);
            release = new CountDownLatch(1);
        }

        int executeCount() {
            return executeCount.get();
        }

        boolean awaitEntered() throws InterruptedException {
            CountDownLatch current = entered;
            return current != null && current.await(5, TimeUnit.SECONDS);
        }

        void release() {
            CountDownLatch current = release;
            if (current != null) current.countDown();
        }

        @Override public Container load(ModelLoader loader) { return this; }

        @Override
        public Container execute() throws ExecuteRuleException {
            executeCount.incrementAndGet();
            CountDownLatch start = entered;
            CountDownLatch finish = release;
            if (start != null && finish != null) {
                start.countDown();
                try {
                    if (!finish.await(5, TimeUnit.SECONDS)) {
                        throw new ExecuteRuleException("TEST", "timed out waiting for concurrent release");
                    }
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new ExecuteRuleException("TEST", "interrupted");
                }
            }
            return this;
        }

        @Override public Container addListener(ContainerListener listener) { return this; }
        @Override public ResultInfo getResult() {
            return success ? ResultInfo.success() : ResultInfo.fail("TEST", "rejected");
        }
    }
}
