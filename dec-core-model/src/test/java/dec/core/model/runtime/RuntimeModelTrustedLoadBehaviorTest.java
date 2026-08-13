package dec.core.model.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import dec.core.model.container.ModelLoader;
import dec.core.model.container.SynContainer;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** DEV-05 concrete behavior used by the registered TESTDESIGN-P2-R32 exact class. */
class RuntimeModelTrustedLoadBehaviorTest {

    void successfulLoadUsesCapturedPlanAndSameModelData() throws Exception {
        Fixture f = fixture(true);
        RuntimeModelLoadResult result = f.root.load(f.request(f.plan, f.origin));
        assertTrue(result.loaded());
        RuntimeModelHandle handle = result.handle().get();
        assertEquals(Integer.valueOf(10), handle.modelData().getValue("amount"));
        assertTrue(handle.container() instanceof SynContainer);

        Field listField = SynContainer.class.getDeclaredField("list");
        listField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ModelLoader> loaders = (List<ModelLoader>) listField.get(handle.container());
        assertEquals(1, loaders.size());
        assertSame(handle.modelData(), loaders.get(0).get());

        RuntimeModelScopeResult scope = f.root.accessScope();
        assertTrue(scope.available());
        assertSame(handle, scope.scope().get().frame().handles().get(0));
    }

    void preScopeFailuresDoNotMintTrustedScope() {
        Fixture f = fixture(true);
        ViewKey foreignView = new ViewKey("ForeignInfo");
        RuntimeBindingPlan foreign = RuntimeBindingPlan.exact(
                TargetKey.of(foreignView),
                CompiledTargetBinding.propertyPath(foreignView, "amount"));
        RuntimeModelLoadResult mismatch = f.root.load(f.request(foreign, f.origin));
        assertFalse(mismatch.loaded());
        assertEquals(RuntimeModelLoadFailureCode.PLAN_NOT_IN_CAPTURED_CONTEXT,
                mismatch.failure().get().code());
        assertFalse(f.root.accessScope().available());

        Fixture missingMaterialization = fixture(false);
        RuntimeModelLoadResult missing = missingMaterialization.root.load(
                missingMaterialization.request(missingMaterialization.plan, missingMaterialization.origin));
        assertFalse(missing.loaded());
        assertEquals(RuntimeModelLoadFailureCode.MATERIALIZATION_DESCRIPTOR_NOT_FOUND,
                missing.failure().get().code());
        assertFalse(missingMaterialization.root.accessScope().available());
    }

    void closedRootRejectsLoadAndScope() {
        Fixture f = fixture(true);
        f.root.close();
        RuntimeModelLoadResult result = f.root.load(f.request(f.plan, f.origin));
        assertFalse(result.loaded());
        assertEquals(RuntimeModelLoadFailureCode.EXECUTION_CLOSED, result.failure().get().code());
        RuntimeModelScopeResult scope = f.root.accessScope();
        assertFalse(scope.available());
        assertEquals(RuntimeModelScopeFailureCode.EXECUTION_CLOSED, scope.failure().get().code());
    }

    private static Fixture fixture(boolean includeMaterialization) {
        ViewKey view = new ViewKey("OrderInfo");
        ModelPath path = ModelPath.of("amount");
        CompiledViewMaterializationPlan materialization = CompiledViewMaterializationPlan.of(
                view, Collections.singletonList(CompiledMaterializationNode.of(path)));
        RuntimeBindingPlan plan = RuntimeBindingPlan.exact(
                TargetKey.of(view), CompiledTargetBinding.propertyPath(view, "amount"));
        CompiledModelAccessRule rule = CompiledModelAccessRule.of(
                ModelAccessRuleKey.of(new SystemKey("sales"), TargetKey.of(view), path, AccessOperation.READ),
                AccessCompilationStatus.RUNTIME_GUARD_REQUIRED,
                plan,
                new SourceRef("test:p2-dev05", 1, 1, "model-access"));
        CompiledViewMaterializationIndex materializationIndex = includeMaterialization
                ? CompiledViewMaterializationIndex.of(Collections.singletonList(materialization))
                : CompiledViewMaterializationIndex.empty();
        CompiledModelSet modelSet = new CompiledModelSet(
                PublishedSourceManifest.empty(),
                materializationIndex,
                ModelAccessPolicyIndex.of(Collections.singletonList(rule)),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                Collections.emptyList(),
                new DigestPair("source-test", "semantic-test"),
                "test-compiler", "test-schema", "test-options");
        Map<String, Object> origin = new LinkedHashMap<String, Object>();
        origin.put("amount", Integer.valueOf(10));
        EngineContext context = new EngineContext(modelSet);
        RuntimeModelExecutionRoot root = RuntimeModelExecutionRoots.production(
                context, ProductionContainerKind.SYNCHRONIZED);
        return new Fixture(plan, origin, root);
    }

    private static final class Fixture {
        private final RuntimeBindingPlan plan;
        private final Map<String, Object> origin;
        private final RuntimeModelExecutionRoot root;
        private Fixture(RuntimeBindingPlan plan, Map<String, Object> origin, RuntimeModelExecutionRoot root) {
            this.plan = plan; this.origin = origin; this.root = root;
        }
        private RuntimeModelLoadRequest request(RuntimeBindingPlan requestedPlan, Object requestedOrigin) {
            return RuntimeModelLoadRequest.of(requestedPlan, requestedOrigin, "testRule", "testConnection");
        }
    }
}
