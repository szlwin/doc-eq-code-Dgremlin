package dec.core.model.security;

import dec.core.context.model.ModelPath;
import dec.core.context.runtime.ResolvedProtectedReadAccess;
import dec.core.context.runtime.ResolvedProtectedWriteAccess;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.context.runtime.RuntimeMutationStamp;
import dec.core.model.runtime.RuntimeModelEffectBindingResult;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/** R34 external-package architecture oracle: MODEL must expose no Guard-less effect authority. */
class RuntimeModelAuthorityBoundaryTest {

    @Test
    @DisplayName("CASE-P2-TD-R34-RAW-MODEL-PORT-UNREACHABLE-001")
    void rawOperationPortAndProoflessFactoriesAreNotPublicProductionSeams() throws Exception {
        Class<?> rawPort = Class.forName("dec.core.model.runtime.RuntimeModelOperationPort");
        assertFalse(Modifier.isPublic(rawPort.getModifiers()),
                "RuntimeModelOperationPort must be MODEL-internal, not public authority");

        assertFalse(Arrays.stream(RuntimeModelEffectBindingResult.class.getMethods())
                        .anyMatch(method -> method.getName().equals("operationPort")),
                "public binding result must not return a raw operation port");

        assertFalse(hasPublicFactory(
                        ResolvedProtectedReadAccess.class,
                        ResolvedRuntimeTarget.class,
                        ModelPath.class),
                "proofless READ factory must not be public");
        assertFalse(hasPublicFactory(
                        ResolvedProtectedWriteAccess.class,
                        ResolvedRuntimeTarget.class,
                        ModelPath.class,
                        RuntimeFactValue.class,
                        RuntimeMutationStamp.class),
                "proofless WRITE factory must not be public");
    }

    private static boolean hasPublicFactory(Class<?> type, Class<?>... parameters) {
        for (Method method : type.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())
                    && Modifier.isPublic(method.getModifiers())
                    && method.getName().equals("of")
                    && Arrays.equals(method.getParameterTypes(), parameters)) {
                return true;
            }
        }
        return false;
    }
}
