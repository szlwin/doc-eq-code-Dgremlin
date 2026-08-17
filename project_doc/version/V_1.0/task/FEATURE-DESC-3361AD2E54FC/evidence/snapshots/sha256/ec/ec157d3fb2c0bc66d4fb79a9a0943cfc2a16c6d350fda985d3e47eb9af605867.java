package dec.core.starter.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.runtime.DenialCode;
import dec.core.context.runtime.ProtectedAccessResult;
import dec.core.context.runtime.ResolvedWriteIntent;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.context.runtime.RuntimeMutationStamp;
import dec.core.context.runtime.RuntimeWriteIntentId;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 executable DEV-08 concurrency oracle. */
class ProtectedAccessConcurrencyTest {

    @Test
    @DisplayName("CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001")
    void sameCapabilityCanBeConsumedByOnlyOneConcurrentCaller() throws Exception {
        try (ProtectedAccessRuntimeTestFixture f = new ProtectedAccessRuntimeTestFixture()) {
            ResolvedWriteIntent intent = ResolvedWriteIntent.of(
                    RuntimeWriteIntentId.of("concurrent-capability"),
                    f.writeKey,
                    Optional.empty(),
                    f.target,
                    RuntimeMutationStamp.of(
                            f.target.sessionId(),
                            f.target.runtimeObjectId(),
                            f.path,
                            f.session.currentVersion(f.target, f.path)),
                    RuntimeFactValue.integerValue(20L));
            OneShotWriteCapability capability = new OneShotWriteCapability(intent);
            ExecutorService pool = Executors.newFixedThreadPool(2);
            CountDownLatch start = new CountDownLatch(1);
            AtomicInteger consumed = new AtomicInteger();
            try {
                Future<?> first = pool.submit(() -> consume(start, capability, consumed));
                Future<?> second = pool.submit(() -> consume(start, capability, consumed));
                start.countDown();
                first.get(5, TimeUnit.SECONDS);
                second.get(5, TimeUnit.SECONDS);
            } finally {
                pool.shutdownNow();
            }
            assertEquals(1, consumed.get());
            assertTrue(capability.consumed());
            assertNull(capability.consume());
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001")
    void overlappingDifferentCapabilitiesProduceAtMostOneEffect() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition composition = f.createComposition();
        f.effect.block();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<ProtectedAccessResult> first = pool.submit(() -> composition.changeEntry().invoke(
                    f.write("concurrent-a", RuntimeFactValue.integerValue(20L))));
            assertTrue(f.effect.awaitEntered());
            Future<ProtectedAccessResult> second = pool.submit(() -> composition.customActionEntry().invoke(
                    f.write("concurrent-b", RuntimeFactValue.integerValue(30L))));
            ProtectedAccessResult competing = second.get(5, TimeUnit.SECONDS);
            assertFalse(competing.allowed());
            assertEquals(DenialCode.CAPABILITY_ALREADY_CONSUMED, competing.denial().get().code());
            assertFalse(competing.writeReceipt().isPresent());
            assertEquals(1, f.effect.executeCount());
            f.effect.release();
            ProtectedAccessResult winner = first.get(5, TimeUnit.SECONDS);
            assertTrue(winner.allowed());
            assertTrue(winner.writeReceipt().isPresent());
            assertEquals(1, f.effect.executeCount());
            assertEquals(Long.valueOf(20L), f.data.getValue("amount"));
        } finally {
            f.effect.release();
            pool.shutdownNow();
            composition.close();
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-CROSS-SESSION-MODELDATA-OWNERSHIP-001")
    void crossSessionOwnershipConflictIsFailClosedAndReleasable() throws Exception {
        ProtectedAccessProductionTestFixture f = new ProtectedAccessProductionTestFixture();
        ProtectedAccessComposition first = f.createComposition();
        ProtectedAccessCompositionResult conflict =
                ProtectedAccessRuntimeFactory.production(f.context).create(f.scope);
        assertFalse(conflict.created());
        assertEquals(ProtectedAccessCompositionFailureCode.SESSION_OWNERSHIP_CONFLICT,
                conflict.failure().get().code());
        assertEquals(0, f.effect.executeCount());
        first.close();
        ProtectedAccessCompositionResult retry =
                ProtectedAccessRuntimeFactory.production(f.context).create(f.scope);
        assertTrue(retry.created());
        retry.composition().get().close();
    }

    private static void consume(
            CountDownLatch start,
            OneShotWriteCapability capability,
            AtomicInteger consumed) {
        try {
            start.await(5, TimeUnit.SECONDS);
            if (capability.consume() != null) {
                consumed.incrementAndGet();
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
