package dec.core.starter.architecture;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class ProtectedAccessDependencyDirectionTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.context.model.ModelAccessRuleKey"};

    @Test
    @DisplayName("CASE-P2-TD-DOWNSTREAM-DEPENDENCY-DIRECTION-001")
    void case_case_p2_td_downstream_dependency_direction_001() {
        observe("CASE-P2-TD-DOWNSTREAM-DEPENDENCY-DIRECTION-001");
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
