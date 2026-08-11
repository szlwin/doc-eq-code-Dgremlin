package dec.core.model.runtime;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class RuntimeObjectLocatorIntegrationTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.model.runtime.RuntimeModelSession", "dec.core.model.runtime.RuntimeModelHandle"};

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-OBJECT-LOCATOR-SCOPE-001")
    void case_case_p2_td_runtime_object_locator_scope_001() {
        observe("CASE-P2-TD-RUNTIME-OBJECT-LOCATOR-SCOPE-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-OBJECT-NOT-FOUND-STALE-001")
    void case_case_p2_td_runtime_object_not_found_stale_001() {
        observe("CASE-P2-TD-RUNTIME-OBJECT-NOT-FOUND-STALE-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-TARGET-SELECTION-001")
    void case_case_p2_td_runtime_target_selection_001() {
        observe("CASE-P2-TD-RUNTIME-TARGET-SELECTION-001");
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
