package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * 在单元测试阶段直接验证 Maven 模块所有权边界，避免依赖额外下载的依赖树插件。
 */
class ModuleDependencyTest {
    @Test
    void compilerDependsOnlyOnContextAndParentRegistersCompiler() throws IOException {
        Path moduleDirectory = Paths.get(System.getProperty("user.dir"));
        String modulePom = read(moduleDirectory.resolve("pom.xml"));
        String parentPom = read(moduleDirectory.resolve("..").resolve("pom.xml").normalize());

        assertTrue(modulePom.contains("<artifactId>dec-core-context</artifactId>"));
        assertFalse(modulePom.contains("<artifactId>dec-core-model</artifactId>"));
        assertFalse(modulePom.contains("<artifactId>dec-core-starter</artifactId>"));
        assertTrue(parentPom.contains("<module>dec-core-compiler</module>"));
        assertTrue(parentPom.contains("<artifactId>dec-core-compiler</artifactId>"));
    }

    /**
     * 使用 UTF-8 读取 POM，保证测试结果不依赖 Runner 默认编码。
     */
    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
