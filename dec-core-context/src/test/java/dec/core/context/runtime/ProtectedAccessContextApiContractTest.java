package dec.core.context.runtime;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class ProtectedAccessContextApiContractTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.context.model.CompiledViewMaterializationIndex", "dec.core.context.runtime.RuntimeFactValue"};

    @Test
    @DisplayName("CASE-P2-TD-CONTEXT-MATERIALIZATION-INDEX-AGGREGATE-001")
    void case_case_p2_td_context_materialization_index_aggregate_001() {
        observe("CASE-P2-TD-CONTEXT-MATERIALIZATION-INDEX-AGGREGATE-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-CONTEXT-API-SELF-CONTAINED-001")
    void case_case_p2_td_context_api_self_contained_001() {
        observe("CASE-P2-TD-CONTEXT-API-SELF-CONTAINED-001");
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
