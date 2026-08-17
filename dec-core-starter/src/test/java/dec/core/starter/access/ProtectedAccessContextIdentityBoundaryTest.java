package dec.core.starter.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dec.core.context.EngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** R34 exact-context provenance oracle: structural equality must never imply runtime ownership. */
class ProtectedAccessContextIdentityBoundaryTest {

    @Test
    @DisplayName("CASE-P2-TD-R34-SAME-PLAN-FOREIGN-CONTEXT-REJECT-001")
    void structurallyEqualForeignContextCannotReuseTrustedScope() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        EngineContext foreign = new EngineContext(f.context.compiledModelSet());

        assertEquals(f.context.compiledModelSet(), foreign.compiledModelSet());
        assertFalse(f.context == foreign);

        ProtectedAccessCompositionResult result =
                ProtectedAccessRuntimeFactory.production(foreign).create(f.scope);
        assertFalse(result.created(),
                "distinct EngineContext identity must reject even when plan/policy/digest are equal");
        assertEquals(
                ProtectedAccessCompositionFailureCode.PROVENANCE_MISMATCH,
                result.failure().get().code());
        assertEquals(0, f.effect.executeCount());
    }
}
