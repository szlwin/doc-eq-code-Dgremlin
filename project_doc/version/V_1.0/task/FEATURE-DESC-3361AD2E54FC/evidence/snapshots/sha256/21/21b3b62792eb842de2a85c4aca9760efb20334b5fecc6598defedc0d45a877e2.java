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
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.RuntimeBindingProof;
import dec.core.context.runtime.RuntimeCollectionCursorId;
import dec.core.context.runtime.RuntimeExecutionFrameId;
import dec.core.context.runtime.RuntimeObjectId;
import dec.core.context.runtime.RuntimeResolutionOwnerId;
import dec.core.model.container.Container;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;
import dec.core.model.container.listener.ContainerListener;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import dec.core.model.runtime.RuntimeModelAccessScope;
import dec.core.model.runtime.RuntimeModelEffectBindingResult;
import dec.core.model.runtime.RuntimeModelFrame;
import dec.core.model.runtime.RuntimeModelHandle;
import dec.core.model.runtime.RuntimeModelProvenance;
import dec.core.model.runtime.RuntimeModelSession;
import dec.core.model.runtime.RuntimeModelStarterEffectBridge;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/** Controlled R37 GREEN fixture: same real MODEL objects, but production raw-port exposure is disabled. */
final class P2SecurityAuthorityGreenFixture implements AutoCloseable {
    final ViewKey view = new ViewKey("OrderInfo");
    final TargetKey targetKey = TargetKey.of(view);
    final ModelPath path = ModelPath.of("amount");
    final SystemKey owner = new SystemKey("sales");
    final RuntimeBindingPlan plan = RuntimeBindingPlan.exact(
            targetKey, CompiledTargetBinding.propertyPath(view, "amount"));
    final ModelAccessRuleKey readKey = ModelAccessRuleKey.of(owner, targetKey, path, AccessOperation.READ);
    final ModelAccessRuleKey writeKey = ModelAccessRuleKey.of(owner, targetKey, path, AccessOperation.WRITE);
    final RuntimeExecutionFrameId frameId = RuntimeExecutionFrameId.of("green-frame");
    final RuntimeResolutionOwnerId ownerId = RuntimeResolutionOwnerId.of("green-owner");
    final Data data = new Data();
    final Effect effect = new Effect();
    final EngineContext context;
    final RuntimeModelAccessScope scope;
    final RuntimeModelSession session;
    final ResolvedRuntimeTarget target;
    final RuntimeModelEffectBindingResult binding;
    final GuardAuthorizedModelEffectPort guardedEffectPort;

    P2SecurityAuthorityGreenFixture() throws Exception { this(true); }

    P2SecurityAuthorityGreenFixture(boolean includeWriteRule) throws Exception {
        data.put("amount", Long.valueOf(10L));
        CompiledViewMaterializationPlan materialization = CompiledViewMaterializationPlan.of(
                view, Collections.singletonList(CompiledMaterializationNode.of(path)));
        CompiledModelAccessRule readRule = rule(readKey, 1);
        CompiledModelAccessRule writeRule = rule(writeKey, 2);
        context = new EngineContext(new CompiledModelSet(
                PublishedSourceManifest.empty(),
                CompiledViewMaterializationIndex.of(Collections.singletonList(materialization)),
                ModelAccessPolicyIndex.of(includeWriteRule
                        ? Arrays.asList(readRule, writeRule)
                        : Collections.singletonList(readRule)),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                Collections.emptyList(),
                new DigestPair("green-source", "green-semantic"),
                "green-compiler", "green-schema", "green-options"));
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
                new Class<?>[] {RuntimeExecutionFrameId.class, RuntimeResolutionOwnerId.class,
                    RuntimeCollectionCursorId.class, java.util.List.class},
                new Object[] {frameId, ownerId, null, Collections.singletonList(handle)});
        scope = construct(RuntimeModelAccessScope.class,
                new Class<?>[] {RuntimeModelFrame.class, boolean.class},
                new Object[] {frame, Boolean.FALSE});
        session = scope.beginSession();
        RuntimeObjectId objectId = session.register(handle);
        session.seal();
        target = ResolvedRuntimeTarget.of(
                session.sessionId(), objectId, targetKey, plan.compiledTargetBinding(),
                frameId, ownerId, Optional.<RuntimeCollectionCursorId>empty(), RuntimeBindingProof.exact(plan));
        binding = scope.effectProvider().bind(session);
        if (!binding.bound()) throw new IllegalStateException("production-mode MODEL effect binding failed: " + binding.failure());
        guardedEffectPort = RuntimeModelStarterEffectBridge.guardedEffectPort(binding);
    }

    @Override public void close() { session.close(); }

    private CompiledModelAccessRule rule(ModelAccessRuleKey key, int line) {
        return CompiledModelAccessRule.of(key, AccessCompilationStatus.RUNTIME_GUARD_REQUIRED, plan,
                new SourceRef("test:p2-green", line, 1, "model-access"));
    }

    private static <T> T construct(Class<T> type, Class<?>[] parameterTypes, Object[] values) throws Exception {
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
        boolean success = true;
        int executeCount;
        @Override public Container load(ModelLoader loader) { return this; }
        @Override public Container execute() throws ExecuteRuleException { executeCount++; return this; }
        @Override public Container addListener(ContainerListener listener) { return this; }
        @Override public ResultInfo getResult() { return success ? ResultInfo.success() : ResultInfo.fail("TEST", "rejected"); }
    }
}
