package dec.core.compiler.ruleview;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */
class RuleViewCompilationContractTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {"dec.core.context.model.RuleViewKey"};

    @Test
    @DisplayName("CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001")
    void case_case_p2_td_ruleview_system_required_001() {
        observe("CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001")
    void case_case_p2_td_ruleview_same_system_duplicate_001() {
        observe("CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001")
    void case_case_p2_td_ruleview_cross_system_isolation_001() {
        observe("CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001")
    void case_case_p2_td_ruleview_view_resolution_001() {
        observe("CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-RULEKEY-CONTRACT-001")
    void case_case_p2_td_rulekey_contract_001() {
        observe("CASE-P2-TD-RULEKEY-CONTRACT-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001")
    void case_case_p2_td_ruleview_composite_lookup_001() {
        observe("CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-KEY-SOURCE-COMPAT-001")
    void case_case_p2_td_key_source_compat_001() {
        observe("CASE-P2-TD-KEY-SOURCE-COMPAT-001");
    }

    @Test
    @DisplayName("CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001")
    void case_case_p2_td_bare_name_compatibility_boundary_001() {
        observe("CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001");
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
