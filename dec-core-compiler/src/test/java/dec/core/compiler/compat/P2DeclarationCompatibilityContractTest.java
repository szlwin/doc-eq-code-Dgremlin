package dec.core.compiler.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.RuntimeFactValue;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

/** DEV-09 declaration 兼容边界：保留旧入口，但禁止重新引入已退役的 authority/token 语义。 */
class P2DeclarationCompatibilityContractTest {

    /**
     * P2 必须保留现有 declaration/XML runtime 入口，删除动作属于后续阶段而不是 DEV-09。
     */
    @Test
    void legacyDeclarationRuntimeRemainsAvailableAtP2Boundary() throws Exception {
        assertNotNull(Class.forName("dec.context.parse.xml.parse.config.ConfigFileParser"));
        assertNotNull(Class.forName("dec.core.model.container.ContainerFactory"));
        assertNotNull(Class.forName("dec.core.model.container.ModelLoader"));
    }

    /**
     * R31 WRITE 只能携带中立 RuntimeFactValue；公开 invocation API 不允许出现 token/version authority 参数。
     */
    @Test
    void protectedInvocationCarriesNeutralWriteValueWithoutAuthorityToken() {
        Method write = null;
        for (Method method : ProtectedAccessInvocation.class.getMethods()) {
            if ("write".equals(method.getName())) {
                write = method;
                break;
            }
        }
        assertNotNull(write);
        assertTrue(contains(write.getParameterTypes(), RuntimeFactValue.class));
        for (Class<?> parameterType : write.getParameterTypes()) {
            String name = parameterType.getSimpleName().toLowerCase();
            assertFalse(name.contains("token"), "authority token leaked into P2 API: " + parameterType);
            assertFalse(name.contains("version"), "authority version leaked into P2 API: " + parameterType);
        }
    }

    /** 判断公开签名是否精确包含指定中立类型。 */
    private static boolean contains(Class<?>[] values, Class<?> expected) {
        for (Class<?> value : values) {
            if (value.equals(expected)) {
                return true;
            }
        }
        return false;
    }
}
