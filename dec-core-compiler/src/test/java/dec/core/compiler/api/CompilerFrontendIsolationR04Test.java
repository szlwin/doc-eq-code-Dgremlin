package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.source.DocumentSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

/**
 * 验证 Source/Frontend 公共边界不暴露 DOM、YAML Node 或第三方 Parser 类型。
 */
class CompilerFrontendIsolationR04Test {
    private static final String[] FORBIDDEN_PREFIXES = {
        "org.w3c.dom",
        "org.yaml",
        "org.snakeyaml",
        "com.fasterxml.jackson",
        "javax.xml",
        "jakarta.xml"
    };

    @Test
    void sourceFrontendAndCanonicalApisExposeOnlyFormatNeutralTypes()
            throws Exception {
        assertPublicApiTypesAreFormatNeutral(DocumentSource.class);
        assertPublicApiTypesAreFormatNeutral(CanonicalDocumentNode.class);
        assertPublicApiTypesAreFormatNeutral(FrontendResult.class);
        assertPublicApiTypesAreFormatNeutral(DocumentFrontend.class);

        Method parse = DocumentFrontend.class.getMethod(
                "parse",
                DocumentSource.class,
                dec.core.compiler.canonical.FrontendOptions.class);
        assertEquals(FrontendResult.class, parse.getReturnType());
    }

    /**
     * 扫描公共构造器和公共方法的原始类型，阻止 Parser 实现泄漏到冻结 API。
     */
    private static void assertPublicApiTypesAreFormatNeutral(Class<?> owner) {
        for (Constructor<?> constructor : owner.getConstructors()) {
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                assertAllowedType(owner, parameterType);
            }
        }
        for (Method method : owner.getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            assertAllowedType(owner, method.getReturnType());
            for (Class<?> parameterType : method.getParameterTypes()) {
                assertAllowedType(owner, parameterType);
            }
        }
    }

    /**
     * 验证单个 API 类型不属于禁止的 Parser 或文档树实现包。
     */
    private static void assertAllowedType(Class<?> owner, Class<?> type) {
        Class<?> inspected = type.isArray() ? type.getComponentType() : type;
        String typeName = inspected.getName();
        for (String prefix : FORBIDDEN_PREFIXES) {
            assertFalse(
                    typeName.startsWith(prefix),
                    owner.getName() + " leaks implementation type " + typeName);
        }
    }
}
