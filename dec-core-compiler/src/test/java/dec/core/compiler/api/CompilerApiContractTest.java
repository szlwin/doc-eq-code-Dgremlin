package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.EngineContext;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.SourceRef;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Freezes the T02 public API and immutable-value behavior before implementation.
 */
class CompilerApiContractTest {
    private static final List<String> REQUIRED_TYPES = Arrays.asList(
            "dec.core.compiler.api.CompilationRequest",
            "dec.core.compiler.api.CompilationOptions",
            "dec.core.compiler.api.CompilationResult",
            "dec.core.compiler.api.CompilationStatus",
            "dec.core.compiler.api.PublishedCompilationResult",
            "dec.core.compiler.api.FailedCompilationResult",
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
        assertEquals(CompilationResult.class, entry.getReturnType());
        assertEquals(
                Arrays.asList(CompilationRequest.class, PublicationRequest.class),
                Arrays.asList(entry.getParameterTypes()));
    }

    @Test
    void keepsPublicValueObjectsFinalWithPrivateFinalState() {
        List<Class<?>> valueTypes = Arrays.asList(
                CompilationOptions.class,
                CompilationRequest.class,
                PublicationRequest.class,
                PublishedCompilationResult.class,
                FailedCompilationResult.class);

        for (Class<?> valueType : valueTypes) {
            assertTrue(Modifier.isFinal(valueType.getModifiers()), valueType.getName());
            assertPrivateFinalSourceFields(valueType);
        }
        assertPrivateFinalSourceFields(CompilationResult.class);
    }

    @Test
    void requestAndOptionsPreserveSessionBoundaries() {
        CancellationToken token = () -> false;
        CompilationOptions options = new CompilationOptions("mix-1", "strict-1", 123L);
        CompilationRequest request = new CompilationRequest("classpath:mix/orm-config.xml", options, token);

        assertEquals("mix-1", options.schemaVersion());
        assertEquals("strict-1", options.optionsVersion());
        assertEquals(123L, options.deadlineNanos());
        assertEquals("classpath:mix/orm-config.xml", request.rootSourceId());
        assertSame(options, request.options());
        assertSame(token, request.cancellationToken());

        assertThrows(IllegalArgumentException.class,
                () -> new CompilationOptions(" ", "strict-1", 1L));
        assertThrows(IllegalArgumentException.class,
                () -> new CompilationOptions("mix-1", "strict-1", -1L));
        assertThrows(NullPointerException.class,
                () -> new CompilationRequest("root", null, token));
    }

    @Test
    void publicationRequestAllowsInitialPublicationButRequiresPublisher() {
        ContextPublisher publisher = (expectedCurrent, candidate) -> PublicationResult.PUBLISHED;
        PublicationRequest request = new PublicationRequest(null, publisher);

        assertNull(request.expectedCurrent());
        assertSame(publisher, request.publisher());
        assertThrows(NullPointerException.class, () -> new PublicationRequest(null, null));
    }

    @Test
    void failedResultDefensivelyCopiesDiagnosticsAndExposesNoCandidate() {
        List<Diagnostic> mutableDiagnostics = new ArrayList<Diagnostic>();
        mutableDiagnostics.add(publicationBlockedDiagnostic());

        FailedCompilationResult result = new FailedCompilationResult(
                "session-failed",
                mutableDiagnostics);
        mutableDiagnostics.clear();

        assertEquals("session-failed", result.sessionId());
        assertEquals(CompilationStatus.FAILED, result.status());
        assertFalse(result.isPublished());
        assertEquals(1, result.diagnostics().size());
        assertThrows(UnsupportedOperationException.class,
                () -> result.diagnostics().add(publicationBlockedDiagnostic()));
        assertThrows(IllegalArgumentException.class,
                () -> new FailedCompilationResult(
                        "session-failed",
                        Collections.<Diagnostic>emptyList()));
        assertFalse(Arrays.stream(FailedCompilationResult.class.getMethods())
                .anyMatch(method -> method.getName().equals("context")
                        || method.getName().equals("compiledModelSet")
                        || method.getName().equals("digests")));
    }

    @Test
    void publishedResultExposesTheCompletePublishedFact() {
        CompiledModelSet modelSet = emptyModelSet();
        EngineContext context = new EngineContext(modelSet);
        PublishedCompilationResult result = new PublishedCompilationResult(
                "session-published",
                modelSet,
                context,
                Collections.<Diagnostic>emptyList());

        assertEquals("session-published", result.sessionId());
        assertEquals(CompilationStatus.PUBLISHED, result.status());
        assertTrue(result.isPublished());
        assertSame(modelSet, result.compiledModelSet());
        assertSame(context, result.context());
        assertEquals(modelSet.digestPair(), result.digests());
        assertEquals("compiler-1", result.compilerVersion());
        assertEquals("schema-1", result.schemaVersion());
        assertEquals("options-1", result.optionsVersion());
    }

    @Test
    void publishedResultRejectsMissingOrMismatchedContext() {
        CompiledModelSet modelSet = emptyModelSet();
        assertThrows(NullPointerException.class,
                () -> new PublishedCompilationResult(
                        "session-published",
                        modelSet,
                        null,
                        Collections.<Diagnostic>emptyList()));
        assertThrows(IllegalArgumentException.class,
                () -> new PublishedCompilationResult(
                        "session-published",
                        modelSet,
                        new EngineContext(emptyModelSetWithSemanticDigest("other")),
                        Collections.<Diagnostic>emptyList()));
    }

    private static void assertPrivateFinalSourceFields(Class<?> valueType) {
        for (Field field : valueType.getDeclaredFields()) {
            // JaCoCo adds synthetic state during CI; only source-declared fields
            // are part of the immutable public contract.
            if (field.isSynthetic()) {
                continue;
            }
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.toString());
            assertTrue(Modifier.isFinal(field.getModifiers()), field.toString());
            assertFalse(Modifier.isStatic(field.getModifiers()), field.toString());
        }
    }

    private static CompiledModelSet emptyModelSet() {
        return emptyModelSetWithSemanticDigest("semantic");
    }

    private static CompiledModelSet emptyModelSetWithSemanticDigest(String semanticDigest) {
        Map<DefinitionKey, CompiledDefinition> definitions =
                Collections.<DefinitionKey, CompiledDefinition>emptyMap();
        Map<DeferredKey, DeferredDefinition> deferred =
                Collections.<DeferredKey, DeferredDefinition>emptyMap();
        return new CompiledModelSet(
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(definitions),
                new ImmutableDeferredRegistry(deferred),
                Collections.<Diagnostic>emptyList(),
                new DigestPair("source", semanticDigest),
                "compiler-1",
                "schema-1",
                "options-1");
    }

    private static Diagnostic publicationBlockedDiagnostic() {
        return new Diagnostic(
                DiagnosticCode.MIX_PUBLICATION_BLOCKED,
                DiagnosticSeverity.ERROR,
                "publication.blocked",
                null,
                new SourceRef("test:root", 1, 1, "/root"),
                Collections.<SourceRef>emptyList(),
                "Resolve compiler diagnostics before publication",
                "PublicationPass");
    }
}
