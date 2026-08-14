package dec.core.starter.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.RuleKey;
import dec.core.context.runtime.DenialCode;
import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.ResolvedWriteIntent;
import dec.core.context.runtime.RuntimeCollectionCursorId;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.context.runtime.RuntimeMutationStamp;
import dec.core.context.runtime.RuntimeTargetResolution;
import dec.core.context.runtime.RuntimeTargetResolutionStatus;
import dec.core.context.runtime.RuntimeWriteIntentId;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32/R33 exact DEV-07 resolver, intent and one-shot-capability oracle. */
class ProtectedWriteIntentResolutionTest {

    @Test
    @DisplayName("CASE-P2-TD-SESSION-PROOF-BOUND-RESOLUTION-001")
    void exactSessionAndProofResolveOneTarget() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            RuntimeTargetResolution result = new ExactRuntimeTargetResolver(Collections.singletonList(f.target))
                    .resolve(f.plan, f.readInvocation("resolve-one"), f.session);
            assertEquals(RuntimeTargetResolutionStatus.RESOLVED, result.status());
            assertEquals(Optional.of(f.target), result.target());
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-OWNER-CURSOR-SELECTION-001")
    void ownerAndCursorMismatchFailClosed() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            ProtectedAccessInvocation mismatched = ProtectedAccessInvocation.of(
                    dec.core.context.runtime.ProtectedInvocationId.of("wrong-owner"),
                    f.readKey,
                    f.frameId,
                    dec.core.context.runtime.RuntimeResolutionOwnerId.of("other-owner"),
                    Optional.<RuntimeCollectionCursorId>empty());
            RuntimeTargetResolution result = new ExactRuntimeTargetResolver(Collections.singletonList(f.target))
                    .resolve(f.plan, mismatched, f.session);
            assertEquals(RuntimeTargetResolutionStatus.CONTEXT_MISMATCH, result.status());
            assertEquals(Optional.of(DenialCode.RUNTIME_CONTEXT_MISMATCH), result.denialCode());
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-ZERO-CANDIDATE-TERMINAL-DENY-001")
    void zeroCandidateTerminatesAsNotFound() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            RuntimeTargetResolution result = new ExactRuntimeTargetResolver(Collections.emptyList())
                    .resolve(f.plan, f.readInvocation("zero"), f.session);
            assertEquals(RuntimeTargetResolutionStatus.NOT_FOUND, result.status());
            assertEquals(Optional.of(DenialCode.RUNTIME_TARGET_NOT_FOUND), result.denialCode());
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-MULTI-CANDIDATE-AMBIGUOUS-DENY-001")
    void multipleExactCandidatesTerminateAsAmbiguous() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            RuntimeTargetResolution result = new ExactRuntimeTargetResolver(Arrays.asList(f.target, f.target))
                    .resolve(f.plan, f.readInvocation("multi"), f.session);
            assertEquals(RuntimeTargetResolutionStatus.AMBIGUOUS, result.status());
            assertEquals(Optional.of(DenialCode.RUNTIME_TARGET_AMBIGUOUS), result.denialCode());
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-ONE-SHOT-CAPABILITY-001")
    void frozenCapabilityCanBeConsumedExactlyOnce() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            ResolvedWriteIntent intent = intent(f, RuntimeFactValue.integerValue(20L));
            OneShotWriteCapability capability = new OneShotWriteCapability(intent);
            assertSame(intent, capability.consume());
            assertTrue(capability.consumed());
            assertNull(capability.consume());
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-RULEKEY-CANNOT-AUTHORIZE-WRITE-001")
    void ruleKeyIsNeverAcceptedByGuardOrInvocationAuthoritySurface() {
        for (Method method : ExactModelAccessGuard.class.getDeclaredMethods()) {
            for (Class<?> parameterType : method.getParameterTypes()) {
                assertFalse(RuleKey.class.isAssignableFrom(parameterType));
            }
        }
        for (Method method : ProtectedAccessInvocation.class.getDeclaredMethods()) {
            if ("of".equals(method.getName()) || "write".equals(method.getName())) {
                for (Class<?> parameterType : method.getParameterTypes()) {
                    assertFalse(RuleKey.class.isAssignableFrom(parameterType));
                }
            }
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-RULEVIEW-CALLER-EXACT-INPUTS-ONLY-001")
    void callerFactsRemainExactAndUnmodified() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            ProtectedAccessInvocation invocation = f.writeInvocation(
                    "exact-input", RuntimeFactValue.integerValue(20L));
            assertEquals(f.writeKey, invocation.modelAccessRuleKey());
            assertEquals(f.frameId, invocation.frameId());
            assertEquals(f.ownerId, invocation.ownerResolutionId());
            assertEquals(Optional.empty(), invocation.cursorId());
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-WRITE-VALUE-FREEZE-001")
    void targetPathVersionAndValueFreezeTogether() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            RuntimeFactValue valueA = RuntimeFactValue.stringValue("A");
            RuntimeFactValue valueB = RuntimeFactValue.stringValue("B");
            ResolvedWriteIntent frozen = intent(f, valueA);
            assertEquals(f.target, frozen.resolvedRuntimeTarget());
            assertEquals(f.path, frozen.modelAccessRuleKey().path());
            assertEquals(f.session.currentVersion(f.target, f.path), frozen.mutationStamp().version());
            assertEquals(Optional.of(valueA), frozen.writeValue());
            assertFalse(frozen.writeValue().equals(Optional.of(valueB)));
            OneShotWriteCapability capability = new OneShotWriteCapability(frozen);
            assertSame(frozen, capability.consume());
            assertNull(capability.consume());
        }
    }

    private static ResolvedWriteIntent intent(
            ProtectedAccessRuntimeTestFixture f,
            RuntimeFactValue value) {
        RuntimeMutationStamp stamp = RuntimeMutationStamp.of(
                f.target.sessionId(),
                f.target.runtimeObjectId(),
                f.path,
                f.session.currentVersion(f.target, f.path));
        return ResolvedWriteIntent.of(
                RuntimeWriteIntentId.of("test-write-intent"),
                f.writeKey,
                Optional.empty(),
                f.target,
                stamp,
                value);
    }
}
