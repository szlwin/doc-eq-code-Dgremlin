package dec.core.model.runtime;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class ProtectedAccessModelApiContractTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.model.runtime.RuntimeModelExecutionRoot", "dec.core.model.runtime.RuntimeModelLoadRequest"};

    @Test
    @DisplayName("CASE-P2-TD-MODEL-API-SELF-CONTAINED-001")
    void case_case_p2_td_model_api_self_contained_001() {
        observe("CASE-P2-TD-MODEL-API-SELF-CONTAINED-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-SESSION-FAILURE-ALGEBRA-001")
    void case_case_p2_td_session_failure_algebra_001() {
        observe("CASE-P2-TD-SESSION-FAILURE-ALGEBRA-001");
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
