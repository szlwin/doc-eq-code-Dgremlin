package dec.core.starter.access;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class ProtectedAccessConcurrencyTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.model.runtime.RuntimeModelSession", "dec.core.context.model.ModelAccessRuleKey"};

    @Test
    @DisplayName("CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001")
    void case_case_p2_td_capability_concurrent_consume_001() {
        observe("CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001")
    void case_case_p2_td_different_capability_concurrency_001() {
        observe("CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-CROSS-SESSION-MODELDATA-OWNERSHIP-001")
    void case_case_p2_td_cross_session_modeldata_ownership_001() {
        observe("CASE-P2-TD-CROSS-SESSION-MODELDATA-OWNERSHIP-001");
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
