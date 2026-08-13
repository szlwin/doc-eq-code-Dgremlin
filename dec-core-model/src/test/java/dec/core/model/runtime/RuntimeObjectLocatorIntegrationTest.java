package dec.core.model.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dec.core.context.data.ModelData;
import dec.core.context.model.CompiledTargetBinding;
import dec.core.context.model.ModelPath;
import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.model.TargetKey;
import dec.core.context.model.ViewKey;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.RuntimeBindingProof;
import dec.core.context.runtime.RuntimeExecutionFrameId;
import dec.core.context.runtime.RuntimeModelSessionId;
import dec.core.context.runtime.RuntimeMutationVersion;
import dec.core.context.runtime.RuntimeObjectId;
import dec.core.context.runtime.RuntimeResolutionOwnerId;
import dec.core.model.container.Container;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;
import dec.core.model.container.listener.ContainerListener;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 DEV-06 locator exact oracle。 */
class RuntimeObjectLocatorIntegrationTest {

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-OBJECT-LOCATOR-SCOPE-001")
    void locatorRequiresSameSealedSessionAndRegisteredHandle() throws Exception {
        Fixture fixture = fixture();
        RuntimeModelSession session = fixture.scope.beginSession();
        RuntimeObjectId objectId = session.register(fixture.handle);
        session.seal();
        ResolvedRuntimeTarget exact = fixture.target(session.sessionId(), objectId, fixture.plan);
        assertNotNull(session.locate(exact));
        assertEquals(RuntimeMutationVersion.of(0L), session.currentVersion(exact, fixture.path));

        ResolvedRuntimeTarget wrongSession = fixture.target(
                RuntimeModelSessionId.of("other-session"), objectId, fixture.plan);
        assertNull(session.locate(wrongSession));
    }

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-OBJECT-NOT-FOUND-STALE-001")
    void locatorRejectsMissingObjectAndClosedSession() throws Exception {
        Fixture fixture = fixture();
        RuntimeModelSession session = fixture.scope.beginSession();
        RuntimeObjectId objectId = session.register(fixture.handle);
        session.seal();
        assertNull(session.locate(fixture.target(
                session.sessionId(), RuntimeObjectId.of("missing-object"), fixture.plan)));
        session.close();
        assertNull(session.locate(fixture.target(session.sessionId(), objectId, fixture.plan)));
    }

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-TARGET-SELECTION-001")
    void locatorUsesExactBindingProofAndExclusiveOwnership() throws Exception {
        Fixture fixture = fixture();
        RuntimeModelSession first = fixture.scope.beginSession();
        RuntimeObjectId objectId = first.register(fixture.handle);
        RuntimeModelSession second = fixture.scope.beginSession();
        RuntimeModelSessionException conflict = assertThrows(
                RuntimeModelSessionException.class,
                () -> second.register(fixture.handle));
        assertEquals(RuntimeModelSessionFailureCode.OWNERSHIP_CONFLICT, conflict.code());
        first.seal();

        ViewKey foreignView = new ViewKey("ForeignInfo");
        RuntimeBindingPlan foreignPlan = RuntimeBindingPlan.exact(
                TargetKey.of(foreignView),
                CompiledTargetBinding.propertyPath(foreignView, "amount"));
        assertNull(first.locate(fixture.target(first.sessionId(), objectId, foreignPlan)));
    }

    private static Fixture fixture() {
        ViewKey view = new ViewKey("OrderInfo");
        ModelPath path = ModelPath.of("amount");
        RuntimeBindingPlan plan = RuntimeBindingPlan.exact(
                TargetKey.of(view),
                CompiledTargetBinding.propertyPath(view, "amount"));
        TestModelData data = new TestModelData();
        data.put("amount", Integer.valueOf(10));
        RuntimeModelHandle handle = new RuntimeModelHandle(
                new RuntimeModelProvenance(plan, view), data, new TestContainer());
        RuntimeModelFrame frame = new RuntimeModelFrame(
                RuntimeExecutionFrameId.of("locator-frame"),
                RuntimeResolutionOwnerId.of("locator-owner"),
                null,
                Arrays.asList(handle));
        return new Fixture(plan, path, handle, new RuntimeModelAccessScope(frame));
    }

    private static final class Fixture {
        private final RuntimeBindingPlan plan;
        private final ModelPath path;
        private final RuntimeModelHandle handle;
        private final RuntimeModelAccessScope scope;
        private Fixture(RuntimeBindingPlan plan, ModelPath path, RuntimeModelHandle handle, RuntimeModelAccessScope scope) {
            this.plan = plan; this.path = path; this.handle = handle; this.scope = scope;
        }
        private ResolvedRuntimeTarget target(RuntimeModelSessionId sessionId, RuntimeObjectId objectId, RuntimeBindingPlan proofPlan) {
            return ResolvedRuntimeTarget.of(
                    sessionId, objectId, plan.sourceTargetKey(), RuntimeBindingProof.exact(proofPlan));
        }
    }

    private static final class TestModelData extends ModelData {
        private static final long serialVersionUID = 1L;
        private TestModelData() { super(); }
        private void put(String key, Object value) { addKey(key); addData(key, value); }
    }

    private static final class TestContainer implements Container {
        @Override public Container load(ModelLoader loader) { return this; }
        @Override public Container execute() throws ExecuteRuleException { return this; }
        @Override public Container addListener(ContainerListener listener) { return this; }
        @Override public ResultInfo getResult() { return ResultInfo.success(); }
    }
}
