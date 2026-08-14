package dec.core.context.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.CompiledViewMaterializationIndex;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 executable CONTEXT contract. */
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
                dec.core.context.model.ModelAccessRuleKey.class,
                RuntimeExecutionFrameId.class,
                RuntimeResolutionOwnerId.class,
                Optional.class);
        ResolvedWriteIntent.class.getMethod(
                "of",
                RuntimeWriteIntentId.class,
                dec.core.context.model.ModelAccessRuleKey.class,
                Optional.class,
                ResolvedRuntimeTarget.class,
                RuntimeMutationStamp.class);
        ProtectedAccessPort.class.getMethod("invoke", ProtectedAccessInvocation.class);
    }
}
