package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Freezes the public API surface before the compiler implementation exists.
 */
class CompilerApiContractTest {
    private static final List<String> REQUIRED_TYPES = Arrays.asList(
            "dec.core.compiler.api.CompilationRequest",
            "dec.core.compiler.api.CompilationOptions",
            "dec.core.compiler.api.CompilationResult",
            "dec.core.compiler.api.PublicationRequest",
            "dec.core.compiler.api.CancellationToken",
            "dec.core.compiler.api.ContextPublisher",
            "dec.core.compiler.api.PublicationResult");

    @Test
    void exposesAllFrozenPublicApiTypes() {
        assertAll(REQUIRED_TYPES.stream()
                .map(typeName -> () -> assertDoesNotThrow(
                        () -> Class.forName(typeName),
                        "Missing frozen API type: " + typeName)));
    }

    @Test
    void exposesCompileAndPublishAsTheOnlyPublicEntry() {
        Method[] methods = ModelCompiler.class.getDeclaredMethods();
        assertEquals(1, methods.length, "ModelCompiler must expose exactly one declared method");

        Method entry = methods[0];
        assertEquals("compileAndPublish", entry.getName());
        assertTrue(Modifier.isPublic(entry.getModifiers()));
        assertEquals("dec.core.compiler.api.CompilationResult", entry.getReturnType().getName());
        assertEquals(
                Arrays.asList(
                        "dec.core.compiler.api.CompilationRequest",
                        "dec.core.compiler.api.PublicationRequest"),
                Arrays.asList(
                        entry.getParameterTypes()[0].getName(),
                        entry.getParameterTypes()[1].getName()));
    }
}
