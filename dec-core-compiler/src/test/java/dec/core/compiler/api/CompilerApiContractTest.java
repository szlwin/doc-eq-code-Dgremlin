package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendRegistry;
import dec.core.compiler.source.DocumentSourceProvider;
import dec.core.compiler.source.SourceReference;
import dec.core.compiler.source.SourceResolutionContext;
import dec.core.compiler.source.SourceResolutionResult;
import dec.core.context.EngineContext;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 冻结 TASK-P1-T02 I003 的公共 API、不可变性和 Session 注入边界。
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
            "dec.core.compiler.api.PublicationResult",
            "dec.core.compiler.api.PublicationStatus",
            "dec.core.compiler.api.CancellationToken",
            "dec.core.compiler.api.ContextPublisher",
            "dec.core.compiler.api.Deadline",
            "dec.core.compiler.api.MonotonicClock",
            "dec.core.compiler.api.CompilationObserver",
            "dec.core.compiler.api.CompilationTiming",
            "dec.core.compiler.api.SessionStateTransition",
            "dec.core.compiler.source.SourceReference",
            "dec.core.compiler.source.DocumentSourceProvider",
            "dec.core.compiler.canonical.FrontendRegistry");

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
        assertEquals(1, methods.length, "ModelCompiler must expose exactly one method");

        Method entry = methods[0];
        assertEquals("compileAndPublish", entry.getName());
        assertTrue(Modifier.isPublic(entry.getModifiers()));
        assertEquals(CompilationResult.class, entry.getReturnType());
        assertEquals(
                Arrays.asList(CompilationRequest.class, PublicationRequest.class),
                Arrays.asList(entry.getParameterTypes()));
    }

    @Test
    void keepsValueObjectsFinalWithPrivateFinalState() {
        List<Class<?>> valueTypes = Arrays.asList(
                CompilationOptions.class,
                CompilationRequest.class,
                PublicationRequest.class,
                SourceReference.class,
                Deadline.class,
                CompilationTiming.class,
                SessionStateTransition.class,
                PublishedCompilationResult.class,
                FailedCompilationResult.class);

        for (Class<?> valueType : valueTypes) {
            assertTrue(Modifier.isFinal(valueType.getModifiers()), valueType.getName());
            assertPrivateFinalSourceFields(valueType);
        }
        assertTrue(CompilationResult.class.isInterface());
        assertTrue(PublicationResult.class.isInterface());
    }

    @Test
    void requestPreservesEveryInjectedSessionDependency() {
        SourceReference root = new SourceReference("classpath:mix/orm-config.xml");
        DocumentSourceProvider provider = provider();
        FrontendRegistry frontends = format -> null;
        CompilationOptions options = new CompilationOptions("schema-1", "options-1");
        Deadline deadline = new Deadline(123L);
        CancellationToken token = () -> false;
        MonotonicClock clock = () -> 100L;
        CompilationObserver observer = observer();

        CompilationRequest request = new CompilationRequest(
                root,
                provider,
                frontends,
                options,
                Optional.of(deadline),
                token,
                clock,
                observer);

        assertSame(root, request.root());
        assertSame(provider, request.sourceProvider());
        assertSame(frontends, request.frontends());
        assertSame(options, request.options());
        assertSame(deadline, request.deadline().get());
        assertSame(token, request.cancellationToken());
        assertSame(clock, request.clock());
        assertSame(observer, request.observer());
        assertEquals("schema-1", options.schemaVersion());
        assertEquals("options-1", options.optionsDigest());
        assertFalse(Arrays.stream(CompilationOptions.class.getMethods())
                .anyMatch(method -> method.getName().equals("deadlineNanos")));

        assertThrows(
                NullPointerException.class,
                () -> new CompilationRequest(
                        root,
                        provider,
                        frontends,
                        options,
                        null,
                        token,
                        clock,
                        observer));
    }

    @Test
    void deadlineUsesTheInjectedMonotonicClockDomain() {
        Deadline deadline = new Deadline(100L);
        assertFalse(deadline.isExpired(99L));
        assertTrue(deadline.isExpired(100L));
        assertEquals(100L, deadline.deadlineNanos());
        assertThrows(IllegalArgumentException.class, () -> new Deadline(-1L));
    }

    @Test
    void publicationRequestUsesOptionalAndSeparateStatus() {
        ContextPublisher publisher = (expectedCurrent, candidate) ->
                () -> PublicationStatus.PUBLISHED;
        PublicationRequest request = new PublicationRequest(Optional.empty(), publisher);

        assertFalse(request.expectedCurrent().isPresent());
        assertSame(publisher, request.publisher());
        assertEquals(
                PublicationStatus.PUBLISHED,
                publisher.publish(Optional.empty(), null).status());
        assertThrows(NullPointerException.class, () -> new PublicationRequest(null, publisher));
        assertThrows(
                NullPointerException.class,
                () -> new PublicationRequest(Optional.empty(), null));
    }

    @Test
    void failedResultDefensivelyCopiesDiagnosticsAndExposesNoCandidate() {
        List<Diagnostic> mutableDiagnostics = new ArrayList<Diagnostic>();
        mutableDiagnostics.add(publicationBlockedDiagnostic());

        FailedCompilationResult result = FailedCompilationResult.failed(mutableDiagnostics);
        mutableDiagnostics.clear();

        assertEquals(CompilationStatus.FAILED, result.status());
        assertEquals(1, result.diagnostics().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> result.diagnostics().add(publicationBlockedDiagnostic()));
        assertThrows(
                IllegalArgumentException.class,
                () -> FailedCompilationResult.failed(Collections.<Diagnostic>emptyList()));
        assertFalse(Arrays.stream(FailedCompilationResult.class.getMethods())
                .anyMatch(method -> method.getName().equals("engineContext")
                        || method.getName().equals("modelSet")
                        || method.getName().equals("digests")
                        || method.getName().equals("compilerVersion")
                        || method.getName().equals("schemaVersion")
                        || method.getName().equals("optionsDigest")
                        || method.getName().equals("digestAlgorithmVersion")));
    }

    @Test
    void resultInterfacesExposeOnlyTheFrozenTerminalContract() {
        assertEquals(
                Arrays.asList("diagnostics", "status"),
                sortedDeclaredMethodNames(CompilationResult.class));
        assertEquals(
                Collections.singletonList("status"),
                sortedDeclaredMethodNames(PublicationResult.class));
        assertFalse(Arrays.stream(CompilationResult.class.getMethods())
                .anyMatch(method -> method.getName().equals("sessionId")
                        || method.getName().equals("isPublished")));
    }

    /**
     * 创建只用于验证实例注入的 Source Provider。
     */
    private static DocumentSourceProvider provider() {
        return new DocumentSourceProvider() {
            @Override
            public SourceResolutionResult resolve(
                    SourceReference reference,
                    SourceResolutionContext context) {
                return null;
            }

            @Override
            public SourceResolutionResult resolveFileSet(
                    SourceReference reference,
                    SourceResolutionContext context) {
                return null;
            }
        };
    }

    /**
     * 创建只用于验证实例注入的 Observer。
     */
    private static CompilationObserver observer() {
        return new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                // 测试替身不记录外部状态。
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                // 测试替身不记录外部状态。
            }
        };
    }

    /**
     * 只检查源码声明字段，忽略 JaCoCo 注入的 synthetic 状态。
     */
    private static void assertPrivateFinalSourceFields(Class<?> valueType) {
        for (Field field : valueType.getDeclaredFields()) {
            if (field.isSynthetic()) {
                continue;
            }
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.toString());
            assertTrue(Modifier.isFinal(field.getModifiers()), field.toString());
            assertFalse(Modifier.isStatic(field.getModifiers()), field.toString());
        }
    }

    /**
     * 返回按名称稳定排序的直接声明方法。
     */
    private static List<String> sortedDeclaredMethodNames(Class<?> type) {
        List<String> names = new ArrayList<String>();
        for (Method method : type.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                names.add(method.getName());
            }
        }
        Collections.sort(names);
        return names;
    }

    /**
     * 创建能够明确说明失败原因的 ERROR Diagnostic。
     */
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
