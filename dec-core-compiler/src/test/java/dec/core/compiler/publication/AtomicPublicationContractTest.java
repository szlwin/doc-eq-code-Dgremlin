package dec.core.compiler.publication;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class AtomicPublicationContractTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.context.model.CompiledViewMaterializationIndex", "dec.core.context.model.ModelAccessRuleKey"};

    @Test
    @DisplayName("CASE-P2-TD-MATERIALIZATION-PUBLICATION-CLOSURE-001")
    void case_case_p2_td_materialization_publication_closure_001() {
        observe("CASE-P2-TD-MATERIALIZATION-PUBLICATION-CLOSURE-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-ATOMIC-PUBLICATION-001")
    void case_case_p2_td_atomic_publication_001() {
        observe("CASE-P2-TD-ATOMIC-PUBLICATION-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-CONTEXT-ISOLATION-001")
    void case_case_p2_td_context_isolation_001() {
        observe("CASE-P2-TD-CONTEXT-ISOLATION-001");
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
