package dec.core.context.runtime;

import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 仅用于隔离 helper 分支：把当前 GitHub Actions checkout 的源码快照写入 Surefire 报告目录。
 * 真实 P2 开发分支不会包含本类。
 */
class SourceSnapshotArtifactTest {

    @Test
    void exportSourceSnapshotForLocalVerification() throws IOException {
        // GitHub Actions 从仓库根目录启动 Maven，因此 user.dir 可稳定定位完整 checkout。
        Path repositoryRoot = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path output = repositoryRoot
                .resolve("dec-core-context")
                .resolve("target")
                .resolve("surefire-reports")
                .resolve("source-snapshot.zip")
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(output.getParent());

        try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(output)));
             Stream<Path> paths = Files.walk(repositoryRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> shouldInclude(repositoryRoot, output, path))
                    .forEach(path -> addEntry(repositoryRoot, zip, path));
        }
    }

    /**
     * 排除 Git 元数据、历史传输包与 Maven target，避免把无关大文件写入测试 artifact。
     */
    private boolean shouldInclude(Path repositoryRoot, Path output, Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        if (normalized.equals(output)) {
            return false;
        }
        Path relative = repositoryRoot.relativize(normalized);
        for (Path segment : relative) {
            String name = segment.toString();
            if (".git".equals(name) || ".common-develop-publish".equals(name) || "target".equals(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 逐文件写入 ZIP；异常包装为非法状态，确保 helper 测试失败时不会产生不完整快照。
     */
    private void addEntry(Path repositoryRoot, ZipOutputStream zip, Path path) {
        String entryName = repositoryRoot.relativize(path).toString().replace('\\', '/');
        try {
            zip.putNextEntry(new ZipEntry(entryName));
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    zip.write(buffer, 0, read);
                }
            }
            zip.closeEntry();
        } catch (IOException e) {
            throw new IllegalStateException("无法生成源码快照条目: " + entryName, e);
        }
    }
}
