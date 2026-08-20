package dec.core.starter.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.AccessOperation;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.SystemKey;
import dec.core.context.runtime.DenialCode;
import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.ProtectedAccessResult;
import dec.core.context.runtime.RuntimeCollectionCursorId;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.context.runtime.RuntimeMutationVersion;
import dec.core.model.runtime.RuntimeModelOperationPort;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32/R33 exact DEV-07 adapter oracle over the real MODEL operation port. */
class ProtectedRuntimeModelAdapterIntegrationTest {

    @Test
    @DisplayName("CASE-P2-TD-MODEL-EFFECT-SAME-HANDLE-001")
    void writeAndReadStayOnTheSameTrustedHandle() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            ProtectedAccessResult write = f.guardedPort.invoke(
                    f.writeInvocation("same-handle-write", RuntimeFactValue.integerValue(20L)));
            assertTrue(write.allowed());
            ProtectedAccessResult read = f.guardedPort.invoke(f.readInvocation("same-handle-read"));
            assertTrue(read.allowed());
            assertEquals(RuntimeFactValue.integerValue(20L), read.readValue().get().value());
            assertEquals(Long.valueOf(20L), f.data.getValue("amount"));
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-OPERATION-PORT-NOT-CALLER-INJECTABLE-001")
    void operationPortIsNotAProductionFactoryInput() {
        for (Method method : ProtectedAccessRuntimeFactory.class.getMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            for (Class<?> parameter : method.getParameterTypes()) {
                assertFalse(RuntimeModelOperationPort.class.isAssignableFrom(parameter));
            }
        }
        assertFalse(Modifier.isPublic(GuardedProtectedAccessPort.class.getModifiers()));
    }

    @Test
    @DisplayName("CASE-P2-TD-REAL-READ-OPERATION-001")
    void readUsesRealModelOperationPortAndReturnsImmutableFact() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            ProtectedAccessResult result = f.guardedPort.invoke(f.readInvocation("real-read"));
            assertTrue(result.allowed());
            assertEquals(RuntimeFactValue.integerValue(10L), result.readValue().get().value());
            assertFalse(result.writeReceipt().isPresent());
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-REAL-WRITE-OPERATION-001")
    void writeUsesRealModelOperationPortAndProducesReceiptOnlyAfterEffect() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            ProtectedAccessResult result = f.guardedPort.invoke(
                    f.writeInvocation("real-write", RuntimeFactValue.integerValue(21L)));
            assertTrue(result.allowed());
            assertTrue(result.writeReceipt().isPresent());
            assertEquals(RuntimeMutationVersion.of(1L), result.writeReceipt().get().committedVersion());
            assertEquals(Long.valueOf(21L), f.data.getValue("amount"));

            f.effect.success = false;
            ProtectedAccessResult rejected = f.guardedPort.invoke(
                    f.writeInvocation("real-write-fail", RuntimeFactValue.integerValue(99L)));
            assertFalse(rejected.allowed());
            assertEquals(DenialCode.RUNTIME_WRITE_FAILED, rejected.denial().get().code());
            assertFalse(rejected.writeReceipt().isPresent());
            assertEquals(Long.valueOf(21L), f.data.getValue("amount"));
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-PRODUCTION-MODEL-ADAPTER-REACHABILITY-001")
    void guardedCoreReachesTheScopeBoundModelPortButDenialHasZeroEffect() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            assertNotNull(f.operationPort);
            ModelAccessRuleKey foreignKey = ModelAccessRuleKey.of(
                    new SystemKey("foreign"), f.targetKey, f.path, AccessOperation.WRITE);
            ProtectedAccessInvocation deniedInvocation = ProtectedAccessInvocation.write(
                    dec.core.context.runtime.ProtectedInvocationId.of("denied-write"),
                    foreignKey,
                    f.frameId,
                    f.ownerId,
                    Optional.<RuntimeCollectionCursorId>empty(),
                    RuntimeFactValue.integerValue(77L));
            ProtectedAccessResult denied = f.guardedPort.invoke(deniedInvocation);
            assertFalse(denied.allowed());
            assertEquals(DenialCode.POLICY_NOT_FOUND, denied.denial().get().code());
            assertEquals(Long.valueOf(10L), f.data.getValue("amount"));
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-WRITE-VALUE-REAL-EFFECT-001")
    void exactFrozenValueIsTheOnlyValueThatReachesTheRealEffect() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            RuntimeFactValue valueA = RuntimeFactValue.stringValue("A");
            ProtectedAccessResult accepted = f.guardedPort.invoke(
                    f.writeInvocation("value-a", valueA));
            assertTrue(accepted.allowed());
            assertTrue(accepted.writeReceipt().isPresent());
            assertEquals("A", f.data.getValue("amount"));
            assertEquals(valueA, f.guardedPort.invoke(f.readInvocation("read-a")).readValue().get().value());

            f.effect.success = false;
            ProtectedAccessResult rejected = f.guardedPort.invoke(
                    f.writeInvocation("value-b", RuntimeFactValue.stringValue("B")));
            assertFalse(rejected.allowed());
            assertFalse(rejected.writeReceipt().isPresent());
            assertEquals("A", f.data.getValue("amount"));
        }
    }
}
