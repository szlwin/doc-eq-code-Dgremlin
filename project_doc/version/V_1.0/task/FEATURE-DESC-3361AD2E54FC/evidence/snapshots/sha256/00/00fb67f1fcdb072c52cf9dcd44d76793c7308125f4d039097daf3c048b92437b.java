package dec.core.starter.access;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import dec.core.context.runtime.ProtectedWriteReceipt;
import dec.core.context.runtime.ResolvedProtectedReadAccess;
import dec.core.context.runtime.ResolvedProtectedWriteAccess;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.context.runtime.RuntimeMutationStamp;
import dec.core.context.runtime.RuntimeMutationVersion;
import dec.core.model.runtime.RuntimeModelAccessScope;
import dec.core.model.runtime.RuntimeModelEffectBindingResult;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TESTDESIGN-P2-R37 genuine RED harness for P2-CR-001.
 *
 * <p>These tests intentionally assert the frozen secure oracle against the current pre-fix
 * production implementation. They must compile and be discovered; current failures must be
 * semantic authority failures, never fixture/compilation failures.
 */
class P2SecurityAuthorityRemediationTest {

    @Test
    @DisplayName("CASE-P2-TD-R34-RAW-MODEL-PORT-PUBLIC-SEAM-001")
    void ordinaryCallerCannotObtainRawModelOperationPort() {
        boolean publicEffectProvider = Arrays.stream(RuntimeModelAccessScope.class.getMethods())
                .filter(method -> method.getName().equals("effectProvider"))
                .anyMatch(method -> Modifier.isPublic(method.getModifiers()));
        boolean publicOperationPort = Arrays.stream(RuntimeModelEffectBindingResult.class.getMethods())
                .filter(method -> method.getName().equals("operationPort"))
                .anyMatch(method -> Modifier.isPublic(method.getModifiers()));

        assertFalse(
                publicEffectProvider && publicOperationPort,
                "ordinary external callers must not have a public Scope -> EffectProvider -> raw operationPort seam");
    }

    @Test
    @DisplayName("CASE-P2-TD-R34-READONLY-RAW-WRITE-BYPASS-001")
    void readOnlyPolicyCannotWriteThroughRawModelPort() throws Exception {
        try (P2SecurityAuthorityRemediationFixture fixture = new P2SecurityAuthorityRemediationFixture(false)) {
            assertFalse(
                    fixture.context.modelAccessPolicyIndex().find(fixture.writeKey).isPresent(),
                    "fixture must contain READ policy only");
            RuntimeMutationVersion beforeVersion = fixture.session.currentVersion(fixture.target, fixture.path);
            Long beforeValue = (Long) fixture.data.getValue("amount");
            int beforeEffects = fixture.effect.executeCount;
            RuntimeMutationStamp stamp = RuntimeMutationStamp.of(
                    fixture.session.sessionId(),
                    fixture.target.runtimeObjectId(),
                    fixture.path,
                    beforeVersion);

            ProtectedWriteReceipt receipt = fixture.operationPort.write(ResolvedProtectedWriteAccess.of(
                    fixture.target,
                    fixture.path,
                    RuntimeFactValue.integerValue(77L),
                    stamp));

            assertAll(
                    "READ-only raw WRITE must be denied before effect",
                    () -> assertNull(receipt, "raw WRITE must not return a receipt without Guard WRITE authorization"),
                    () -> assertEquals(beforeEffects, fixture.effect.executeCount, "effectCount must remain unchanged"),
                    () -> assertEquals(beforeValue, fixture.data.getValue("amount"), "model value must remain unchanged"),
                    () -> assertEquals(beforeVersion, fixture.session.currentVersion(fixture.target, fixture.path),
                            "mutation version must remain unchanged"));
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-R34-PROOFLESS-READ-ACCESS-NOT-AUTHORITY-001")
    void prooflessReadAccessCannotAuthorizeRawRead() throws Exception {
        try (P2SecurityAuthorityRemediationFixture fixture = new P2SecurityAuthorityRemediationFixture()) {
            RuntimeFactValue rawRead = fixture.operationPort.read(
                    ResolvedProtectedReadAccess.of(fixture.target, fixture.path));

            assertNull(
                    rawRead,
                    "proofless ResolvedProtectedReadAccess transport object must not authorize a raw READ effect");
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-R34-PROOFLESS-WRITE-ACCESS-NOT-AUTHORITY-001")
    void prooflessWriteAccessCannotAuthorizeRawWrite() throws Exception {
        try (P2SecurityAuthorityRemediationFixture fixture = new P2SecurityAuthorityRemediationFixture()) {
            RuntimeMutationVersion beforeVersion = fixture.session.currentVersion(fixture.target, fixture.path);
            Long beforeValue = (Long) fixture.data.getValue("amount");
            int beforeEffects = fixture.effect.executeCount;
            RuntimeMutationStamp stamp = RuntimeMutationStamp.of(
                    fixture.session.sessionId(),
                    fixture.target.runtimeObjectId(),
                    fixture.path,
                    beforeVersion);

            ProtectedWriteReceipt receipt = fixture.operationPort.write(ResolvedProtectedWriteAccess.of(
                    fixture.target,
                    fixture.path,
                    RuntimeFactValue.integerValue(88L),
                    stamp));

            assertAll(
                    "proofless raw WRITE must be denied before effect",
                    () -> assertNull(receipt, "proofless transport object must not return a write receipt"),
                    () -> assertEquals(beforeEffects, fixture.effect.executeCount, "effectCount must remain unchanged"),
                    () -> assertEquals(beforeValue, fixture.data.getValue("amount"), "model value must remain unchanged"),
                    () -> assertEquals(beforeVersion, fixture.session.currentVersion(fixture.target, fixture.path),
                            "mutation version must remain unchanged"));
        }
    }
}
