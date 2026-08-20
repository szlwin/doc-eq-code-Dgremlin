package dec.core.model.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.data.ModelData;
import dec.core.context.model.CompiledTargetBinding;
import dec.core.context.model.ModelPath;
import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.model.TargetKey;
import dec.core.context.model.ViewKey;
import dec.core.context.runtime.ProtectedWriteReceipt;
import dec.core.context.runtime.ResolvedProtectedReadAccess;
import dec.core.context.runtime.ResolvedProtectedWriteAccess;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.RuntimeBindingProof;
import dec.core.context.runtime.RuntimeExecutionFrameId;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.context.runtime.RuntimeModelSessionId;
import dec.core.context.runtime.RuntimeMutationStamp;
import dec.core.context.runtime.RuntimeMutationVersion;
import dec.core.context.runtime.RuntimeObjectId;
import dec.core.context.runtime.RuntimeResolutionOwnerId;
import dec.core.model.container.Container;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;
import dec.core.model.container.listener.ContainerListener;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** DEV-06 concrete behavior：验证 sealed Session、exact locator 与 same-handle effect。 */
class RuntimeModelSessionEffectBehaviorTest {

    /** duplicate register 与跨 Session ownership 必须稳定 fail closed。 */
    @Test
    void registrationUsesExactHandleIdentityAndExclusiveLease() throws Exception {
        Fixture fixture = fixture(true);
        RuntimeModelSession first = fixture.scope.beginSession();
        RuntimeObjectId objectId = first.register(fixture.handle);
        assertNotNull(objectId);

        RuntimeModelSessionException duplicate = assertThrows(
                RuntimeModelSessionException.class,
                () -> first.register(fixture.handle));
        assertEquals(RuntimeModelSessionFailureCode.DUPLICATE_REGISTRATION, duplicate.code());

        RuntimeModelSession second = fixture.scope.beginSession();
        RuntimeModelSessionException conflict = assertThrows(
                RuntimeModelSessionException.class,
                () -> second.register(fixture.handle));
        assertEquals(RuntimeModelSessionFailureCode.OWNERSHIP_CONFLICT, conflict.code());

        first.close();
        assertNotNull(second.register(fixture.handle));
    }

    /** seal 后 locator 必须 exact 匹配 session/object/TargetKey/binding proof，不得 fallback。 */
    @Test
    void sealedLocatorAcceptsOnlyExactRegisteredTarget() throws Exception {
        Fixture fixture = fixture(true);
        RuntimeModelSession session = fixture.scope.beginSession();
        RuntimeObjectId objectId = session.register(fixture.handle);
        session.seal();

        ResolvedRuntimeTarget exact = fixture.target(session.sessionId(), objectId);
        assertNotNull(session.locate(exact));
        assertEquals(RuntimeMutationVersion.of(0L), session.currentVersion(exact, fixture.path));

        ResolvedRuntimeTarget wrongSession = fixture.target(
                RuntimeModelSessionId.of("foreign-session"), objectId);
        assertNull(session.locate(wrongSession));

        ResolvedRuntimeTarget wrongObject = fixture.target(
                session.sessionId(), RuntimeObjectId.of("foreign-object"));
        assertNull(session.locate(wrongObject));

        ViewKey otherView = new ViewKey("OtherInfo");
        RuntimeBindingPlan otherPlan = RuntimeBindingPlan.exact(
                TargetKey.of(otherView),
                CompiledTargetBinding.propertyPath(otherView, "amount"));
        ResolvedRuntimeTarget wrongProof = ResolvedRuntimeTarget.of(
                session.sessionId(),
                objectId,
                fixture.plan.sourceTargetKey(),
                RuntimeBindingProof.exact(otherPlan));
        assertNull(session.locate(wrongProof));
    }

