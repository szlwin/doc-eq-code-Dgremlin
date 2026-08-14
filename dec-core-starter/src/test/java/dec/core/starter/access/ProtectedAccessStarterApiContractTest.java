package dec.core.starter.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.EngineContext;
import dec.core.context.runtime.DenialCode;
import dec.core.context.runtime.ProtectedAccessResult;
import dec.core.model.runtime.RuntimeModelAccessScope;
import dec.core.model.runtime.RuntimeModelEffectProvider;
import dec.core.model.runtime.RuntimeModelOperationPort;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32/R33 exact DEV-07 public-boundary and missing-WRITE-value oracle. */
class ProtectedAccessStarterApiContractTest {

    @Test
    @DisplayName("CASE-P2-TD-STARTER-API-SELF-CONTAINED-001")
    void starterApiIsSelfContainedAndHasNoPublicEffectInjectionSeam() throws Exception {
        Method production = ProtectedAccessRuntimeFactory.class.getMethod("production", EngineContext.class);
        assertNotNull(production);
        Method create = ProtectedAccessRuntimeFactory.class.getMethod("create", RuntimeModelAccessScope.class);
        assertNotNull(create);

        for (Constructor<?> constructor : ProtectedAccessRuntimeFactory.class.getConstructors()) {
            assertNoEffectInjection(constructor.getParameterTypes());
        }
        for (Method method : ProtectedAccessRuntimeFactory.class.getMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                assertNoEffectInjection(method.getParameterTypes());
            }
        }
        assertFalse(Modifier.isPublic(GuardedProtectedAccessPort.class.getModifiers()));
        assertFalse(Modifier.isPublic(ExactModelAccessGuard.class.getModifiers()));
    }

    @Test
    @DisplayName("CASE-P2-TD-CALLER-STARTER-CONSTRUCTIBILITY-001")
    void callerCanConstructOnlyTheProductionRootFromCapturedContext() throws Exception {
        try (ProtectedAccessRuntimeTestFixture fixture = new ProtectedAccessRuntimeTestFixture()) {
            ProtectedAccessRuntimeFactory factory = ProtectedAccessRuntimeFactory.production(fixture.context);
            assertNotNull(factory);
            assertEquals(fixture.context, factory.capturedContext());
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-WRITE-VALUE-REQUIRED-001")
    void valueLessWriteIsDeniedBeforeAnyEffect() throws Exception {
        try (ProtectedAccessRuntimeTestFixture fixture = new ProtectedAccessRuntimeTestFixture()) {
            ProtectedAccessResult result = fixture.guardedPort.invoke(
                    fixture.valueLessWriteInvocation("missing-write-value"));
            assertFalse(result.allowed());
            assertTrue(result.denial().isPresent());
            assertEquals(DenialCode.WRITE_INTENT_NOT_FOUND, result.denial().get().code());
            assertEquals(Long.valueOf(10L), fixture.data.getValue("amount"));
            assertFalse(result.writeReceipt().isPresent());
        }
    }

    private static void assertNoEffectInjection(Class<?>[] parameterTypes) {
        for (Class<?> parameterType : parameterTypes) {
            assertFalse(RuntimeModelOperationPort.class.isAssignableFrom(parameterType));
            assertFalse(RuntimeModelEffectProvider.class.isAssignableFrom(parameterType));
            assertFalse(ExactModelAccessGuard.class.isAssignableFrom(parameterType));
        }
    }
}
