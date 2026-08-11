package dec.demo.p2;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class P2RealFixtureIntegrationTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.model.runtime.RuntimeModelExecutionRoot", "dec.core.context.model.TargetKey"};

    @Test
    @DisplayName("CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001")
    void case_case_p2_td_dynamic_classifier_real_001() {
        observe("CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-SOURCE-TO-READ-WRITE-OPERATION-001")
    void case_case_p2_td_source_to_read_write_operation_001() {
        observe("CASE-P2-TD-SOURCE-TO-READ-WRITE-OPERATION-001");
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
