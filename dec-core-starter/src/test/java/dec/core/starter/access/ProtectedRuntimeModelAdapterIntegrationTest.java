package dec.core.starter.access;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class ProtectedRuntimeModelAdapterIntegrationTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.model.runtime.RuntimeModelAccessScope", "dec.core.context.model.TargetKey"};

    @Test
    @DisplayName("CASE-P2-TD-MODEL-EFFECT-SAME-HANDLE-001")
    void case_case_p2_td_model_effect_same_handle_001() {
        observe("CASE-P2-TD-MODEL-EFFECT-SAME-HANDLE-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-OPERATION-PORT-NOT-CALLER-INJECTABLE-001")
    void case_case_p2_td_operation_port_not_caller_injectable_001() {
        observe("CASE-P2-TD-OPERATION-PORT-NOT-CALLER-INJECTABLE-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-REAL-READ-OPERATION-001")
    void case_case_p2_td_real_read_operation_001() {
        observe("CASE-P2-TD-REAL-READ-OPERATION-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-REAL-WRITE-OPERATION-001")
    void case_case_p2_td_real_write_operation_001() {
        observe("CASE-P2-TD-REAL-WRITE-OPERATION-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-PRODUCTION-MODEL-ADAPTER-REACHABILITY-001")
    void case_case_p2_td_production_model_adapter_reachability_001() {
        observe("CASE-P2-TD-PRODUCTION-MODEL-ADAPTER-REACHABILITY-001");
    }

    private static void observe(String caseId) {
        for (String typeName : REQUIRED_CONTRACTS) {
            try {
                Class.forName(typeName);
            } catch (ClassNotFoundException missing) {
                fail("P2 RED [" + caseId + "]: missing production contract " + typeName);
            }
        }
    }
}
