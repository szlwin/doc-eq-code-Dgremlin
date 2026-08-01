package dec.core.context.tdd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextValueContractTest {
    private static final String CASE_ID = "CASE-P1-T01-CONTEXT-VALUE-RED-001";

    @Test
    @DisplayName(CASE_ID + " publishes the neutral immutable value-type closure")
    void publishesNeutralImmutableValueTypeClosure() {
        List<String> requiredTypes = Arrays.asList(
                "dec.core.context.model.SourceRef",
                "dec.core.context.model.DiagnosticCode",
                "dec.core.context.model.Diagnostic",
                "dec.core.context.model.DefinitionKey",
                "dec.core.context.model.DataSourceKey",
                "dec.core.context.model.ConnectionKey",
                "dec.core.context.model.DataKey",
                "dec.core.context.model.ViewKey",
                "dec.core.context.model.SystemKey",
                "dec.core.context.model.RuleViewKey",
                "dec.core.context.model.BusinessScopeKey",
                "dec.core.context.model.InformationKey",
                "dec.core.context.model.DirectoryKey",
                "dec.core.context.model.ActionKey",
                "dec.core.context.model.ProduceKey",
                "dec.core.context.model.DeferredKind",
                "dec.core.context.model.RequiredStage",
                "dec.core.context.model.DeferredDefinition",
                "dec.core.context.model.CompiledDefinition",
                "dec.core.context.model.DigestPair",
                "dec.core.context.model.CompiledModelSet"
        );

        for (String typeName : requiredTypes) {
            Class<?> type = ContractReflectionAssertions.requireType(CASE_ID, typeName);
            ContractReflectionAssertions.assertStableValueShape(CASE_ID, type);
        }

        Class<?> definitionKey = ContractReflectionAssertions.requireType(
                CASE_ID, "dec.core.context.model.DefinitionKey");
        assertTrue(definitionKey.isInterface(),
                "TDD RED [" + CASE_ID + "]: DefinitionKey must be an interface");
        ContractReflectionAssertions.requirePublicMethod(CASE_ID, definitionKey, "canonical", String.class);

        Class<?> informationKey = ContractReflectionAssertions.requireType(
                CASE_ID, "dec.core.context.model.InformationKey");
        assertTrue(definitionKey.isAssignableFrom(informationKey),
                "TDD RED [" + CASE_ID + "]: InformationKey must implement DefinitionKey");
        ContractReflectionAssertions.requirePublicMethod(CASE_ID, informationKey, "canonical", String.class);
        assertEquals("dec.core.context.model", informationKey.getPackage().getName(),
                "TDD RED [" + CASE_ID + "]: context values must stay in the neutral model package");
    }
}
