package dec.core.compiler.compat;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/** Declaration 兼容边界：Compiler 继续识别现有配置资源。 */
class P2DeclarationCompatibilityContractTest {

    /**
     * P2 必须继续携带 declaration 兼容 fixture；Compiler 只消费复制后的资源，不反向依赖 XML Parser 模块。
     */
    @Test
    void legacyDeclarationFixturesRemainAvailableAtP2Boundary() {
        ClassLoader loader = getClass().getClassLoader();
        assertNotNull(loader.getResource("test-fixture/system/orm-config.xml"));
        assertNotNull(loader.getResource("test-fixture/mix/rule/order-rule.xml"));
        assertNotNull(loader.getResource("test-fixture/mix/system/systems.xml"));
    }

}
