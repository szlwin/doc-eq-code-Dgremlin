package dec.core.compiler.canonical.xml;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendOptions;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.DocumentSource;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * T04 XML Frontend Oracle 的共享构造和外部访问探针。
 */
final class XmlFrontendTestSupport {
    private static final String FRONTEND_CLASS =
            "dec.core.compiler.canonical.xml.SecureXmlDocumentFrontend";
    private static final String LIMITS_CLASS =
            "dec.core.compiler.canonical.xml.XmlFrontendLimits";

    private XmlFrontendTestSupport() {
        throw new AssertionError("No instances");
    }

    /**
     * 通过反射加载目标 Frontend，使生产类缺失时测试仍能完成 Java 8 编译并形成有效 RED。
     */
    static FrontendHarness frontend() {
        try {
            AtomicInteger attempts = new AtomicInteger();
            Consumer<String> observer = observer(attempts);
            Class<?> type = Class.forName(FRONTEND_CLASS);
            Constructor<?> constructor = type.getDeclaredConstructor(Consumer.class);
            constructor.setAccessible(true);
            DocumentFrontend frontend = (DocumentFrontend) constructor.newInstance(observer);
            return new FrontendHarness(frontend, attempts);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(
                    "T04 secure XML Frontend must exist with the probe constructor",
                    failure);
        }
    }

    /**
     * 使用小型可注入预算构造 Frontend，避免通过真实 OOM 验证资源门禁。
     */
    static FrontendHarness frontendWithLimits(
            int maxDocumentBytes,
            int maxElementDepth,
            int maxNodeCount,
            long maxCumulativeNodePathChars,
            int maxAttributesPerElement,
            int maxDirectTextCharsPerElement,
            long maxCumulativeDirectTextChars) {
        try {
            AtomicInteger attempts = new AtomicInteger();
            Consumer<String> observer = observer(attempts);
            Class<?> frontendType = Class.forName(FRONTEND_CLASS);
            Class<?> limitsType = Class.forName(LIMITS_CLASS);
            Constructor<?> limitsConstructor = limitsType.getDeclaredConstructor(
                    int.class,
                    int.class,
                    int.class,
                    long.class,
                    int.class,
                    int.class,
                    long.class);
            limitsConstructor.setAccessible(true);
            Object limits = limitsConstructor.newInstance(
                    maxDocumentBytes,
                    maxElementDepth,
                    maxNodeCount,
                    maxCumulativeNodePathChars,
                    maxAttributesPerElement,
                    maxDirectTextCharsPerElement,
                    maxCumulativeDirectTextChars);
            Constructor<?> frontendConstructor = frontendType.getDeclaredConstructor(
                    Consumer.class,
                    limitsType);
            frontendConstructor.setAccessible(true);
            DocumentFrontend frontend = (DocumentFrontend) frontendConstructor.newInstance(
                    observer,
                    limits);
            return new FrontendHarness(frontend, attempts);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(
                    "T04 XML resource limits and injectable constructor must exist",
                    failure);
        }
    }

    /**
     * 创建只记录外部资源解析尝试次数的测试探针。
     */
    private static Consumer<String> observer(final AtomicInteger attempts) {
        return new Consumer<String>() {
            @Override
            public void accept(String location) {
                attempts.incrementAndGet();
            }
        };
    }

    /**
     * 创建位于固定允许根内的不可变 XML Source。
     */
    static DocumentSource xmlSource(String content) {
        return source(content, DocumentFormat.XML);
    }

    /**
     * 创建指定格式的固定 Source，用于格式边界负向测试。
     */
    static DocumentSource source(String content, DocumentFormat format) {
        URI root = URI.create("file:/workspace/config/");
        URI uri = URI.create("file:/workspace/config/root.xml");
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
    static FrontendResult parse(FrontendHarness harness, DocumentSource source) {
        return parse(harness, source, new FrontendOptions("1.0"));
    }

    /**
     * 使用显式 FrontendOptions 执行一次解析，支持 null 选项负向 Oracle。
     */
    static FrontendResult parse(
            FrontendHarness harness,
            DocumentSource source,
            FrontendOptions options) {
        return harness.frontend().parse(source, options);
    }

    /**
     * 同时暴露 Frontend 和外部访问计数的测试值对象。
     */
    static final class FrontendHarness {
        private final DocumentFrontend frontend;
        private final AtomicInteger externalAccessAttempts;

        private FrontendHarness(
                DocumentFrontend frontend,
                AtomicInteger externalAccessAttempts) {
            this.frontend = frontend;
            this.externalAccessAttempts = externalAccessAttempts;
        }

        DocumentFrontend frontend() {
            return frontend;
        }

        int externalAccessAttempts() {
            return externalAccessAttempts.get();
        }
    }
}
