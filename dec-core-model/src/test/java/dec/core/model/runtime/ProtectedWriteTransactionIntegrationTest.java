package dec.core.model.runtime;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class ProtectedWriteTransactionIntegrationTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.model.runtime.RuntimeModelAccessScope", "dec.core.model.runtime.RuntimeModelSession"};

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-WRITE-ROLLBACK-001")
    void case_case_p2_td_runtime_write_rollback_001() {
        observe("CASE-P2-TD-RUNTIME-WRITE-ROLLBACK-001");
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
