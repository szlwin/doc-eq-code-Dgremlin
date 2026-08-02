package dec.core.compiler.canonical.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

/**
 * T05 YAML Frontend 的公共 API、内部状态和架构隔离 Oracle。
 */
class YamlFrontendArchitectureTest {
    private static final String FRONTEND_CLASS =
            "dec.core.compiler.canonical.yaml.SafeYamlDocumentFrontend";

    /**
     * 生产类型必须公开、final、可无参构造并且只声明 YAML 格式。
     */
    @Test
    void exposesDirectFinalDocumentFrontendApi() throws Exception {
        Class<?> type = Class.forName(FRONTEND_CLASS);
        Constructor<?> constructor = type.getConstructor();
        Object instance = constructor.newInstance();

        assertTrue(Modifier.isPublic(type.getModifiers()));
        assertTrue(Modifier.isFinal(type.getModifiers()));
        assertTrue(DocumentFrontend.class.isAssignableFrom(type));
        assertEquals(DocumentFormat.YAML,
                ((DocumentFrontend) instance).format());
    }

    /**
     * Frontend 字段不得长期持有 SnakeYAML Node、旧 Config 或运行时 Context。
     */
    @Test
    void doesNotHoldParserTreeOrRuntimeConfigurationState() throws Exception {
        Class<?> type = Class.forName(FRONTEND_CLASS);
        for (Field field : type.getDeclaredFields()) {
            assertAllowedPublishedType(field.getType());
        }
    }

    /**
     * 公共构造器和公共方法不得向调用方暴露 SnakeYAML 或旧运行时类型。
     */
    @Test
    void publicApiDoesNotExposeParserTypes() throws Exception {
        Class<?> type = Class.forName(FRONTEND_CLASS);
        for (Constructor<?> constructor : type.getConstructors()) {
            for (Class<?> parameter : constructor.getParameterTypes()) {
                assertAllowedPublishedType(parameter);
            }
        }
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            assertAllowedPublishedType(method.getReturnType());
            for (Class<?> parameter : method.getParameterTypes()) {
                assertAllowedPublishedType(parameter);
            }
        }
    }

    /**
     * 禁止 parser、旧 Config 和运行时 Context 类型进入持久字段或公共签名。
     */
    private static void assertAllowedPublishedType(Class<?> type) {
        String name = type.getName();
        assertFalse(name.startsWith("org.yaml.snakeyaml.nodes."), name);
        assertFalse(name.startsWith("org.yaml.snakeyaml.constructor."), name);
        assertFalse(name.contains("ConfigFactory"), name);
        assertFalse(name.contains("ConfigInfo"), name);
        assertFalse(name.endsWith("Registry"), name);
        assertFalse(name.endsWith("EngineContext"), name);
        assertFalse(name.contains("RawDefinition"), name);
    }
}
