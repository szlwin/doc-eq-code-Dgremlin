package dec.core.compiler.system;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class SystemCompilationContractTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.context.model.SystemKey", "dec.core.context.model.TypedDefinitionRegistries"};

    @Test
    @DisplayName("CASE-P2-TD-SYSTEM-DETERMINISM-001")
    void case_case_p2_td_system_determinism_001() {
        observe("CASE-P2-TD-SYSTEM-DETERMINISM-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-SYSTEM-DUPLICATE-001")
    void case_case_p2_td_system_duplicate_001() {
        observe("CASE-P2-TD-SYSTEM-DUPLICATE-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-SYSTEM-FORWARD-REF-001")
    void case_case_p2_td_system_forward_ref_001() {
        observe("CASE-P2-TD-SYSTEM-FORWARD-REF-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001")
    void case_case_p2_td_system_ownership_snapshot_001() {
        observe("CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001")
    void case_case_p2_td_system_version_identity_001() {
        observe("CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-BM-CANONICAL-PAIR-001")
    void case_case_p2_td_bm_canonical_pair_001() {
        observe("CASE-P2-TD-BM-CANONICAL-PAIR-001");
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
