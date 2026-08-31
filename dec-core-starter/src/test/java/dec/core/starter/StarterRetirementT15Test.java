package dec.core.starter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.ModelCompiler;
import dec.core.context.config.model.config.ConfigInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T15 行为合同：Compiler 保持实例边界，简洁配置门面不得暴露 ConfigInfo。
 */
class StarterRetirementT15Test {

    /** 新 Starter 必须存在、为 final，且唯一构造依赖是 ModelCompiler。 */
    @Test
    void compilerStarterUsesOnlyInstanceCompilerBoundary() {
        Class<?> starter = assertDoesNotThrow(
                () -> Class.forName("dec.core.starter.CompilerStarter"));

        assertTrue(Modifier.isFinal(starter.getModifiers()));
        Constructor<?>[] constructors = starter.getConstructors();
        assertEquals(1, constructors.length);
        assertEquals(1, constructors[0].getParameterCount());
        assertEquals(ModelCompiler.class, constructors[0].getParameterTypes()[0]);

        List<Field> contractFields = new ArrayList<Field>();
        for (Field field : starter.getDeclaredFields()) {
            // JaCoCo 会注入 synthetic static 字段，该字段不属于 Starter 业务状态合同。
            if (field.isSynthetic()) {
                continue;
            }
            contractFields.add(field);
            assertFalse(
                    Modifier.isStatic(field.getModifiers()),
                    "Starter 不得保存 static current 或全局状态");
        }
        assertEquals(1, contractFields.size());
        assertEquals(ModelCompiler.class, contractFields.get(0).getType());
        assertTrue(Modifier.isFinal(contractFields.get(0).getModifiers()));
    }

    /** 简洁配置门面可以恢复，但业务调用者不得创建或传递 ConfigInfo。 */
    @Test
    void simpleConfigurationFacadesHideConfigInfo() {
        Class<?> configUtil = assertDoesNotThrow(
                () -> Class.forName("dec.core.starter.common.ConfigUtil"));
        Class<?> dataSourceManager = assertDoesNotThrow(
                () -> Class.forName("dec.core.starter.common.DataSourceManager"));

        assertPublicMethodsHideConfigInfo(configUtil);
        assertPublicMethodsHideConfigInfo(dataSourceManager);
    }

    private static void assertPublicMethodsHideConfigInfo(Class<?> facade) {
        for (Method method : facade.getMethods()) {
            if (method.getDeclaringClass() != facade) {
                continue;
            }
            assertFalse(
                    ConfigInfo.class.isAssignableFrom(method.getReturnType()),
                    method + " 不得返回 ConfigInfo");
            for (Class<?> parameterType : method.getParameterTypes()) {
                assertFalse(
                        ConfigInfo.class.isAssignableFrom(parameterType),
                        method + " 不得接收 ConfigInfo");
            }
        }
    }
}
