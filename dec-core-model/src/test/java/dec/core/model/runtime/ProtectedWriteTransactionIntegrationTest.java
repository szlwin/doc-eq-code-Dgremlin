package dec.core.model.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dec.core.context.data.ModelData;
import dec.core.context.model.CompiledTargetBinding;
import dec.core.context.model.ModelPath;
import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.model.TargetKey;
import dec.core.context.model.ViewKey;
import dec.core.context.runtime.ProtectedWriteReceipt;
import dec.core.context.runtime.ResolvedProtectedWriteAccess;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.RuntimeBindingProof;
import dec.core.context.runtime.RuntimeExecutionFrameId;
import dec.core.context.runtime.RuntimeFactValue;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProtectedWriteTransactionIntegrationTest {
    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-WRITE-ROLLBACK-001")
    void writeFailureHasNoReceiptAndKeepsVersion() throws Exception {
        ViewKey view = new ViewKey("OrderInfo");
        ModelPath path = ModelPath.of("amount");
        RuntimeBindingPlan plan = RuntimeBindingPlan.exact(
                TargetKey.of(view), CompiledTargetBinding.propertyPath(view, "amount"));
        Data data = new Data();
        data.put("amount", Integer.valueOf(10));
        Effect container = new Effect();
        RuntimeModelHandle handle = new RuntimeModelHandle(
                new RuntimeModelProvenance(plan, view), data, container);
        RuntimeModelAccessScope scope = new RuntimeModelAccessScope(new RuntimeModelFrame(
                RuntimeExecutionFrameId.of("txn-frame"), RuntimeResolutionOwnerId.of("txn-owner"),
                null, Arrays.asList(handle)));
        RuntimeModelSession session = scope.beginSession();
        RuntimeObjectId objectId = session.register(handle);
        session.seal();
        ResolvedRuntimeTarget target = ResolvedRuntimeTarget.of(
                session.sessionId(), objectId, plan.sourceTargetKey(), RuntimeBindingProof.exact(plan));
        RuntimeModelOperationPort port = scope.effectProvider().bind(session).operationPort().get();
        RuntimeMutationStamp stamp = RuntimeMutationStamp.of(
                session.sessionId(), objectId, path, RuntimeMutationVersion.of(0L));

        ProtectedWriteReceipt rejected = port.write(ResolvedProtectedWriteAccess.of(
                target, path, RuntimeFactValue.integerValue(99L), stamp));
        assertNull(rejected);
        assertEquals(Integer.valueOf(10), data.getValue("amount"));
        assertEquals(RuntimeMutationVersion.of(0L), session.currentVersion(target, path));

        container.success = true;
        ProtectedWriteReceipt accepted = port.write(ResolvedProtectedWriteAccess.of(
                target, path, RuntimeFactValue.integerValue(20L), stamp));
        assertNotNull(accepted);
        assertEquals(RuntimeMutationVersion.of(1L), accepted.version());
        assertEquals(Long.valueOf(20L), data.getValue("amount"));

        assertNull(port.write(ResolvedProtectedWriteAccess.of(
                target, path, RuntimeFactValue.integerValue(30L), stamp)));
        assertEquals(Long.valueOf(20L), data.getValue("amount"));
    }

    private static final class Data extends ModelData {
        private static final long serialVersionUID = 1L;
        private Data() { super(); }
        private void put(String key, Object value) { addKey(key); addData(key, value); }
    }

    private static final class Effect implements Container {
        private boolean success;
        @Override public Container load(ModelLoader loader) { return this; }
        @Override public Container execute() throws ExecuteRuleException { return this; }
        @Override public Container addListener(ContainerListener listener) { return this; }
        @Override public ResultInfo getResult() {
            return success ? ResultInfo.success() : ResultInfo.fail("TEST", "rejected");
        }
    }
}
