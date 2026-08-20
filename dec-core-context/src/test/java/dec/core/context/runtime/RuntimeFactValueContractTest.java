package dec.core.context.runtime;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class RuntimeFactValueContractTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.context.runtime.RuntimeFactValue"};

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-FACT-VALUE-DOMAIN-001")
    void case_case_p2_td_runtime_fact_value_domain_001() {
        observe("CASE-P2-TD-RUNTIME-FACT-VALUE-DOMAIN-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-FACT-VALUE-DEEP-IMMUTABILITY-001")
    void case_case_p2_td_runtime_fact_value_deep_immutability_001() {
        observe("CASE-P2-TD-RUNTIME-FACT-VALUE-DEEP-IMMUTABILITY-001");
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
