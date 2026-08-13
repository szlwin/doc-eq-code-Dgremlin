package dec.core.model.runtime;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TESTDESIGN-P2-R32 的 opaque runtime contract。
 *
 * <p>该契约必须由拥有 RuntimeModelHandle 的 MODEL 模块执行；如果放在上游 CONTEXT 模块，
 * Maven reactor 的合法依赖方向会导致下游生产类永远不在测试 classpath，从而产生伪 RED。
 */
class OpaqueRuntimeIdContractTest {
    private static final String[] REQUIRED_CONTRACTS = new String[] {
            "dec.core.model.runtime.RuntimeModelHandle"
    };

    @Test
    @DisplayName("CASE-P2-TD-OPAQUE-RUNTIME-ID-VALUE-CONTRACT-001")
    void case_case_p2_td_opaque_runtime_id_value_contract_001() {
        observe("CASE-P2-TD-OPAQUE-RUNTIME-ID-VALUE-CONTRACT-001");
    }

    /** 在生产类型所属模块验证真实 classpath 可见性，禁止通过上游 test stub 伪造通过。 */
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
