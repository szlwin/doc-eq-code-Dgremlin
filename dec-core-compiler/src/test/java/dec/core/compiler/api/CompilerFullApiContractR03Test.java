package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.EngineContext;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DigestPair;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * 冻结 TASK-P1-T02 I003 的完整 R05/R10 公共 API。
 *
 * <p>测试只通过反射引用尚未实现的类型，确保 RED 阶段仍能完成 Java 8 编译，
 * 失败只能来自公共合同尚未实现，而不能来自测试源码编译错误。</p>
 */
class CompilerFullApiContractR03Test {
    @Test
    void compilationRequestExposesCompleteInjectedSessionBoundary() throws Exception {
        Class<?> sourceReference = requiredType(
                "dec.core.compiler.source.SourceReference");
        Class<?> sourceProvider = requiredType(
                "dec.core.compiler.source.DocumentSourceProvider");
        Class<?> frontendRegistry = requiredType(
                "dec.core.compiler.canonical.FrontendRegistry");
        Class<?> deadline = requiredType("dec.core.compiler.api.Deadline");
        Class<?> clock = requiredType("dec.core.compiler.api.MonotonicClock");
        Class<?> observer = requiredType("dec.core.compiler.api.CompilationObserver");

        Constructor<CompilationRequest> constructor = CompilationRequest.class.getConstructor(
                sourceReference,
                sourceProvider,
                frontendRegistry,
                CompilationOptions.class,
                Optional.class,
                CancellationToken.class,
                clock,
                observer);
        assertNotNull(constructor);

        assertAccessor(CompilationRequest.class, "root", sourceReference);
        assertAccessor(CompilationRequest.class, "sourceProvider", sourceProvider);
        assertAccessor(CompilationRequest.class, "frontends", frontendRegistry);
        assertAccessor(CompilationRequest.class, "options", CompilationOptions.class);
        assertAccessor(CompilationRequest.class, "deadline", Optional.class);
        assertAccessor(
                CompilationRequest.class,
                "cancellationToken",
                CancellationToken.class);
        assertAccessor(CompilationRequest.class, "clock", clock);
        assertAccessor(CompilationRequest.class, "observer", observer);

        CompilationOptions.class.getConstructor(String.class, String.class);
        assertAccessor(CompilationOptions.class, "schemaVersion", String.class);
        assertAccessor(CompilationOptions.class, "optionsDigest", String.class);
        assertFalse(hasPublicMethod(CompilationOptions.class, "deadlineNanos"));
    }

    @Test
    void publicationContractUsesOptionalAndSeparateStatus() throws Exception {
        Class<?> publicationStatus = requiredType(
                "dec.core.compiler.api.PublicationStatus");
        assertTrue(publicationStatus.isEnum());
        assertEquals(
                new HashSet<String>(Arrays.asList("PUBLISHED", "CONFLICT")),
                enumNames(publicationStatus));

        assertTrue(PublicationResult.class.isInterface());
        assertAccessor(PublicationResult.class, "status", publicationStatus);

        PublicationRequest.class.getConstructor(Optional.class, ContextPublisher.class);
        assertAccessor(PublicationRequest.class, "expectedCurrent", Optional.class);
        Method publish = ContextPublisher.class.getMethod(
                "publish",
                Optional.class,
                EngineContext.class);
        assertEquals(PublicationResult.class, publish.getReturnType());
    }

    @Test
    void compilationResultMatchesTheFrozenInterfaceAndPublishedFact() throws Exception {
        assertTrue(CompilationResult.class.isInterface());
        assertEquals(
                new HashSet<String>(Arrays.asList("status", "diagnostics")),
                declaredPublicMethodNames(CompilationResult.class));

        assertAccessor(PublishedCompilationResult.class, "modelSet", CompiledModelSet.class);
        assertAccessor(PublishedCompilationResult.class, "engineContext", EngineContext.class);
        assertAccessor(PublishedCompilationResult.class, "digests", DigestPair.class);
        assertAccessor(PublishedCompilationResult.class, "compilerVersion", String.class);
        assertAccessor(PublishedCompilationResult.class, "schemaVersion", String.class);
        assertAccessor(PublishedCompilationResult.class, "optionsDigest", String.class);
        assertAccessor(
                PublishedCompilationResult.class,
                "digestAlgorithmVersion",
                String.class);

        Method publishedFactory = PublishedCompilationResult.class.getMethod(
                "published",
                List.class,
                CompiledModelSet.class,
                EngineContext.class,
                DigestPair.class,
                String.class,
                String.class,
                String.class,
                String.class);
        assertTrue(Modifier.isStatic(publishedFactory.getModifiers()));
        assertEquals(PublishedCompilationResult.class, publishedFactory.getReturnType());

        Method failedFactory = FailedCompilationResult.class.getMethod("failed", List.class);
        assertTrue(Modifier.isStatic(failedFactory.getModifiers()));
        assertEquals(FailedCompilationResult.class, failedFactory.getReturnType());
        assertFalse(hasPublicMethod(CompilationResult.class, "sessionId"));
        assertFalse(hasPublicMethod(CompilationResult.class, "isPublished"));
    }

