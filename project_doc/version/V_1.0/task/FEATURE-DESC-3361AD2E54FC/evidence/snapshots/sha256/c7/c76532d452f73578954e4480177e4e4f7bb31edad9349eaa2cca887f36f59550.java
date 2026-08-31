package dec.core.starter.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.CompiledModelAccessRule;
import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.ProtectedInvocationId;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.model.runtime.ProductionContainerKind;
import dec.core.model.runtime.RuntimeModelExecutionRoot;
import dec.core.model.runtime.RuntimeModelExecutionRoots;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R37 single-runtime EngineContext lifecycle verification. Exact task7 harness. */
class SingleEngineContextRuntimeLifecycleTest {

    @Test
    @DisplayName("CASE-P2-TD-R37-SINGLE-RUNTIME-CONTEXT-BIND-ONCE-001")
    void productionGenerationCapturesExactlyOneContextAndKeepsGuardedReadFunctional()
            throws Exception {
        try (P2SecurityAuthorityGreenFixture fixture = new P2SecurityAuthorityGreenFixture()) {
            ProtectedAccessRuntimeFactory factory = ProtectedAccessRuntimeFactory.production(fixture.context);
            assertSame(fixture.context, factory.capturedContext(),
                    "STARTER production factory must retain the bootstrap EngineContext identity");
            assertFinalContextField(ProtectedAccessRuntimeFactory.class, factory, fixture.context);

            RuntimeModelExecutionRoot root = RuntimeModelExecutionRoots.production(
                    fixture.context,
                    ProductionContainerKind.SYNCHRONIZED);
            try {
                assertFinalContextField(root.getClass(), root, fixture.context);

                ExactModelAccessGuard guard = new ExactModelAccessGuard(fixture.context);
                ProtectedAccessInvocation invocation = ProtectedAccessInvocation.of(
                        ProtectedInvocationId.of("r37-bind-once-read"),
                        fixture.readKey,
                        fixture.frameId,
                        fixture.ownerId,
                        Optional.empty());
                CompiledModelAccessRule rule = guard.exactRule(invocation).orElseThrow(
                        () -> new AssertionError("captured EngineContext must expose the exact READ rule"));
                ModelEffectAuthorization authorization = guard.authorizeRead(
                        rule,
                        invocation,
                        fixture.target);
                assertNotNull(authorization,
                        "the captured bootstrap Context must mint the matching Guard READ authorization");
                assertEquals(RuntimeFactValue.integerValue(10L), fixture.guardedEffectPort.read(authorization),
                        "normal guarded READ must remain functional for the bound runtime generation");
            } finally {
                root.close();
            }
        }
    }

    @Test
    @DisplayName("CASE-P2-TD-R37-NO-HOT-RELOAD-001")
    void activeRuntimeSurfaceExposesNoContextMutationOrHotReloadApi() {
        assertNoContextMutationSurface(ProtectedAccessRuntimeFactory.class);
        assertNoContextMutationSurface(RuntimeModelExecutionRoot.class);
        assertNoContextMutationSurface(RuntimeModelExecutionRoots.class);

        Field factoryContext = declaredField(ProtectedAccessRuntimeFactory.class, "context");
        assertTrue(Modifier.isFinal(factoryContext.getModifiers()),
                "STARTER runtime Context field must be final");
    }

    @Test
    @DisplayName("CASE-P2-TD-R37-RESTART-NEW-GENERATION-001")
    void newContextIsAcceptedOnlyByASeparateRuntimeGenerationAfterOldRootCloses()
            throws Exception {
        P2SecurityAuthorityGreenFixture generationA = new P2SecurityAuthorityGreenFixture();
        RuntimeModelExecutionRoot rootA = RuntimeModelExecutionRoots.production(
                generationA.context,
                ProductionContainerKind.SYNCHRONIZED);
        ProtectedAccessRuntimeFactory factoryA = ProtectedAccessRuntimeFactory.production(generationA.context);
        assertSame(generationA.context, factoryA.capturedContext());

        rootA.close();
        generationA.close();
        assertFalse(rootA.accessScope().available(),
                "closed generation A must not continue exposing active runtime artifacts");

        try (P2SecurityAuthorityGreenFixture generationB = new P2SecurityAuthorityGreenFixture()) {
            RuntimeModelExecutionRoot rootB = RuntimeModelExecutionRoots.production(
                    generationB.context,
                    ProductionContainerKind.SYNCHRONIZED);
            try {
                ProtectedAccessRuntimeFactory factoryB = ProtectedAccessRuntimeFactory.production(generationB.context);
                assertNotSame(generationA.context, generationB.context,
                        "restart must construct a distinct EngineContext generation");
                assertSame(generationB.context, factoryB.capturedContext(),
                        "generation B must bind its own bootstrap Context");
                assertFinalContextField(rootB.getClass(), rootB, generationB.context);
            } finally {
                rootB.close();
            }
        }
    }

    private static void assertNoContextMutationSurface(Class<?> type) {
        for (Method method : type.getMethods()) {
            String normalized = method.getName().toLowerCase(Locale.ROOT);
            boolean forbidden = normalized.equals("setcontext")
                    || normalized.equals("replacecontext")
                    || normalized.equals("reloadcontext")
                    || normalized.equals("publishintoruntime")
                    || normalized.equals("rebindcontext")
                    || normalized.equals("swapcontext")
                    || normalized.equals("updatecontext");
            assertFalse(forbidden,
                    () -> "active runtime must not expose Context mutation/hot-reload API: "
                            + type.getName() + "#" + method.getName());
        }
    }

    private static void assertFinalContextField(
            Class<?> owner,
            Object instance,
            Object expectedContext) throws Exception {
        Field field = declaredField(owner, "context");
        field.setAccessible(true);
        assertTrue(Modifier.isFinal(field.getModifiers()),
                "runtime Context field must be immutable/final: " + owner.getName());
        assertSame(expectedContext, field.get(instance),
                "runtime generation must retain exactly the bootstrap Context identity");
    }

    private static Field declaredField(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "expected captured Context field on " + type.getName()));
    }
}
