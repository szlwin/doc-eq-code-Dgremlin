package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.SourceRef;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * 冻结 TASK-P1-T02 I004 的 Source、Frontend 与 Canonical 数据闭包。
 *
 * <p>本测试只通过反射引用尚未实现的公共类型，使 RED 阶段仍能完成
 * Java 8 测试源码编译。失败必须来自合同缺失，而不能来自编译错误。</p>
 */
class CompilerSourceFrontendClosureContractR04Test {
    @Test
    void documentSourceCarriesCompleteSecurityFacts() throws Exception {
        Class<?> documentSource = requiredType(
                "dec.core.compiler.source.DocumentSource");
        Class<?> allowedRoot = requiredType(
                "dec.core.compiler.source.AllowedRoot");
        Class<?> documentFormat = requiredType(
                "dec.core.compiler.canonical.DocumentFormat");

        assertTrue(Modifier.isFinal(documentSource.getModifiers()));
        Constructor<?> constructor = documentSource.getConstructor(
                String.class,
                URI.class,
                documentFormat,
                allowedRoot,
                byte[].class,
                String.class);
        assertNotNull(constructor);

        assertAccessor(documentSource, "sourceId", String.class);
        assertAccessor(documentSource, "uri", URI.class);
        assertAccessor(documentSource, "format", documentFormat);
        assertAccessor(documentSource, "allowedRoot", allowedRoot);
        assertAccessor(documentSource, "content", byte[].class);
        assertAccessor(documentSource, "contentDigest", String.class);

        allowedRoot.getConstructor(URI.class);
        assertAccessor(allowedRoot, "uri", URI.class);
        assertMethod(allowedRoot, "contains", boolean.class, URI.class);
    }

    @Test
    void canonicalNodeFreezesFormatNeutralParseFacts() throws Exception {
        Class<?> canonicalNode = requiredType(
                "dec.core.compiler.canonical.CanonicalDocumentNode");
        Class<?> documentFormat = requiredType(
                "dec.core.compiler.canonical.DocumentFormat");

        assertTrue(Modifier.isFinal(canonicalNode.getModifiers()));
        canonicalNode.getConstructor(
                String.class,
                Map.class,
                Optional.class,
                List.class,
                SourceRef.class,
                documentFormat,
                String.class);

        assertAccessor(canonicalNode, "name", String.class);
        assertAccessor(canonicalNode, "attributes", Map.class);
        assertAccessor(canonicalNode, "scalar", Optional.class);
        assertAccessor(canonicalNode, "children", List.class);
        assertAccessor(canonicalNode, "sourceRef", SourceRef.class);
        assertAccessor(canonicalNode, "format", documentFormat);
        assertAccessor(canonicalNode, "schemaVersion", String.class);
    }

    @Test
    void frontendResultCarriesExactlyOneCanonicalSuccessOrTypedFailure()
            throws Exception {
        Class<?> frontendResult = requiredType(
                "dec.core.compiler.canonical.FrontendResult");
        Class<?> frontendStatus = requiredType(
                "dec.core.compiler.canonical.FrontendStatus");
        Class<?> canonicalNode = requiredType(
                "dec.core.compiler.canonical.CanonicalDocumentNode");
        Class<?> frontendResults = requiredType(
                "dec.core.compiler.canonical.FrontendResults");

        assertTrue(frontendResult.isInterface());
        assertTrue(frontendStatus.isEnum());
        assertEquals(
                new HashSet<String>(Arrays.asList("PARSED", "FAILED")),
                enumNames(frontendStatus));
        assertEquals(
                new HashSet<String>(Arrays.asList(
                        "status",
                        "canonicalRoot",
                        "diagnostics")),
                declaredPublicMethodNames(frontendResult));

        assertAccessor(frontendResult, "status", frontendStatus);
        assertAccessor(frontendResult, "canonicalRoot", Optional.class);
        assertAccessor(frontendResult, "diagnostics", List.class);
        assertFalsePublicMethod(frontendResult, "isSuccessful");

        Method parsed = frontendResults.getMethod(
                "parsed",
                canonicalNode,
                List.class);
        Method failed = frontendResults.getMethod("failed", List.class);
        assertTrue(Modifier.isStatic(parsed.getModifiers()));
        assertTrue(Modifier.isStatic(failed.getModifiers()));
        assertEquals(frontendResult, parsed.getReturnType());
        assertEquals(frontendResult, failed.getReturnType());
    }

    @Test
    void providerFrontendCanonicalFlowNeedsNoAdditionalPublicSignature()
            throws Exception {
        Class<?> sourceReference = requiredType(
                "dec.core.compiler.source.SourceReference");
        Class<?> sourceContext = requiredType(
                "dec.core.compiler.source.SourceResolutionContext");
        Class<?> sourceResult = requiredType(
                "dec.core.compiler.source.SourceResolutionResult");
        Class<?> sourceProvider = requiredType(
                "dec.core.compiler.source.DocumentSourceProvider");
        Class<?> documentSource = requiredType(
                "dec.core.compiler.source.DocumentSource");
        Class<?> documentFormat = requiredType(
                "dec.core.compiler.canonical.DocumentFormat");
        Class<?> frontendRegistry = requiredType(
                "dec.core.compiler.canonical.FrontendRegistry");
        Class<?> documentFrontend = requiredType(
                "dec.core.compiler.canonical.DocumentFrontend");
        Class<?> frontendOptions = requiredType(
                "dec.core.compiler.canonical.FrontendOptions");
        Class<?> frontendResult = requiredType(
                "dec.core.compiler.canonical.FrontendResult");

        assertMethod(
                sourceProvider,
                "resolve",
                sourceResult,
                sourceReference,
                sourceContext);
        assertAccessor(sourceResult, "sources", List.class);
        assertAccessor(documentSource, "format", documentFormat);
        assertMethod(frontendRegistry, "require", documentFrontend, documentFormat);
        assertMethod(
                documentFrontend,
                "parse",
                frontendResult,
                documentSource,
                frontendOptions);
        assertAccessor(frontendResult, "canonicalRoot", Optional.class);
    }

    /**
     * 加载必须存在的公共类型，并把缺失类型转为明确合同失败。
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
     * 返回直接声明的公共方法名称集合。
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
     * 验证指定旧方法不再属于冻结公共合同。
     */
    private static void assertFalsePublicMethod(Class<?> owner, String methodName) {
        for (Method method : owner.getMethods()) {
            if (method.getName().equals(methodName)) {
                throw new AssertionError(
                        owner.getName() + " must not expose public method " + methodName);
            }
        }
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
}
