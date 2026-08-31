package dec.core.starter.access;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dec.core.context.runtime.ProtectedWriteReceipt;
import dec.core.context.runtime.ResolvedProtectedReadAccess;
import dec.core.context.runtime.ResolvedProtectedWriteAccess;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.context.runtime.RuntimeMutationStamp;
import dec.core.context.runtime.RuntimeMutationVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R37 controlled RED-to-GREEN adaptation for P2-CR-001. */
class P2SecurityAuthorityRemediationTest {
    @Test
    @DisplayName("CASE-P2-TD-R34-RAW-MODEL-PORT-PUBLIC-SEAM-001")
    void ordinaryCallerCannotObtainRawModelOperationPort() throws Exception {
        try (P2SecurityAuthorityGreenFixture fixture = new P2SecurityAuthorityGreenFixture()) {
            assertFalse(fixture.binding.operationPort().isPresent(),
                    "production scope must never expose a usable raw RuntimeModelOperationPort");
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-R34-READONLY-RAW-WRITE-BYPASS-001")
    void readOnlyPolicyCannotWriteThroughRawModelPort() throws Exception {
        try (P2SecurityAuthorityGreenFixture fixture = new P2SecurityAuthorityGreenFixture(false)) {
            assertFalse(fixture.context.modelAccessPolicyIndex().find(fixture.writeKey).isPresent(),
                    "fixture must contain READ policy only");
            RuntimeMutationVersion beforeVersion = fixture.session.currentVersion(fixture.target, fixture.path);
            Long beforeValue = (Long) fixture.data.getValue("amount");
            int beforeEffects = fixture.effect.executeCount;
            RuntimeMutationStamp stamp = RuntimeMutationStamp.of(
                    fixture.session.sessionId(), fixture.target.runtimeObjectId(), fixture.path, beforeVersion);
            ResolvedProtectedWriteAccess proofless = ResolvedProtectedWriteAccess.of(
                    fixture.target, fixture.path, RuntimeFactValue.integerValue(77L), stamp);
            assertNotNull(proofless, "proofless transport may exist but must not itself grant authority");
            assertFalse(fixture.binding.operationPort().isPresent(), "production binding must hide raw WRITE port");
            ProtectedWriteReceipt receipt = fixture.guardedEffectPort.write(null);
            assertAll("READ-only WRITE must be denied before effect without Guard authorization",
                    () -> assertNull(receipt, "missing Guard WRITE authorization must not return a receipt"),
                    () -> assertEquals(beforeEffects, fixture.effect.executeCount, "effectCount must remain unchanged"),
                    () -> assertEquals(beforeValue, fixture.data.getValue("amount"), "model value must remain unchanged"),
                    () -> assertEquals(beforeVersion, fixture.session.currentVersion(fixture.target, fixture.path),
                            "mutation version must remain unchanged"));
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-R34-PROOFLESS-READ-ACCESS-NOT-AUTHORITY-001")
    void prooflessReadAccessCannotAuthorizeRawRead() throws Exception {
        try (P2SecurityAuthorityGreenFixture fixture = new P2SecurityAuthorityGreenFixture()) {
            ResolvedProtectedReadAccess proofless = ResolvedProtectedReadAccess.of(fixture.target, fixture.path);
            int beforeEffects = fixture.effect.executeCount;
            Long beforeValue = (Long) fixture.data.getValue("amount");
            assertNotNull(proofless, "proofless READ transport may exist but must not itself grant authority");
            assertFalse(fixture.binding.operationPort().isPresent(), "production binding must hide raw READ port");
            RuntimeFactValue rawRead = fixture.guardedEffectPort.read(null);
            assertAll("proofless READ cannot reach MODEL effect without Guard authorization",
                    () -> assertNull(rawRead, "missing Guard READ authorization must return no value"),
                    () -> assertEquals(beforeEffects, fixture.effect.executeCount, "effectCount must remain unchanged"),
                    () -> assertEquals(beforeValue, fixture.data.getValue("amount"), "model value must remain unchanged"));
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-R34-PROOFLESS-WRITE-ACCESS-NOT-AUTHORITY-001")
    void prooflessWriteAccessCannotAuthorizeRawWrite() throws Exception {
        try (P2SecurityAuthorityGreenFixture fixture = new P2SecurityAuthorityGreenFixture()) {
            RuntimeMutationVersion beforeVersion = fixture.session.currentVersion(fixture.target, fixture.path);
            Long beforeValue = (Long) fixture.data.getValue("amount");
            int beforeEffects = fixture.effect.executeCount;
            RuntimeMutationStamp stamp = RuntimeMutationStamp.of(
                    fixture.session.sessionId(), fixture.target.runtimeObjectId(), fixture.path, beforeVersion);
            ResolvedProtectedWriteAccess proofless = ResolvedProtectedWriteAccess.of(
                    fixture.target, fixture.path, RuntimeFactValue.integerValue(88L), stamp);
            assertNotNull(proofless, "proofless WRITE transport may exist but must not itself grant authority");
            assertFalse(fixture.binding.operationPort().isPresent(), "production binding must hide raw WRITE port");
            ProtectedWriteReceipt receipt = fixture.guardedEffectPort.write(null);
            assertAll("proofless WRITE cannot reach MODEL effect without Guard authorization",
                    () -> assertNull(receipt, "missing Guard WRITE authorization must not return a receipt"),
                    () -> assertEquals(beforeEffects, fixture.effect.executeCount, "effectCount must remain unchanged"),
                    () -> assertEquals(beforeValue, fixture.data.getValue("amount"), "model value must remain unchanged"),
                    () -> assertEquals(beforeVersion, fixture.session.currentVersion(fixture.target, fixture.path),
                            "mutation version must remain unchanged"));
        }
    }
}
