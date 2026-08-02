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

    private XmlFrontendTestSupport() {
        throw new AssertionError("No instances");
    }

    /**
     * 通过反射加载目标 Frontend，使生产类缺失时测试仍能完成 Java 8 编译并形成有效 RED。
     */
    static FrontendHarness frontend() {
        try {
            AtomicInteger attempts = new AtomicInteger();
            Consumer<String> observer = new Consumer<String>() {
                @Override
                public void accept(String location) {
                    attempts.incrementAndGet();
                }
            };
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
        return harness.frontend().parse(source, new FrontendOptions("1.0"));
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
