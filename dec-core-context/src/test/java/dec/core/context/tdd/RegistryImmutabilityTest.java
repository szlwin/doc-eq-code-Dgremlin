package dec.core.context.tdd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryImmutabilityTest {
    private static final String CASE_ID = "CASE-P1-T01-REGISTRY-RED-001";

    @Test
    @DisplayName(CASE_ID + " exposes read-only registry contracts")
    void exposesReadOnlyRegistryContracts() {
        Class<?> definitionKey = ContractReflectionAssertions.requireType(
                CASE_ID, "dec.core.context.model.DefinitionKey");
        Class<?> registry = ContractReflectionAssertions.requireType(
                CASE_ID, "dec.core.context.model.Registry");

        assertTrue(registry.isInterface(),
                "TDD RED [" + CASE_ID + "]: Registry must be an interface");
        ContractReflectionAssertions.requirePublicMethod(CASE_ID, registry, "find", Optional.class, definitionKey);
        ContractReflectionAssertions.requirePublicMethod(CASE_ID, registry, "require", Object.class, definitionKey);
        ContractReflectionAssertions.requirePublicMethod(CASE_ID, registry, "keys", List.class);
        ContractReflectionAssertions.requirePublicMethod(CASE_ID, registry, "size", int.class);
        ContractReflectionAssertions.assertNoPublicMutationApi(CASE_ID, registry);

        Class<?> deferredRegistry = ContractReflectionAssertions.requireType(
                CASE_ID, "dec.core.context.model.DeferredRegistry");
        assertTrue(deferredRegistry.isInterface(),
                "TDD RED [" + CASE_ID + "]: DeferredRegistry must be an interface");
        ContractReflectionAssertions.assertNoPublicMutationApi(CASE_ID, deferredRegistry);

        Class<?> compiledModelSet = ContractReflectionAssertions.requireType(
                CASE_ID, "dec.core.context.model.CompiledModelSet");
        ContractReflectionAssertions.assertStableValueShape(CASE_ID, compiledModelSet);
        ContractReflectionAssertions.assertNoPublicMutationApi(CASE_ID, compiledModelSet);
        ContractReflectionAssertions.assertNoStaticMutableState(CASE_ID, compiledModelSet);
    }
}
