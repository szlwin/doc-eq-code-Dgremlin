package dec.core.context.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.AccessOperation;
import dec.core.context.model.CompiledTargetBinding;
import dec.core.context.model.CompiledViewMaterializationIndex;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.ModelPath;
import dec.core.context.model.SystemKey;
import dec.core.context.model.TargetKey;
import dec.core.context.model.ViewKey;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 plus R33/R31 executable CONTEXT contract. */
class ProtectedAccessContextApiContractTest {

    @Test
    @DisplayName("CASE-P2-TD-CONTEXT-MATERIALIZATION-INDEX-AGGREGATE-001")
    void case_case_p2_td_context_materialization_index_aggregate_001() {
        CompiledViewMaterializationIndex left = CompiledViewMaterializationIndex.empty();
        CompiledViewMaterializationIndex right = CompiledViewMaterializationIndex.empty();
        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertTrue(left.viewKeys().isEmpty());
    }

    @Test
    @DisplayName("CASE-P2-TD-CONTEXT-API-SELF-CONTAINED-001")
    void case_case_p2_td_context_api_self_contained_001() throws Exception {
        ProtectedInvocationId id = ProtectedInvocationId.of("inv-1");
        assertEquals(id, ProtectedInvocationId.of("inv-1"));
        assertFalse(id.equals(ProtectedInvocationId.of("INV-1")));
        assertThrows(IllegalArgumentException.class, () -> ProtectedInvocationId.of(" inv-1"));

        RuntimeWriteIntentId intentId = RuntimeWriteIntentId.of("write-1");
        assertEquals(intentId, RuntimeWriteIntentId.of("write-1"));

        RuntimeBindingProof proof = RuntimeBindingProof.exact("binding-proof-1");
        assertEquals("binding-proof-1", proof.value());
        assertFalse(proof.equals(RuntimeBindingProof.exact("BINDING-PROOF-1")));

        ProtectedWriteReceipt consumerReceipt = ProtectedWriteReceipt.of(
                id, intentId, RuntimeMutationVersion.of(2L));
        assertEquals(id, consumerReceipt.invocationId());
        assertEquals(intentId, consumerReceipt.writeIntentId());
        assertEquals(RuntimeMutationVersion.of(2L), consumerReceipt.committedVersion());

        ProtectedAccessDenial denial = ProtectedAccessDenial.of(
                id, DenialCode.POLICY_NOT_FOUND, "policy not found");
        ProtectedAccessResult denied = ProtectedAccessResult.deny(denial);
        assertFalse(denied.allowed());
        assertEquals(Optional.of(denial), denied.denial());
        assertFalse(denied.readValue().isPresent());
        assertFalse(denied.writeReceipt().isPresent());
        assertEquals(denied, ProtectedAccessResult.deny(
                ProtectedAccessDenial.of(
                        ProtectedInvocationId.of("inv-1"),
                        DenialCode.POLICY_NOT_FOUND,
                        "policy not found")));

        RuntimeTargetResolution resolution = RuntimeTargetResolution.denied(
                RuntimeTargetResolutionStatus.NOT_FOUND,
                DenialCode.RUNTIME_TARGET_NOT_FOUND);
        assertEquals(RuntimeTargetResolutionStatus.NOT_FOUND, resolution.status());
        assertFalse(resolution.target().isPresent());
        assertEquals(Optional.of(DenialCode.RUNTIME_TARGET_NOT_FOUND), resolution.denialCode());
        assertThrows(IllegalArgumentException.class, () -> RuntimeTargetResolution.denied(
                RuntimeTargetResolutionStatus.RESOLVED,
                DenialCode.RUNTIME_TARGET_NOT_FOUND));

        ProtectedAccessInvocation.class.getMethod(
                "of",
                ProtectedInvocationId.class,
                ModelAccessRuleKey.class,
                RuntimeExecutionFrameId.class,
                RuntimeResolutionOwnerId.class,
                Optional.class);
        ProtectedAccessInvocation.class.getMethod(
                "write",
                ProtectedInvocationId.class,
                ModelAccessRuleKey.class,
                RuntimeExecutionFrameId.class,
                RuntimeResolutionOwnerId.class,
                Optional.class,
                RuntimeFactValue.class);
        ResolvedRuntimeTarget.class.getMethod(
                "of",
                RuntimeModelSessionId.class,
                RuntimeObjectId.class,
                TargetKey.class,
                CompiledTargetBinding.class,
                RuntimeExecutionFrameId.class,
                RuntimeResolutionOwnerId.class,
                Optional.class,
                RuntimeBindingProof.class);
        ResolvedProtectedReadAccess.class.getMethod(
                "of", ProtectedInvocationId.class, ModelAccessRuleKey.class, ResolvedRuntimeTarget.class);
        ResolvedWriteIntent.class.getMethod(
                "of",
                RuntimeWriteIntentId.class,
                ModelAccessRuleKey.class,
                Optional.class,
                ResolvedRuntimeTarget.class,
                RuntimeMutationStamp.class);
        ResolvedWriteIntent.class.getMethod(
                "of",
                RuntimeWriteIntentId.class,
                ModelAccessRuleKey.class,
                Optional.class,
                ResolvedRuntimeTarget.class,
                RuntimeMutationStamp.class,
                RuntimeFactValue.class);
        ResolvedProtectedWriteAccess.class.getMethod(
                "of", ProtectedInvocationId.class, ResolvedWriteIntent.class);
        ProtectedAccessPort.class.getMethod("invoke", ProtectedAccessInvocation.class);
    }

