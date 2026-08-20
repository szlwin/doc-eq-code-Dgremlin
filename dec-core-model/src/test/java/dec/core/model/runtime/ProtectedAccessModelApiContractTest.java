package dec.core.model.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 exact MODEL API/session contract bound to concrete runtime behavior. */
class ProtectedAccessModelApiContractTest {
    private final RuntimeModelTrustedLoadBehaviorTest trustedLoad = new RuntimeModelTrustedLoadBehaviorTest();
    private final RuntimeModelSessionEffectBehaviorTest sessionEffect = new RuntimeModelSessionEffectBehaviorTest();

    @Test
    @DisplayName("CASE-P2-TD-MODEL-API-SELF-CONTAINED-001")
    void case_case_p2_td_model_api_self_contained_001() throws Exception {
        trustedLoad.productionApiHasNoModelDataInjectionSeam();
        trustedLoad.successfulLoadUsesCapturedPlanAndSameModelData();
        trustedLoad.rejectsCallerSuppliedModelData();
    }

    @Test
    @DisplayName("CASE-P2-TD-SESSION-FAILURE-ALGEBRA-001")
    void case_case_p2_td_session_failure_algebra_001() throws Exception {
        sessionEffect.registrationUsesExactHandleIdentityAndExclusiveLease();
        sessionEffect.effectProviderBindsOnlySameScopeSealedSession();
        sessionEffect.failedWriteRestoresPathValueWithoutReceipt();
    }
}
