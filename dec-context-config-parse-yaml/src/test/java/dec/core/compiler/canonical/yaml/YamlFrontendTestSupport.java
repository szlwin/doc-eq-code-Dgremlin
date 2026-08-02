package dec.core.compiler.canonical.yaml;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendOptions;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.DocumentSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * T05 YAML Frontend Oracle 的共享反射构造和固定 Source 工具。
 */
final class YamlFrontendTestSupport {
    private static final String FRONTEND_CLASS =
            "dec.core.compiler.canonical.yaml.SafeYamlDocumentFrontend";
    private static final String LIMITS_CLASS =
            "dec.core.compiler.canonical.yaml.YamlFrontendLimits";

    private YamlFrontendTestSupport() {
        throw new AssertionError("No instances");
    }

    /**
     * 通过反射加载目标 Frontend，使生产类缺失时测试仍可完成 Java 8 编译并形成有效 RED。
     */
    static DocumentFrontend frontend() {
        try {
            Class<?> type = Class.forName(FRONTEND_CLASS);
            Constructor<?> constructor = type.getDeclaredConstructor();
            return (DocumentFrontend) constructor.newInstance();
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(
                    "T05 safe YAML Frontend must exist with a public no-arg constructor",
                    failure);
        }
    }

    /**
     * 使用可注入的小型预算构造 Frontend，避免以真实 OOM 验证资源门禁。
     */
    static DocumentFrontend frontendWithLimits(
            int maxDocumentBytes,
            int maxCodePoints,
            int maxNestingDepth,
            int maxNodeCount,
            long maxCumulativeNodePathChars,
            int maxMappingEntriesPerNode,
            int maxSequenceItemsPerNode,
            int maxScalarCharsPerNode,
            long maxCumulativeScalarChars,
            int maxAliasesForCollections) {
        try {
            Class<?> frontendType = Class.forName(FRONTEND_CLASS);
            Class<?> limitsType = Class.forName(LIMITS_CLASS);
            Constructor<?> limitsConstructor = limitsType.getDeclaredConstructor(
                    int.class,
                    int.class,
                    int.class,
                    int.class,
                    long.class,
                    int.class,
                    int.class,
                    int.class,
                    long.class,
                    int.class);
            limitsConstructor.setAccessible(true);
            Object limits = limitsConstructor.newInstance(
                    maxDocumentBytes,
                    maxCodePoints,
                    maxNestingDepth,
                    maxNodeCount,
                    maxCumulativeNodePathChars,
                    maxMappingEntriesPerNode,
                    maxSequenceItemsPerNode,
                    maxScalarCharsPerNode,
                    maxCumulativeScalarChars,
                    maxAliasesForCollections);
            Constructor<?> frontendConstructor = frontendType.getDeclaredConstructor(
                    limitsType);
            frontendConstructor.setAccessible(true);
            return (DocumentFrontend) frontendConstructor.newInstance(limits);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(
                    "T05 YAML resource limits and injectable constructor must exist",
                    failure);
        }
    }

    /**
     * 读取生产预算常量，确保 Design 冻结值不会被静默修改。
     */
    static long productionLimit(String fieldName) {
        try {
            Class<?> limitsType = Class.forName(LIMITS_CLASS);
            Field field = limitsType.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(null);
            return ((Number) value).longValue();
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(
                    "T05 production YAML limit must exist: " + fieldName,
                    failure);
        }
    }

    /**
     * 创建位于固定允许根内的不可变 YAML Source。
     */
    static DocumentSource yamlSource(String content) {
        return source(content, DocumentFormat.YAML, "root.yaml");
    }

    /**
     * 创建指定格式的固定 Source，用于格式边界与 XML parity 测试。
     */
    static DocumentSource source(
            String content,
            DocumentFormat format,
            String fileName) {
        URI root = URI.create("file:/workspace/config/");
        URI uri = URI.create("file:/workspace/config/" + fileName);
        return new DocumentSource(
                uri.toString(),
                uri,
                format,
                new AllowedRoot(root),
                content.getBytes(StandardCharsets.UTF_8),
                "test-digest-" + Integer.toHexString(content.hashCode()));
    }

    /**
     * 使用固定 schemaVersion 执行一次解析。
     */
    static FrontendResult parse(DocumentFrontend frontend, DocumentSource source) {
        return parse(frontend, source, new FrontendOptions("1.0"));
    }

    /**
     * 使用显式 FrontendOptions 执行一次解析，支持 null 选项负向 Oracle。
     */
    static FrontendResult parse(
            DocumentFrontend frontend,
            DocumentSource source,
            FrontendOptions options) {
        return frontend.parse(source, options);
    }
}
