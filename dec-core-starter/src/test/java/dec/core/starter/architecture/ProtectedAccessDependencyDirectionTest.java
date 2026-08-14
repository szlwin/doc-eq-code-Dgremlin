package dec.core.starter.architecture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.data.ModelData;
import dec.core.model.runtime.RuntimeModelEffectProvider;
import dec.core.model.runtime.RuntimeModelExecutionRoot;
import dec.core.model.runtime.RuntimeModelHandle;
import dec.core.model.runtime.RuntimeModelLoadRequest;
import dec.core.model.runtime.RuntimeModelOperationPort;
import dec.core.starter.access.ChangeProtectedAccessEntry;
import dec.core.starter.access.CustomActionProtectedAccessEntry;
import dec.core.starter.access.ProtectedAccessComposition;
import dec.core.starter.access.ProtectedAccessRuntimeFactory;
import dec.core.starter.access.RuleProtectedAccessEntry;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 structural proof that business-facing STARTER APIs cannot bypass composition. */
class ProtectedAccessDependencyDirectionTest {

    @Test
    @DisplayName("CASE-P2-TD-DOWNSTREAM-DEPENDENCY-DIRECTION-001")
    void publicStarterConsumerSurfaceDoesNotExposeModelBypassTypes() {
        Class<?>[] publicSurface = new Class<?>[] {
            ProtectedAccessRuntimeFactory.class,
            ProtectedAccessComposition.class,
            RuleProtectedAccessEntry.class,
            ChangeProtectedAccessEntry.class,
            CustomActionProtectedAccessEntry.class
        };
        boolean createFound = false;
        for (Class<?> type : publicSurface) {
            for (Method method : type.getMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) continue;
                if (type == ProtectedAccessRuntimeFactory.class && "create".equals(method.getName())) {
                    createFound = true;
                }
                assertAllowed(method.getReturnType());
                for (Class<?> parameter : method.getParameterTypes()) {
                    assertAllowed(parameter);
                }
            }
        }
        assertTrue(createFound);
    }

    private static void assertAllowed(Class<?> type) {
        assertFalse(RuntimeModelExecutionRoot.class.isAssignableFrom(type));
        assertFalse(RuntimeModelLoadRequest.class.isAssignableFrom(type));
        assertFalse(RuntimeModelHandle.class.isAssignableFrom(type));
        assertFalse(ModelData.class.isAssignableFrom(type));
        assertFalse(RuntimeModelEffectProvider.class.isAssignableFrom(type));
        assertFalse(RuntimeModelOperationPort.class.isAssignableFrom(type));
    }
}