    /** EffectProvider 只能绑定同 Scope 的 sealed Session；未 seal/跨 Scope/closed 都稳定拒绝。 */
    @Test
    void effectProviderBindsOnlySameScopeSealedSession() throws Exception {
        Fixture fixture = fixture(true);
        RuntimeModelSession session = fixture.scope.beginSession();
        session.register(fixture.handle);

        RuntimeModelEffectBindingResult unsealed = fixture.scope.effectProvider().bind(session);
        assertFalse(unsealed.bound());
        assertEquals(
                RuntimeModelEffectBindingFailureCode.SESSION_NOT_SEALED,
                unsealed.failure().get().code());

        session.seal();
        RuntimeModelEffectBindingResult bound = fixture.scope.effectProvider().bind(session);
        assertTrue(bound.bound());
        assertTrue(bound.operationPort().isPresent());

        Fixture foreign = fixture(true);
        RuntimeModelSession foreignSession = foreign.scope.beginSession();
        foreignSession.register(foreign.handle);
        foreignSession.seal();
        RuntimeModelEffectBindingResult mismatch = fixture.scope.effectProvider().bind(foreignSession);
        assertEquals(
                RuntimeModelEffectBindingFailureCode.SESSION_SCOPE_MISMATCH,
                mismatch.failure().get().code());

        session.close();
        RuntimeModelEffectBindingResult closed = fixture.scope.effectProvider().bind(session);
        assertEquals(
                RuntimeModelEffectBindingFailureCode.SESSION_CLOSED,
                closed.failure().get().code());
    }

    /** READ 返回深不可变事实快照；WRITE 只接受同 target/path/version 的当前 stamp。 */
    @Test
    void operationPortRevalidatesTargetAndMutationVersion() throws Exception {
        Fixture fixture = fixture(true);
        RuntimeModelSession session = fixture.scope.beginSession();
        RuntimeObjectId objectId = session.register(fixture.handle);
        session.seal();
        ResolvedRuntimeTarget target = fixture.target(session.sessionId(), objectId);
        RuntimeModelOperationPort port = fixture.scope.effectProvider()
                .bind(session)
                .operationPort()
                .get();

        RuntimeFactValue read = port.read(ResolvedProtectedReadAccess.of(target, fixture.path));
        assertEquals("10", read.canonicalForm());

        RuntimeMutationStamp stamp = RuntimeMutationStamp.of(
                session.sessionId(),
                objectId,
                fixture.path,
                session.currentVersion(target, fixture.path));
        ResolvedProtectedWriteAccess write = ResolvedProtectedWriteAccess.of(
                target,
                fixture.path,
                RuntimeFactValue.integerValue(20L),
                stamp);
        ProtectedWriteReceipt receipt = port.write(write);

        assertNotNull(receipt);
        assertEquals(RuntimeMutationVersion.of(1L), receipt.version());
        assertEquals(Long.valueOf(20L), fixture.modelData.getValue("amount"));
        assertEquals(RuntimeMutationVersion.of(1L), session.currentVersion(target, fixture.path));

        // 同一旧 stamp 第二次使用必须成为 stale loser，既无 receipt 也不能重复 effect。
        assertNull(port.write(write));
        assertEquals(Long.valueOf(20L), fixture.modelData.getValue("amount"));
        assertEquals(1, fixture.container.executeCount);
    }

    /** Container effect 失败时必须恢复本次写入前的 ModelData 值且不产生 receipt/version。 */
    @Test
    void failedWriteRestoresPathValueWithoutReceipt() throws Exception {
        Fixture fixture = fixture(false);
        RuntimeModelSession session = fixture.scope.beginSession();
        RuntimeObjectId objectId = session.register(fixture.handle);
        session.seal();
        ResolvedRuntimeTarget target = fixture.target(session.sessionId(), objectId);
        RuntimeModelOperationPort port = fixture.scope.effectProvider()
                .bind(session)
                .operationPort()
                .get();
        RuntimeMutationStamp stamp = RuntimeMutationStamp.of(
                session.sessionId(),
                objectId,
                fixture.path,
                RuntimeMutationVersion.of(0L));

        ProtectedWriteReceipt receipt = port.write(ResolvedProtectedWriteAccess.of(
                target,
                fixture.path,
                RuntimeFactValue.integerValue(99L),
                stamp));

        assertNull(receipt);
        assertEquals(Integer.valueOf(10), fixture.modelData.getValue("amount"));
        assertEquals(RuntimeMutationVersion.of(0L), session.currentVersion(target, fixture.path));
        assertEquals(1, fixture.container.executeCount);
    }

