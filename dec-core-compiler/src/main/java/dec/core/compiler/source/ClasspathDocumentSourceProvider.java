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
    private final ClassLoader classLoader;
    private final AllowedRoot allowedRoot;

    /**
     * 绑定显式 ClassLoader 和安全根；Provider 不读取全局可变配置。
     */
    public ClasspathDocumentSourceProvider(
            ClassLoader classLoader,
            AllowedRoot allowedRoot) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
        this.allowedRoot = Objects.requireNonNull(allowedRoot, "allowedRoot");
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
                            path),
                    Collections.<Diagnostic>emptyList());
        } catch (DuplicateSourceException failure) {
            return failed(reference, "source.classpath.duplicate");
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
            for (String path : paths) {
                documents.add(document(
                        CLASSPATH_SCHEME + ":" + path,
                        resources.get(path),
                        path));
            }
            return SourceResolutionResults.resolvedFileSet(
                    documents,
                    Collections.<Diagnostic>emptyList());
        } catch (DuplicateSourceException failure) {
            return failed(reference, "source.classpath.fileset-duplicate");
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

    /** 构造不可变 DocumentSource，并以原始字节计算真实 SHA-256。 */
    private DocumentSource document(
            String sourceId,
            URL resource,
            String path) throws IOException {
        byte[] bytes = readAll(resource.openStream());
        URI uri = URI.create(sourceId);
        return new DocumentSource(
                sourceId,
                uri,
                format(path),
                allowedRoot,
                bytes,
                sha256(bytes));
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

    /** 关闭输入流并读取完整字节。 */
    private static byte[] readAll(InputStream input) throws IOException {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (count > 0) {
                    output.write(buffer, 0, count);
                }
            }
            return output.toByteArray();
        } finally {
            input.close();
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
