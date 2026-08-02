package dec.core.compiler.canonical.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendStatus;
import dec.core.context.model.DiagnosticCode;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

/**
 * T04 XML Frontend 的直接 API、模块隔离和安全结构 Oracle。
 */
class XmlFrontendArchitectureTest {

    /**
     * 生产类型必须可通过公开无参构造直接使用，并且只声明 XML 格式。
     */
    @Test
    void exposesDirectFinalDocumentFrontendApi() {
        SecureXmlDocumentFrontend frontend = new SecureXmlDocumentFrontend();

        assertTrue(Modifier.isPublic(SecureXmlDocumentFrontend.class.getModifiers()));
        assertTrue(Modifier.isFinal(SecureXmlDocumentFrontend.class.getModifiers()));
        assertTrue(DocumentFrontend.class.isAssignableFrom(
                SecureXmlDocumentFrontend.class));
        assertEquals(DocumentFormat.XML, frontend.format());

        FrontendResult result = frontend.parse(
                XmlFrontendTestSupport.xmlSource("<root><child/></root>"),
                new dec.core.compiler.canonical.FrontendOptions("1.0"));
        assertEquals(FrontendStatus.PARSED, result.status());
    }

    /**
     * Frontend 及其内部类型不得持有 DOM4J、旧 Config、Registry 或 EngineContext。
     */
    @Test
    void doesNotHoldParserOrRuntimeConfigurationTypes() {
        assertForbiddenTypesAbsent(SecureXmlDocumentFrontend.class);
        for (Class<?> nested : SecureXmlDocumentFrontend.class.getDeclaredClasses()) {
            assertForbiddenTypesAbsent(nested);
        }
        assertForbiddenTypesAbsent(CanonicalDocumentNode.class);
    }

    /**
     * 不同命名空间使用同一 local-name 的属性必须失败，避免 Canonical 覆盖。
     */
    @Test
    void rejectsDuplicateCanonicalAttributeLocalNames() {
        String xml = "<root xmlns:a=\"urn:a\" xmlns:b=\"urn:b\" "
                + "a:id=\"1\" b:id=\"2\"/>";
        XmlFrontendTestSupport.FrontendHarness harness =
                XmlFrontendTestSupport.frontend();

        FrontendResult result = XmlFrontendTestSupport.parse(
                harness,
                XmlFrontendTestSupport.xmlSource(xml));

        assertEquals(FrontendStatus.FAILED, result.status());
        assertFalse(result.canonicalRoot().isPresent());
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.code()
                        == DiagnosticCode.MIX_FRONTEND_XML_UNSAFE));
        assertEquals(0, harness.externalAccessAttempts());
    }

    /**
     * XInclude 只能作为普通 Canonical 节点保留，不能触发包含或外部访问。
     */
    @Test
    void keepsXIncludeAsDataWithoutResolvingIt() {
        String xml = "<root xmlns:xi=\"http://www.w3.org/2001/XInclude\">"
                + "<xi:include href=\"https://example.invalid/data.xml\"/>"
                + "</root>";
        XmlFrontendTestSupport.FrontendHarness harness =
                XmlFrontendTestSupport.frontend();

        FrontendResult result = XmlFrontendTestSupport.parse(
                harness,
                XmlFrontendTestSupport.xmlSource(xml));

        assertEquals(FrontendStatus.PARSED, result.status());
        assertEquals("include", result.canonicalRoot().get()
                .children().get(0).name());
        assertEquals("https://example.invalid/data.xml",
                result.canonicalRoot().get().children().get(0)
                        .attributes().get("href"));
        assertEquals(0, harness.externalAccessAttempts());
    }

    /**
     * 检查字段、构造参数和方法签名不暴露禁止类型。
     */
    private static void assertForbiddenTypesAbsent(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            assertAllowedType(field.getType());
        }
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            for (Class<?> parameter : constructor.getParameterTypes()) {
                assertAllowedType(parameter);
            }
        }
        for (Method method : type.getDeclaredMethods()) {
            assertAllowedType(method.getReturnType());
            for (Class<?> parameter : method.getParameterTypes()) {
                assertAllowedType(parameter);
            }
        }
    }

    /**
     * 拒绝 DOM、旧 Config 和运行时 Context/Registry 类型进入 Frontend 状态。
     */
    private static void assertAllowedType(Class<?> type) {
        String name = type.getName();
        assertFalse(name.startsWith("org.dom4j."), name);
        assertFalse(name.contains("ConfigFactory"), name);
        assertFalse(name.contains("ConfigInfo"), name);
        assertFalse(name.endsWith("Registry"), name);
        assertFalse(name.endsWith("EngineContext"), name);
        assertFalse(name.startsWith("org.w3c.dom."), name);
    }
}