    /** 构造同一个 MODEL frame 内的真实 Handle/ModelData/Container 测试闭包。 */
    private static Fixture fixture(boolean executeSuccess) {
        ViewKey viewKey = new ViewKey("OrderInfo");
        RuntimeBindingPlan plan = RuntimeBindingPlan.exact(
                TargetKey.of(viewKey),
                CompiledTargetBinding.propertyPath(viewKey, "amount"));
        TestModelData modelData = new TestModelData();
        modelData.put("amount", Integer.valueOf(10));
        TestContainer container = new TestContainer(executeSuccess);
        RuntimeModelHandle handle = new RuntimeModelHandle(
                new RuntimeModelProvenance(plan, viewKey), modelData, container);
        RuntimeModelFrame frame = new RuntimeModelFrame(
                RuntimeExecutionFrameId.of("frame-1"),
                RuntimeResolutionOwnerId.of("owner-1"),
                null,
                Arrays.asList(handle));
        return new Fixture(
                plan,
                ModelPath.of("amount"),
                modelData,
                container,
                handle,
                new RuntimeModelAccessScope(frame));
    }

    /** DEV-06 测试所需的最小 ModelData 子类，只暴露 protected 初始化能力。 */
    private static final class TestModelData extends ModelData {
        private static final long serialVersionUID = 1L;

        private TestModelData() {
            super();
        }

        private void put(String key, Object value) {
            addKey(key);
            addData(key, value);
        }
    }

    /** 模拟真实 Container 的成功/失败事务结果，并记录 effect 调用次数。 */
    private static final class TestContainer implements Container {
        private final boolean success;
        private int executeCount;
        private ResultInfo result;

        private TestContainer(boolean success) {
            this.success = success;
        }

        @Override
        public Container load(ModelLoader modelLoader) {
            return this;
        }

        @Override
        public Container execute() throws ExecuteRuleException {
            executeCount++;
            result = success
                    ? ResultInfo.success()
                    : ResultInfo.fail("TEST_FAILURE", "forced failure");
            return this;
        }

        @Override
        public Container addListener(ContainerListener listener) {
            return this;
        }

        @Override
        public ResultInfo getResult() {
            return result;
        }
    }

    /** 保存每个测试共享的 exact provenance 和 trusted runtime 对象。 */
    private static final class Fixture {
        private final RuntimeBindingPlan plan;
        private final ModelPath path;
        private final TestModelData modelData;
        private final TestContainer container;
        private final RuntimeModelHandle handle;
        private final RuntimeModelAccessScope scope;

        private Fixture(
                RuntimeBindingPlan plan,
                ModelPath path,
                TestModelData modelData,
                TestContainer container,
                RuntimeModelHandle handle,
                RuntimeModelAccessScope scope) {
            this.plan = plan;
            this.path = path;
            this.modelData = modelData;
            this.container = container;
            this.handle = handle;
            this.scope = scope;
        }

        /** 基于当前 fixture 的 exact plan 创建 resolver 结果，不自报新的 authority。 */
        private ResolvedRuntimeTarget target(
                RuntimeModelSessionId sessionId,
                RuntimeObjectId objectId) {
            return ResolvedRuntimeTarget.of(
                    sessionId,
                    objectId,
                    plan.sourceTargetKey(),
                    RuntimeBindingProof.exact(plan));
        }
    }
}