    @Test
    @DisplayName("DEV-P2-DEV04R-R03-R31-WRITE-VALUE-TRANSPORT")
    void r31_write_value_is_frozen_and_value_less_intent_fails_closed() {
        TargetKey targetKey = TargetKey.of(new ViewKey("Order"));
        ModelPath path = ModelPath.of("user.authInfo");
        ModelAccessRuleKey writeKey = ModelAccessRuleKey.of(
                new SystemKey("Trade"), targetKey, path, AccessOperation.WRITE);
        ProtectedInvocationId invocationId = ProtectedInvocationId.of("inv-write-1");
        RuntimeExecutionFrameId frameId = RuntimeExecutionFrameId.of("frame-1");
        RuntimeResolutionOwnerId ownerId = RuntimeResolutionOwnerId.of("owner-1");
        RuntimeFactValue value = RuntimeFactValue.stringValue("A");

        ProtectedAccessInvocation readShape = ProtectedAccessInvocation.of(
                invocationId, writeKey, frameId, ownerId, Optional.<RuntimeCollectionCursorId>empty());
        assertFalse(readShape.writeValue().isPresent());

        ProtectedAccessInvocation writeInvocation = ProtectedAccessInvocation.write(
                invocationId,
                writeKey,
                frameId,
                ownerId,
                Optional.<RuntimeCollectionCursorId>empty(),
                value);
        assertEquals(Optional.of(value), writeInvocation.writeValue());
        assertThrows(NullPointerException.class, () -> ProtectedAccessInvocation.write(
                invocationId,
                writeKey,
                frameId,
                ownerId,
                Optional.<RuntimeCollectionCursorId>empty(),
                null));

        RuntimeModelSessionId sessionId = RuntimeModelSessionId.of("session-1");
        RuntimeObjectId objectId = RuntimeObjectId.of("object-1");
        ResolvedRuntimeTarget target = ResolvedRuntimeTarget.of(
                sessionId, objectId, targetKey, RuntimeBindingProof.exact("proof-1"));
        RuntimeMutationStamp stamp = RuntimeMutationStamp.of(
                sessionId, objectId, path, RuntimeMutationVersion.of(7L));
        RuntimeWriteIntentId intentId = RuntimeWriteIntentId.of("intent-1");

        ResolvedWriteIntent valueLessIntent = ResolvedWriteIntent.of(
                intentId, writeKey, Optional.empty(), target, stamp);
        assertFalse(valueLessIntent.writeValue().isPresent());
        assertThrows(
                IllegalArgumentException.class,
                () -> ResolvedProtectedWriteAccess.of(invocationId, valueLessIntent));

        ResolvedWriteIntent executableIntent = ResolvedWriteIntent.of(
                intentId, writeKey, Optional.empty(), target, stamp, value);
        assertEquals(Optional.of(value), executableIntent.writeValue());
        ResolvedProtectedWriteAccess writeAccess =
                ResolvedProtectedWriteAccess.of(invocationId, executableIntent);
        assertEquals(value, writeAccess.value());
        assertEquals(target, writeAccess.target());
        assertEquals(path, writeAccess.modelPath());
        assertEquals(stamp, writeAccess.mutationStamp());
    }
}
