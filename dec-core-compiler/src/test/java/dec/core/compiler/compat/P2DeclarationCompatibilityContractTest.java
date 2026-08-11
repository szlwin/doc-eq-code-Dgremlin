package dec.core.compiler.compat;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class P2DeclarationCompatibilityContractTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.context.model.RuleViewKey", "dec.core.context.model.SystemKey"};

    @Test
    @DisplayName("CASE-P2-TD-DECLARATION-BOUNDARY-001")
    void case_case_p2_td_declaration_boundary_001() {
        observe("CASE-P2-TD-DECLARATION-BOUNDARY-001");
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
