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

    /**
     * AllowedRoot 内的符号链接不得把单文件解析带到 classpath 根之外。
     */
    @Test
    void rejectsSymlinkEscapeFromAllowedRoot(
            @TempDir Path directory) throws Exception {
        Path root = Files.createDirectory(directory.resolve("root"));
        Path outside = Files.createDirectory(directory.resolve("outside"));
        write(outside, "secret.xml", "<data name=\"secret\"/>");
        Files.createDirectories(root.resolve("stage"));
        Files.createSymbolicLink(
                root.resolve("stage/escape.xml"),
                outside.resolve("secret.xml"));
        URLClassLoader loader = loader(root);
        try {
            SourceResolutionResult result = provider(loader).resolve(
                    new SourceReference("classpath:stage/escape.xml"),
                    context());

            assertEquals(SourceResolutionStatus.FAILED, result.status());
            assertTrue(result.diagnostics().get(0).messageKey()
                    .contains("path-escape"));
        } finally {
            loader.close();
        }
    }

    /**
     * 文件集扫描遇到目录符号链接环时必须 fail-closed，且不得递归跟随。
     */
    @Test
    void rejectsSymlinkCycleInFileSet(
            @TempDir Path directory) throws Exception {
        Path root = Files.createDirectory(directory.resolve("root"));
        write(root, "stage/item.xml", "<data name=\"item\"/>");
        Files.createSymbolicLink(
                root.resolve("stage/loop"),
                root.resolve("stage"));
        URLClassLoader loader = loader(root);
        try {
            SourceResolutionResult result = provider(loader).resolveFileSet(
                    new SourceReference("classpath:stage/"),
                    context());

            assertEquals(SourceResolutionStatus.FAILED, result.status());
            assertTrue(result.diagnostics().get(0).messageKey()
                    .contains("path-escape"));
        } finally {
            loader.close();
        }
    }

    /**
     * 单个 Source 超过读取上限时必须由 Provider 在构造字节数组前拒绝。
     */
    @Test
    void rejectsOversizedSingleSourceBeforeFullRead(
            @TempDir Path directory) throws Exception {
        write(directory, "stage/item.xml",
                "<data name=\"this-source-is-too-large\"/>");
        URLClassLoader loader = loader(directory);
        try {
            SourceResolutionResult result = provider(loader, 8L).resolve(
                    new SourceReference("classpath:stage/item.xml"),
                    context());

            assertEquals(SourceResolutionStatus.FAILED, result.status());
            assertTrue(result.diagnostics().get(0).messageKey()
                    .contains("byte-budget"));
        } finally {
            loader.close();
        }
    }

    /**
     * 同一次文件集解析的累计字节不得超过 SourcePolicy 对应读取上限。
     */
    @Test
    void rejectsFileSetAggregateByteBudget(
            @TempDir Path directory) throws Exception {
        write(directory, "stage/a.xml", "<a/>");
        write(directory, "stage/b.xml", "<bbbb/>");
        URLClassLoader loader = loader(directory);
        try {
            SourceResolutionResult result = provider(loader, 8L).resolveFileSet(
                    new SourceReference("classpath:stage/"),
                    context());

            assertEquals(SourceResolutionStatus.FAILED, result.status());
            assertTrue(result.diagnostics().get(0).messageKey()
                    .contains("byte-budget"));
        } finally {
            loader.close();
        }
    }

    /** 创建只允许 stage 根的生产 Provider。 */
    private static ClasspathDocumentSourceProvider provider(
            ClassLoader loader) {
        return provider(loader, 64L * 1024L * 1024L);
    }

    /** 创建带显式读取预算的生产 Provider。 */
    private static ClasspathDocumentSourceProvider provider(
            ClassLoader loader,
            long maxResolutionBytes) {
        return new ClasspathDocumentSourceProvider(
                loader,
                new AllowedRoot(URI.create("classpath:stage/")),
                maxResolutionBytes);
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
