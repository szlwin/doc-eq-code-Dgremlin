package dec.core.starter.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.AccessOperation;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.SystemKey;
import dec.core.context.model.TargetKey;
import dec.core.context.model.ViewKey;
import dec.core.context.runtime.DenialCode;
import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.ProtectedAccessResult;
import dec.core.context.runtime.ProtectedInvocationId;
import dec.core.context.runtime.RuntimeCollectionCursorId;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.model.runtime.RuntimeModelEffectProvider;
import dec.core.model.runtime.RuntimeModelOperationPort;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 executable DEV-08 production composition oracle. */
class ProtectedAccessProductionCompositionTest {

    @Test
    @DisplayName("CASE-P2-TD-MODEL-EFFECT-PROVIDER-BINDING-001")
    void sameScopeProviderBindingCreatesUsableComposition() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessCompositionResult result = ProtectedAccessRuntimeFactory.production(f.context).create(f.scope);
        assertTrue(result.created());
        ProtectedAccessComposition composition = result.composition().get();
        assertTrue(composition.protectedAccessPort().invoke(f.read("provider-read")).allowed());
        composition.close();
    }

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-TARGET-SUBSTITUTION-001")
    void targetSubstitutionIsDeniedBeforeEffect() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition composition = f.createComposition();
        TargetKey foreignTarget = TargetKey.of(new ViewKey("ForeignInfo"));
        ModelAccessRuleKey foreign = ModelAccessRuleKey.of(
                new SystemKey("sales"), foreignTarget, f.path, AccessOperation.WRITE);
        ProtectedAccessInvocation invocation = ProtectedAccessInvocation.write(
                ProtectedInvocationId.of("substitution"),
                foreign,
                f.scope.frame().frameId(),
                f.scope.frame().ownerResolutionId(),
                Optional.<RuntimeCollectionCursorId>empty(),
                RuntimeFactValue.integerValue(99L));
        ProtectedAccessResult denied = composition.protectedAccessPort().invoke(invocation);
        assertFalse(denied.allowed());
        assertEquals(DenialCode.POLICY_NOT_FOUND, denied.denial().get().code());
        assertEquals(Long.valueOf(10L), f.data.getValue("amount"));
        assertEquals(0, f.effect.executeCount());
        composition.close();
    }

    @Test
    @DisplayName("CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001")
    void publicProductionSeamHasNoGuardEffectOrOperationInjection() {
        for (Constructor<?> constructor : ProtectedAccessRuntimeFactory.class.getConstructors()) {
            assertNoForbidden(constructor.getParameterTypes());
        }
        for (Method method : ProtectedAccessRuntimeFactory.class.getMethods()) {
            if (Modifier.isPublic(method.getModifiers())) assertNoForbidden(method.getParameterTypes());
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001")
    void productionCompositionPublishesAllRequiredEntries() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition composition = f.createComposition();
        assertNotNull(composition.protectedAccessPort());
        assertNotNull(composition.ruleEntry());
        assertNotNull(composition.changeEntry());
        assertNotNull(composition.customActionEntry());
        assertNotNull(composition.runtimeModelSessionId());
        composition.close();
    }

    @Test
    @DisplayName("CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001")
    void ruleEntryDelegatesToTheGuardedPort() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition composition = f.createComposition();
        ProtectedAccessResult result = composition.ruleEntry().invoke(f.read("rule-read"));
        assertTrue(result.allowed());
        assertEquals(RuntimeFactValue.integerValue(10L), result.readValue().get().value());
        composition.close();
    }

    @Test
    @DisplayName("CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001")
    void changeEntryDelegatesToTheGuardedPort() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition composition = f.createComposition();
        ProtectedAccessResult result = composition.changeEntry().invoke(
                f.write("change-write", RuntimeFactValue.integerValue(20L)));
        assertTrue(result.allowed());
        assertEquals(Long.valueOf(20L), f.data.getValue("amount"));
        composition.close();
    }

    @Test
    @DisplayName("CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001")
    void customActionEntryDelegatesToTheGuardedPort() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition composition = f.createComposition();
        ProtectedAccessResult result = composition.customActionEntry().invoke(f.read("action-read"));
        assertTrue(result.allowed());
        assertEquals(RuntimeFactValue.integerValue(10L), result.readValue().get().value());
        composition.close();
    }

    @Test
    @DisplayName("CASE-P2-TD-AC007-CONSUMER-PARITY-001")
    void allConsumerEntriesAreTheSameAdapterInstance() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition composition = f.createComposition();
        assertSame(composition.ruleEntry(), composition.changeEntry());
        assertSame(composition.ruleEntry(), composition.customActionEntry());
        composition.close();
    }

    @Test
    @DisplayName("CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001")
    void consumersExposeOnlyNeutralInvocationAndResult() throws Exception {
        assertEquals(1, RuleProtectedAccessEntry.class.getMethods().length);
        Method rule = RuleProtectedAccessEntry.class.getMethod("invoke", ProtectedAccessInvocation.class);
        assertEquals(dec.core.context.runtime.ProtectedAccessResult.class, rule.getReturnType());
        Method change = ChangeProtectedAccessEntry.class.getMethod("invoke", ProtectedAccessInvocation.class);
        Method action = CustomActionProtectedAccessEntry.class.getMethod("invoke", ProtectedAccessInvocation.class);
        assertEquals(rule.getReturnType(), change.getReturnType());
        assertEquals(rule.getReturnType(), action.getReturnType());
    }

    @Test
    @DisplayName("CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001")
    void factoryReachesRealScopeBoundModelEffect() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition composition = f.createComposition();
        ProtectedAccessResult write = composition.protectedAccessPort().invoke(
                f.write("reachable-write", RuntimeFactValue.integerValue(31L)));
        assertTrue(write.allowed());
        assertTrue(write.writeReceipt().isPresent());
        assertEquals(1, f.effect.executeCount());
        composition.close();
    }

    @Test
    @DisplayName("CASE-P2-TD-COMPOSITION-RUNTIME-CONTEXT-MATCH-001")
    void capturedContextMustContainExactHandlePlan() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessCompositionResult result =
                ProtectedAccessRuntimeFactory.production(f.foreignContext()).create(f.scope);
        assertFalse(result.created());
        assertEquals(ProtectedAccessCompositionFailureCode.PROVENANCE_MISMATCH,
                result.failure().get().code());
    }

    @Test
    @DisplayName("CASE-P2-TD-PRODUCTION-RUNTIME-REGISTRATION-BINDING-001")
    void factoryRegistersAndSealsTrustedHandleBeforePublishing() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition composition = f.createComposition();
        assertNotNull(composition.runtimeModelSessionId());
        assertTrue(composition.ruleEntry().invoke(f.read("registered-read")).allowed());
        composition.close();
    }

    @Test
    @DisplayName("CASE-P2-TD-PRODUCTION-FRAME-HANDOFF-001")
    void invocationMustUseExactTrustedFrameFacts() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition composition = f.createComposition();
        ProtectedAccessInvocation wrongOwner = ProtectedAccessInvocation.of(
                ProtectedInvocationId.of("wrong-owner"),
                f.readKey,
                f.scope.frame().frameId(),
                dec.core.context.runtime.RuntimeResolutionOwnerId.of("other-owner"),
                Optional.<RuntimeCollectionCursorId>empty());
        ProtectedAccessResult denied = composition.ruleEntry().invoke(wrongOwner);
        assertFalse(denied.allowed());
        assertEquals(DenialCode.RUNTIME_CONTEXT_MISMATCH, denied.denial().get().code());
        composition.close();
    }

    @Test
    @DisplayName("CASE-P2-TD-PRODUCTION-SESSION-HANDOFF-001")
    void sameHandleCannotBeOwnedByTwoOpenCompositions() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition first = f.createComposition();
        ProtectedAccessCompositionResult second =
                ProtectedAccessRuntimeFactory.production(f.context).create(f.scope);
        assertFalse(second.created());
        assertEquals(ProtectedAccessCompositionFailureCode.SESSION_OWNERSHIP_CONFLICT,
                second.failure().get().code());
        first.close();
        ProtectedAccessCompositionResult afterClose =
                ProtectedAccessRuntimeFactory.production(f.context).create(f.scope);
        assertTrue(afterClose.created());
        afterClose.composition().get().close();
    }

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-SCOPE-PROVENANCE-001")
    void foreignContextCannotReuseTrustedScope() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessCompositionResult result =
                ProtectedAccessRuntimeFactory.production(f.foreignContext()).create(f.scope);
        assertFalse(result.created());
        assertEquals(ProtectedAccessCompositionFailureCode.PROVENANCE_MISMATCH,
                result.failure().get().code());
        assertEquals(0, f.effect.executeCount());
    }

    @Test
    @DisplayName("CASE-P2-TD-COMPOSITION-FAILURE-ALGEBRA-001")
    void ownershipFailureIsStableAndCarriesNoComposition() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition first = f.createComposition();
        ProtectedAccessCompositionResult conflict =
                ProtectedAccessRuntimeFactory.production(f.context).create(f.scope);
        assertFalse(conflict.created());
        assertFalse(conflict.composition().isPresent());
        assertTrue(conflict.failure().isPresent());
        assertEquals(ProtectedAccessCompositionFailureCode.SESSION_OWNERSHIP_CONFLICT,
                conflict.failure().get().code());
        first.close();
    }

    @Test
    void closedCompositionFailsClosedWithoutEffect() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition composition = f.createComposition();
        composition.close();
        composition.close();
        ProtectedAccessResult denied = composition.changeEntry().invoke(
                f.write("closed-write", RuntimeFactValue.integerValue(44L)));
        assertFalse(denied.allowed());
        assertEquals(0, f.effect.executeCount());
        assertEquals(Long.valueOf(10L), f.data.getValue("amount"));
    }

    private static void assertNoForbidden(Class<?>[] parameters) {
        for (Class<?> parameter : parameters) {
            assertFalse(RuntimeModelOperationPort.class.isAssignableFrom(parameter));
            assertFalse(RuntimeModelEffectProvider.class.isAssignableFrom(parameter));
            assertFalse(ExactModelAccessGuard.class.isAssignableFrom(parameter));
        }
    }
}