    @Test
    void sourceFrontendClockAndObserverRemainInstanceInjectedSeams() throws Exception {
        Class<?> sourceReference = requiredType(
                "dec.core.compiler.source.SourceReference");
        Class<?> sourceProvider = requiredType(
                "dec.core.compiler.source.DocumentSourceProvider");
        Class<?> sourceResolutionContext = requiredType(
                "dec.core.compiler.source.SourceResolutionContext");
        Class<?> sourceResolutionResult = requiredType(
                "dec.core.compiler.source.SourceResolutionResult");
        Class<?> frontendRegistry = requiredType(
                "dec.core.compiler.canonical.FrontendRegistry");
        Class<?> documentFormat = requiredType(
                "dec.core.compiler.canonical.DocumentFormat");
        Class<?> documentFrontend = requiredType(
                "dec.core.compiler.canonical.DocumentFrontend");
        Class<?> deadline = requiredType("dec.core.compiler.api.Deadline");
        Class<?> clock = requiredType("dec.core.compiler.api.MonotonicClock");
        Class<?> observer = requiredType("dec.core.compiler.api.CompilationObserver");
        Class<?> timing = requiredType("dec.core.compiler.api.CompilationTiming");
        Class<?> transition = requiredType(
                "dec.core.compiler.api.SessionStateTransition");

        assertMethod(
                sourceProvider,
                "resolve",
                sourceResolutionResult,
                sourceReference,
                sourceResolutionContext);
        assertMethod(
                sourceProvider,
                "resolveFileSet",
                sourceResolutionResult,
                sourceReference,
                sourceResolutionContext);
        assertMethod(frontendRegistry, "require", documentFrontend, documentFormat);
        assertMethod(clock, "nanoTime", long.class);
        assertMethod(observer, "onTiming", void.class, timing);
        assertMethod(observer, "onStateTransition", void.class, transition);

        deadline.getConstructor(long.class);
        assertMethod(deadline, "deadlineNanos", long.class);
        assertMethod(deadline, "isExpired", boolean.class, long.class);

        assertNoStaticMutableFields(
                CompilationRequest.class,
                sourceReference,
                deadline,
                timing,
                transition);
    }

    /**
     * 加载必须存在的公共类型，并把缺失类型记录为语义断言失败。
     */
    private static Class<?> requiredType(String typeName) {
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("Missing frozen API type: " + typeName, exception);
        }
    }

    /**
     * 验证无参数读取方法的返回类型。
     */
    private static void assertAccessor(
            Class<?> owner,
            String methodName,
            Class<?> returnType) throws Exception {
        assertMethod(owner, methodName, returnType);
    }

    /**
     * 验证公共方法的返回类型和参数顺序。
     */
    private static void assertMethod(
            Class<?> owner,
            String methodName,
            Class<?> returnType,
            Class<?>... parameterTypes) throws Exception {
        Method method = owner.getMethod(methodName, parameterTypes);
        assertEquals(returnType, method.getReturnType());
    }

    /**
     * 返回接口或类直接声明的公共方法名称。
     */
    private static Set<String> declaredPublicMethodNames(Class<?> owner) {
        Set<String> names = new HashSet<String>();
        for (Method method : owner.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                names.add(method.getName());
            }
        }
        return names;
    }

    /**
     * 检查指定公共方法是否存在。
     */
    private static boolean hasPublicMethod(Class<?> owner, String methodName) {
        for (Method method : owner.getMethods()) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回枚举常量名称集合。
     */
    private static Set<String> enumNames(Class<?> enumType) {
        Set<String> names = new HashSet<String>();
        for (Object constant : enumType.getEnumConstants()) {
            names.add(((Enum<?>) constant).name());
        }
        return names;
    }

    /**
     * 确保 Session 输入和值对象没有源码声明的可变 static 状态。
     */
    private static void assertNoStaticMutableFields(Class<?>... types) {
        for (Class<?> type : types) {
            for (Field field : type.getDeclaredFields()) {
                // JaCoCo 会在 CI 字节码中注入 synthetic $jacocoData，不属于源码状态。
                if (field.isSynthetic()) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers())) {
                    assertTrue(
                            Modifier.isFinal(field.getModifiers()),
                            type.getName() + " exposes mutable static field " + field.getName());
                }
            }
        }
    }
}
