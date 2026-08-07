package dec.core.compiler.source;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 从应用 ClassLoader 安全读取单个文档或稳定展开文件集的生产 SourceProvider。
 */
public final class ClasspathDocumentSourceProvider
        implements DocumentSourceProvider {
    private static final String CLASSPATH_SCHEME = "classpath";
    private static final long DEFAULT_MAX_RESOLUTION_BYTES =
            64L * 1024L * 1024L;
    private final ClassLoader classLoader;
    private final AllowedRoot allowedRoot;
    private final long maxResolutionBytes;

    /**
     * 绑定显式 ClassLoader 和安全根；Provider 不读取全局可变配置。
     */
    public ClasspathDocumentSourceProvider(
            ClassLoader classLoader,
            AllowedRoot allowedRoot) {
        this(classLoader, allowedRoot, DEFAULT_MAX_RESOLUTION_BYTES);
    }

    /**
     * 绑定显式读取预算；Provider 必须在构造完整字节数组前执行该门禁。
     */
    public ClasspathDocumentSourceProvider(
            ClassLoader classLoader,
            AllowedRoot allowedRoot,
            long maxResolutionBytes) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
        this.allowedRoot = Objects.requireNonNull(allowedRoot, "allowedRoot");
        if (maxResolutionBytes <= 0L) {
            throw new IllegalArgumentException(
                    "maxResolutionBytes must be > 0");
        }
        this.maxResolutionBytes = maxResolutionBytes;
    }

    /** 精确解析一个 classpath 文档。 */
    @Override
    public SourceResolutionResult resolve(
            SourceReference reference,
            SourceResolutionContext context) {
        Objects.requireNonNull(context, "context");
        try {
            String path = classpathPath(reference);
            Map<String, URL> matches = new LinkedHashMap<String, URL>();
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                putUnique(path, resources.nextElement(), matches);
            }
            if (matches.isEmpty() || path.endsWith("/")) {
                return failed(reference, "source.classpath.not-found");
            }
            if (matches.size() != 1) {
                return failed(reference, "source.classpath.duplicate");
            }
            return SourceResolutionResults.resolvedSingle(
                    document(
                            reference.value(),
                            matches.values().iterator().next(),
                            path,
                            maxResolutionBytes),
                    Collections.<Diagnostic>emptyList());
        } catch (DuplicateSourceException failure) {
            return failed(reference, "source.classpath.duplicate");
        } catch (PathEscapeException failure) {
            return failed(reference, "source.classpath.path-escape");
        } catch (SourceBudgetExceededException failure) {
            return failed(reference, "source.classpath.byte-budget");
        } catch (RuntimeException failure) {
            return failed(reference, "source.classpath.invalid");
        } catch (IOException failure) {
            return failed(reference, "source.classpath.unreadable");
        }
    }

    /**
     * 在 exploded directory 与 jar 两种部署形态中递归展开文件集并稳定排序。
     */
    @Override
    public SourceResolutionResult resolveFileSet(
            SourceReference reference,
            SourceResolutionContext context) {
        Objects.requireNonNull(context, "context");
        try {
            String prefix = classpathPath(reference);
            Map<String, URL> resources = new LinkedHashMap<String, URL>();
            collectFromNamedResources(prefix, resources);
            collectFromClassPath(prefix, resources);
            if (resources.isEmpty()) {
                return failed(reference, "source.classpath.fileset-empty");
            }

            List<String> paths = new ArrayList<String>(resources.keySet());
            Collections.sort(paths);
            List<DocumentSource> documents = new ArrayList<DocumentSource>();
            long remainingBytes = maxResolutionBytes;
            for (String path : paths) {
                DocumentSource source = document(
                        CLASSPATH_SCHEME + ":" + path,
                        resources.get(path),
                        path,
                        remainingBytes);
                documents.add(source);
                remainingBytes -= source.content().length;
            }
            return SourceResolutionResults.resolvedFileSet(
                    documents,
                    Collections.<Diagnostic>emptyList());
        } catch (DuplicateSourceException failure) {
            return failed(reference, "source.classpath.fileset-duplicate");
        } catch (PathEscapeException failure) {
            return failed(reference, "source.classpath.fileset-path-escape");
        } catch (SourceBudgetExceededException failure) {
            return failed(reference, "source.classpath.fileset-byte-budget");
        } catch (RuntimeException failure) {
            return failed(reference, "source.classpath.fileset-invalid");
        } catch (IOException failure) {
            return failed(reference, "source.classpath.fileset-unreadable");
        }
    }

    /** 收集 ClassLoader 对指定前缀直接暴露的目录或 jar URL。 */
    private void collectFromNamedResources(
            String prefix,
            Map<String, URL> output) throws IOException {
        Enumeration<URL> urls = classLoader.getResources(prefix);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if ("file".equalsIgnoreCase(url.getProtocol())) {
                File directory = new File(URI.create(url.toExternalForm()));
                collectDirectory(prefix, directory, output);
            } else if ("jar".equalsIgnoreCase(url.getProtocol())) {
                JarURLConnection connection =
                        (JarURLConnection) url.openConnection();
                collectJar(prefix, connection.getJarFile(), output);
            }
        }
    }

    /**
     * 补充扫描 ClassLoader 根和 java.class.path，覆盖未返回目录 URL 的实现。
     */
    private void collectFromClassPath(
            String prefix,
            Map<String, URL> output) throws IOException {
        List<URL> roots = new ArrayList<URL>();
        if (classLoader instanceof URLClassLoader) {
            Collections.addAll(
                    roots,
                    ((URLClassLoader) classLoader).getURLs());
        }
        if (classLoader == ClassLoader.getSystemClassLoader()
                || classLoader == Thread.currentThread().getContextClassLoader()) {
            String[] entries = System.getProperty("java.class.path", "")
                    .split(File.pathSeparator);
            for (String entry : entries) {
                if (!entry.trim().isEmpty()) {
                    roots.add(new File(entry).toURI().toURL());
                }
            }
        }

        for (URL root : roots) {
            if (!"file".equalsIgnoreCase(root.getProtocol())) {
                continue;
            }
            File file = new File(URI.create(root.toExternalForm()));
            if (file.isDirectory()) {
                collectDirectory(
                        prefix,
                        new File(file, prefix),
                        output);
            } else if (file.isFile() && file.getName().endsWith(".jar")) {
                JarFile jar = new JarFile(file);
                try {
                    collectJar(prefix, jar, output);
                } finally {
                    jar.close();
                }
            }
        }
    }

    /** 递归读取目录中的 XML/YAML 文档。 */
    private static void collectDirectory(
            String prefix,
            File directory,
            Map<String, URL> output) throws IOException {
        Path directoryPath = directory.toPath();
        if (Files.isSymbolicLink(directoryPath)) {
            throw new PathEscapeException(directory.toString());
        }
        if (!directory.isDirectory()) {
            return;
        }
        List<File> files = new ArrayList<File>();
        walk(directory, files);
        Collections.sort(files);
        for (File file : files) {
            String relative = directory.toURI().relativize(file.toURI()).getPath();
            String path = appendPath(prefix, relative);
            if (isSupported(path)) {
                putUnique(path, file.toURI().toURL(), output);
            }
        }
    }

    /** 使用显式栈递归收集普通文件。 */
    private static void walk(File directory, List<File> output) {
        File[] children = directory.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (Files.isSymbolicLink(child.toPath())) {
                throw new PathEscapeException(child.toString());
            }
            if (child.isDirectory()) {
                walk(child, output);
            } else if (child.isFile()) {
                output.add(child);
            }
        }
    }

    /** 从 jar 中收集指定前缀下的支持格式 entry。 */
    private static void collectJar(
            String prefix,
            JarFile jar,
            Map<String, URL> output) throws IOException {
        String normalizedPrefix = prefix.endsWith("/")
                ? prefix
                : prefix + "/";
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!entry.isDirectory()
                    && name.startsWith(normalizedPrefix)
                    && isSupported(name)) {
                putUnique(
                        name,
                        new URL("jar:" + new File(jar.getName()).toURI().toURL()
                                + "!/" + name),
                        output);
            }
        }
    }

    /** 同一 SourceId 只能对应一个物理资源，重复来源必须 fail-closed。 */
    private static void putUnique(
            String path,
            URL resource,
            Map<String, URL> output) {
        URL previous = output.get(path);
        if (previous != null
                && !previous.toExternalForm().equals(resource.toExternalForm())) {
            throw new DuplicateSourceException(path);
        }
        output.put(path, resource);
    }

    /** 标识同一逻辑 SourceId 映射到多个物理资源的安全失败。 */
    private static final class DuplicateSourceException
            extends IllegalArgumentException {
        private DuplicateSourceException(String path) {
            super("duplicate classpath source: " + path);
        }
    }

    /**
     * 构造不可变 DocumentSource；文件资源先执行真实路径边界，再按预算读取。
     */
    private DocumentSource document(
            String sourceId,
            URL resource,
            String path,
            long maxBytes) throws IOException {
        validatePhysicalResource(resource, path);
        byte[] bytes = readAll(resource.openStream(), maxBytes);
        URI uri = URI.create(sourceId);
        return new DocumentSource(
                sourceId,
                uri,
                format(path),
                allowedRoot,
                bytes,
                sha256(bytes));
    }

    /**
     * file: 资源必须解析到当前 ClasspathRoot 对应 AllowedRoot 的真实后代。
     *
     * <p>逻辑 URI 门禁无法识别符号链接，因此这里同时拒绝路径中的任何
     * symlink，并用 real path 再做一次边界比较；jar entry 不存在此类文件系统
     * 跳转，继续使用既有逻辑边界。</p>
     */
    private void validatePhysicalResource(URL resource, String path)
            throws IOException {
        if (!"file".equalsIgnoreCase(resource.getProtocol())) {
            return;
        }
        Path candidate = new File(URI.create(resource.toExternalForm()))
                .toPath().toAbsolutePath().normalize();
        Path classpathRoot = classpathRoot(candidate, path);
        Path physicalAllowedRoot = classpathRoot
                .resolve(classpathLocation(allowedRoot.uri()))
                .normalize();
        if (!physicalAllowedRoot.startsWith(classpathRoot)) {
            throw new PathEscapeException(candidate.toString());
        }

        rejectSymbolicLinks(classpathRoot, physicalAllowedRoot);
        rejectSymbolicLinks(classpathRoot, candidate);

        Path rootReal = classpathRoot.toRealPath();
        Path allowedReal = physicalAllowedRoot.toRealPath();
        Path candidateReal = candidate.toRealPath();
        if (!allowedReal.startsWith(rootReal)
                || !candidateReal.startsWith(allowedReal)) {
            throw new PathEscapeException(candidate.toString());
        }
    }

    /** 根据逻辑 classpath 路径从物理候选反推 ClasspathRoot。 */
    private static Path classpathRoot(Path candidate, String path) {
        Path root = candidate;
        String normalized = path.replace('\\', '/');
        String[] segments = normalized.split("/");
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            root = root.getParent();
            if (root == null) {
                throw new PathEscapeException(candidate.toString());
            }
        }
        return root.toAbsolutePath().normalize();
    }

    /** 返回 AllowedRoot 的相对 classpath 位置。 */
    private static String classpathLocation(URI uri) {
        String value = uri.isOpaque()
                ? uri.getSchemeSpecificPart()
                : uri.getPath();
        String normalized = value == null ? "" : value.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    /**
     * 从 ClasspathRoot 的第一个后代开始逐段拒绝符号链接，避免逃逸与循环。
     */
    private static void rejectSymbolicLinks(Path classpathRoot, Path candidate) {
        Path normalizedRoot = classpathRoot.toAbsolutePath().normalize();
        Path normalizedCandidate = candidate.toAbsolutePath().normalize();
        if (!normalizedCandidate.startsWith(normalizedRoot)) {
            throw new PathEscapeException(candidate.toString());
        }
        Path relative = normalizedRoot.relativize(normalizedCandidate);
        Path current = normalizedRoot;
        for (Path segment : relative) {
            current = current.resolve(segment);
            if (Files.isSymbolicLink(current)) {
                throw new PathEscapeException(current.toString());
            }
        }
    }

    /** 校验 classpath scheme、安全根和规范路径。 */
    private String classpathPath(SourceReference reference) {
        SourceReference checked = Objects.requireNonNull(reference, "reference");
        URI uri = URI.create(checked.value());
        if (!CLASSPATH_SCHEME.equalsIgnoreCase(uri.getScheme())
                || !allowedRoot.contains(uri)) {
            throw new IllegalArgumentException("reference is outside classpath root");
        }
        String path = uri.getSchemeSpecificPart();
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.isEmpty() || path.contains("..")) {
            throw new IllegalArgumentException("invalid classpath path");
        }
        return path;
    }

    /** 根据文件扩展名选择明确 Frontend 格式。 */
    private static DocumentFormat format(String path) {
        String lower = path.toLowerCase(java.util.Locale.ROOT);
        if (lower.endsWith(".xml")) {
            return DocumentFormat.XML;
        }
        if (lower.endsWith(".yaml") || lower.endsWith(".yml")) {
            return DocumentFormat.YAML;
        }
        throw new IllegalArgumentException("unsupported document format: " + path);
    }

    /** 判断文件是否属于生产 Frontend 支持范围。 */
    private static boolean isSupported(String path) {
        String lower = path.toLowerCase(java.util.Locale.ROOT);
        return lower.endsWith(".xml")
                || lower.endsWith(".yaml")
                || lower.endsWith(".yml");
    }

    /** 拼接文件集前缀和相对路径并统一分隔符。 */
    private static String appendPath(String prefix, String relative) {
        String left = prefix.endsWith("/")
                ? prefix.substring(0, prefix.length() - 1)
                : prefix;
        return left + "/" + relative.replace('\\', '/');
    }

    /**
     * 在流式读取期间执行硬字节上限；超过上限的字节永不写入增长缓冲区。
     */
    private static byte[] readAll(InputStream input, long maxBytes)
            throws IOException {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            long total = 0L;
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (count <= 0) {
                    continue;
                }
                if (total > maxBytes - count) {
                    throw new SourceBudgetExceededException(maxBytes);
                }
                output.write(buffer, 0, count);
                total += count;
            }
            return output.toByteArray();
        } finally {
            input.close();
        }
    }

    /** 文件系统真实路径越界或 symlink 出现时的 fail-closed 标记。 */
    private static final class PathEscapeException
            extends IllegalArgumentException {
        private PathEscapeException(String path) {
            super("classpath physical path escapes allowed root: " + path);
        }
    }

    /** Provider 在完整分配 Source 内容前触发的流式字节预算失败。 */
    private static final class SourceBudgetExceededException
            extends IOException {
        private SourceBudgetExceededException(long maxBytes) {
            super("classpath source byte budget exceeded: " + maxBytes);
        }
    }

    /** 计算固定小写 SHA-256。 */
    private static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] value = digest.digest(bytes);
            StringBuilder text = new StringBuilder(value.length * 2);
            for (byte item : value) {
                int unsigned = item & 0xff;
                if (unsigned < 16) {
                    text.append('0');
                }
                text.append(Integer.toHexString(unsigned));
            }
            return text.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    /** 将 Provider 失败收敛为稳定业务 Diagnostic。 */
    private static SourceResolutionResult failed(
            SourceReference reference,
            String messageKey) {
        SourceRef sourceRef = new SourceRef(
                reference == null ? "classpath:<unknown>" : reference.value(),
                0,
                0,
                "/source");
        Diagnostic diagnostic = new Diagnostic(
                DiagnosticCode.MIX_SOURCE_NOT_FOUND,
                DiagnosticSeverity.ERROR,
                messageKey,
                null,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                "确认 classpath 资源存在且位于允许根内",
                "ClasspathDocumentSourceProvider");
        return SourceResolutionResults.failed(
                Collections.singletonList(diagnostic));
    }
}
