package dec.core.starter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.ModelCompiler;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T15 行为合同：Starter 必须只保留实例级 Compiler 入口并退役旧全局配置路径。
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

        for (Field field : starter.getDeclaredFields()) {
            assertFalse(
                    Modifier.isStatic(field.getModifiers())
                            && !Modifier.isFinal(field.getModifiers()),
                    "Starter 不得保存 static mutable current 或全局状态");
        }
    }

    /** 旧 Starter 的全局 Config 写入口必须从发布 Artifact 中消失。 */
    @Test
    void legacyGlobalStarterEntryPointsAreRetired() {
        assertThrows(
                ClassNotFoundException.class,
                () -> Class.forName("dec.core.starter.common.ConfigUtil"));
        assertThrows(
                ClassNotFoundException.class,
                () -> Class.forName("dec.core.starter.common.DataSourceManager"));
    }

    /** Starter 发布依赖中不得继续携带旧 XML/YAML 配置 Parser。 */
    @Test
    void legacyParserTypesAreNotVisibleFromStarterRuntime() {
        assertThrows(
                ClassNotFoundException.class,
                () -> Class.forName(
                        "dec.context.parse.xml.parse.config.ConfigFileParser"));
        assertThrows(
                ClassNotFoundException.class,
                () -> Class.forName(
                        "dec.context.parse.yaml.YamlConfigParser"));
    }
}
