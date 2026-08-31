package dec.core.compiler.diagnostic;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class P2DiagnosticDeterminismTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.context.model.ModelAccessRuleKey"};

    @Test
    @DisplayName("CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001")
    void case_case_p2_td_diagnostic_determinism_001() {
        observe("CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001")
    void case_case_p2_td_runtime_denial_diagnostic_determinism_001() {
        observe("CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001");
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
