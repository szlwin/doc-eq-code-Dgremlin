package dec.core.compiler.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 生产 Classpath Provider 的真实目录解析与重复身份门禁。
 */
class ClasspathDocumentSourceProviderTest {

    /** 唯一 classpath 资源必须解析为带真实摘要的不可变 Source。 */
    @Test
    void resolvesUniqueClasspathSource(@TempDir Path directory) throws Exception {
        write(directory, "stage/item.xml", "<data name=\"item\"/>");
        URLClassLoader loader = loader(directory);
        try {
            ClasspathDocumentSourceProvider provider = provider(loader);
            SourceResolutionResult result = provider.resolve(
                    new SourceReference("classpath:stage/item.xml"),
                    context());

            assertEquals(SourceResolutionStatus.RESOLVED, result.status());
            assertEquals(1, result.sources().size());
            assertEquals(64, result.sources().get(0).contentDigest().length());
        } finally {
            loader.close();
        }
    }

    /** 同一 SourceId 来自两个 classpath 根时，单文件解析必须 fail-closed。 */
    @Test
    void rejectsDuplicateSingleSource(
            @TempDir Path directory) throws Exception {
        Path first = Files.createDirectory(directory.resolve("first"));
        Path second = Files.createDirectory(directory.resolve("second"));
        write(first, "stage/item.xml", "<data name=\"first\"/>");
        write(second, "stage/item.xml", "<data name=\"second\"/>");
        URLClassLoader loader = loader(first, second);
        try {
            SourceResolutionResult result = provider(loader).resolve(
                    new SourceReference("classpath:stage/item.xml"),
                    context());

            assertEquals(SourceResolutionStatus.FAILED, result.status());
            assertTrue(result.diagnostics().get(0).messageKey()
                    .contains("duplicate"));
        } finally {
            loader.close();
        }
    }

    /** 文件集中的重复相对路径同样不得被后一个 classpath 根静默覆盖。 */
    @Test
    void rejectsDuplicateFileSetSource(
            @TempDir Path directory) throws Exception {
        Path first = Files.createDirectory(directory.resolve("first"));
        Path second = Files.createDirectory(directory.resolve("second"));
        write(first, "stage/item.xml", "<data name=\"first\"/>");
        write(second, "stage/item.xml", "<data name=\"second\"/>");
        URLClassLoader loader = loader(first, second);
        try {
            SourceResolutionResult result = provider(loader).resolveFileSet(
                    new SourceReference("classpath:stage/"),
                    context());

            assertEquals(SourceResolutionStatus.FAILED, result.status());
        } finally {
            loader.close();
        }
    }

    /** 创建只允许 stage 根的生产 Provider。 */
    private static ClasspathDocumentSourceProvider provider(
            ClassLoader loader) {
        return new ClasspathDocumentSourceProvider(
                loader,
                new AllowedRoot(URI.create("classpath:stage/")));
    }

    /** 创建不继承应用资源的隔离 URLClassLoader。 */
    private static URLClassLoader loader(Path... roots) throws Exception {
        URL[] urls = new URL[roots.length];
        for (int index = 0; index < roots.length; index++) {
            urls[index] = roots[index].toUri().toURL();
        }
        return new URLClassLoader(urls, null);
    }

    /** 写入 UTF-8 测试资源并创建父目录。 */
    private static void write(
            Path root,
            String relative,
            String content) throws Exception {
        Path target = root.resolve(relative);
        Files.createDirectories(target.getParent());
        Files.write(target, content.getBytes(StandardCharsets.UTF_8));
    }

    /** 创建根级、无父 Source 的解析上下文。 */
    private static SourceResolutionContext context() {
        return new SourceResolutionContext() {
            @Override
            public SourceReference root() {
                return new SourceReference("classpath:stage/item.xml");
            }

            @Override
            public Optional<String> parentSourceId() {
                return Optional.empty();
            }

            @Override
            public int depth() {
                return 0;
            }
        };
    }
}
