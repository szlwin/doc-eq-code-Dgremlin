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
 * Verifies the Maven ownership boundary without relying on a downloaded
 * dependency-tree plugin during the unit-test phase.
 */
class ModuleDependencyTest {
    @Test
    void compilerDependsOnContextAndParentRegistersCompiler() throws IOException {
        Path moduleDirectory = Paths.get(System.getProperty("user.dir"));
        String modulePom = read(moduleDirectory.resolve("pom.xml"));
        String parentPom = read(moduleDirectory.resolve("..").resolve("pom.xml").normalize());

        assertTrue(modulePom.contains("<artifactId>dec-core-context</artifactId>"));
        assertFalse(modulePom.contains("<artifactId>dec-core-model</artifactId>"));
        assertFalse(modulePom.contains("<artifactId>dec-core-starter</artifactId>"));
        assertTrue(parentPom.contains("<module>dec-core-compiler</module>"));
        assertTrue(parentPom.contains("<artifactId>dec-core-compiler</artifactId>"));
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
