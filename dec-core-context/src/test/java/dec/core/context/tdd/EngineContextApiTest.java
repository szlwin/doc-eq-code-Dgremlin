package dec.core.context.tdd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineContextApiTest {
    private static final String CASE_ID = "CASE-P1-T01-ENGINE-CONTEXT-RED-001";

    @Test
    @DisplayName(CASE_ID + " exposes an explicit read-only context boundary")
    void exposesExplicitReadOnlyContextBoundary() {
        Class<?> compiledModelSet = ContractReflectionAssertions.requireType(
                CASE_ID, "dec.core.context.model.CompiledModelSet");
        Class<?> engineContext = ContractReflectionAssertions.requireType(
                CASE_ID, "dec.core.context.EngineContext");
        Class<?> projection = ContractReflectionAssertions.requireType(
                CASE_ID, "dec.core.context.CoreConfigProjection");

        assertEquals("dec.core.context", engineContext.getPackage().getName(),
                "TDD RED [" + CASE_ID + "]: EngineContext must remain in the neutral context package");
        ContractReflectionAssertions.assertStableValueShape(CASE_ID, engineContext);
        ContractReflectionAssertions.assertNoPublicMutationApi(CASE_ID, engineContext);
        ContractReflectionAssertions.assertNoStaticMutableState(CASE_ID, engineContext);

        boolean constructorAcceptsCompleteModel = false;
        for (Constructor<?> constructor : engineContext.getConstructors()) {
            if (Arrays.asList(constructor.getParameterTypes()).contains(compiledModelSet)) {
                constructorAcceptsCompleteModel = true;
            }
            assertFalse(constructor.getParameterCount() == 0,
                    "TDD RED [" + CASE_ID + "]: EngineContext must not expose an empty constructor");
        }
        assertTrue(constructorAcceptsCompleteModel,
                "TDD RED [" + CASE_ID + "]: a public EngineContext constructor must receive CompiledModelSet");

        ContractReflectionAssertions.assertNoPublicMutationApi(CASE_ID, projection);
        ContractReflectionAssertions.assertNoStaticMutableState(CASE_ID, projection);
        ContractReflectionAssertions.requirePublicMethod(CASE_ID, projection, "data", java.util.List.class);
        ContractReflectionAssertions.requirePublicMethod(CASE_ID, projection, "views", java.util.List.class);
        ContractReflectionAssertions.requirePublicMethod(CASE_ID, projection, "rules", java.util.List.class);
    }
}
