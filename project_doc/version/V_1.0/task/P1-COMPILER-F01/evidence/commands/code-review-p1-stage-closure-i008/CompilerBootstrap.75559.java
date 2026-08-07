package dec.core.starter;

import dec.core.compiler.api.CancellationToken;
import dec.core.compiler.api.CompilationObserver;
import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationResult;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.MonotonicClock;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendRegistry;
import dec.core.compiler.canonical.xml.SecureXmlDocumentFrontend;
import dec.core.compiler.canonical.yaml.SafeYamlDocumentFrontend;
import dec.core.compiler.pass.StandardModelCompilerFactory;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.ClasspathDocumentSourceProvider;
import dec.core.compiler.source.DocumentSourceProvider;
import dec.core.compiler.source.SourcePolicy;
import dec.core.compiler.source.SourceReference;
import dec.core.context.CoreConfigProjection;
import dec.core.context.EngineContext;
import java.net.URI;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 从根 Source 和编译选项开始组装真实生产编译链的一键入口。
 */
public final class CompilerBootstrap {
    private static final String DEFAULT_COMPILER_VERSION =
            "dec-p1-standard-compiler-1.0";
    private final CompilerStarter starter;
    private final DocumentSourceProvider sourceProvider;
    private final FrontendRegistry frontends;
    private final ContextPublisher publisher;
    private final MonotonicClock clock;
    private final CompilationObserver observer;
    private final CancellationToken cancellationToken;

    /** 保存 Builder 已冻结的生产依赖，不持有全局 current Context。 */
    private CompilerBootstrap(Builder builder) {
        AllowedRoot allowedRoot = new AllowedRoot(
                URI.create(requireRoot(builder.allowedRoot)));
        SourcePolicy sourcePolicy = new SourcePolicy(
                Collections.singleton("classpath"),
                allowedRoot,
                builder.maxDepth,
                builder.maxSources,
                builder.maxTotalBytes);
        this.sourceProvider = new ClasspathDocumentSourceProvider(
                builder.classLoader,
                allowedRoot,
                builder.maxTotalBytes);
        this.frontends = standardFrontends();
        this.publisher = builder.publisher;
        this.clock = builder.clock;
        this.observer = builder.observer;
        this.cancellationToken = builder.cancellationToken;
        this.starter = new CompilerStarter(StandardModelCompilerFactory.create(
                sourcePolicy,
                builder.compilerVersion));
    }

    /** 创建提供安全生产默认值的 Builder。 */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 从根 Source、语义选项和显式 CAS 预期一次完成真实编译与发布。
     */
    public CompilationResult compileAndPublish(
            SourceReference root,
            CompilationOptions options,
            Optional<EngineContext> expectedCurrent) {
        CompilationRequest request = new CompilationRequest(
                Objects.requireNonNull(root, "root"),
                sourceProvider,
                frontends,
                Objects.requireNonNull(options, "options"),
                Optional.empty(),
                cancellationToken,
                clock,
                observer);
        PublicationRequest publicationRequest = new PublicationRequest(
                Objects.requireNonNull(expectedCurrent, "expectedCurrent"),
                publisher);
        return starter.compileAndPublish(request, publicationRequest);
    }

    /** 从同一个 Published EngineContext 获取只读 Projection。 */
    public CoreConfigProjection projection(CompilationResult result) {
        return starter.projection(result);
    }

    /** 创建只包含 XML/YAML 两个明确格式映射的不可变 FrontendRegistry。 */
    private static FrontendRegistry standardFrontends() {
        Map<DocumentFormat, DocumentFrontend> values =
                new EnumMap<DocumentFormat, DocumentFrontend>(
                        DocumentFormat.class);
        values.put(DocumentFormat.XML, new SecureXmlDocumentFrontend());
        values.put(DocumentFormat.YAML, new SafeYamlDocumentFrontend());
        final Map<DocumentFormat, DocumentFrontend> frozen =
                Collections.unmodifiableMap(values);
        return new FrontendRegistry() {
            @Override
            public DocumentFrontend require(DocumentFormat format) {
                DocumentFrontend frontend = frozen.get(
                        Objects.requireNonNull(format, "format"));
                if (frontend == null) {
                    throw new IllegalArgumentException(
                            "unsupported frontend format: " + format);
                }
                return frontend;
            }
        };
    }

    /** 规范化允许根，确保文件集前缀比较使用目录边界。 */
    private static String requireRoot(String value) {
        String checked = Objects.requireNonNull(value, "allowedRoot").trim();
        if (!checked.endsWith("/")) {
            checked = checked + "/";
        }
        URI uri = URI.create(checked);
        if (!"classpath".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException(
                    "allowedRoot must use classpath scheme");
        }
        return checked;
    }

    /**
     * 收集可替换基础设施；核心编译组件保持标准生产实现。
     */
    public static final class Builder {
        private ClassLoader classLoader = defaultClassLoader();
        private String allowedRoot;
        private ContextPublisher publisher;
        private MonotonicClock clock = new MonotonicClock() {
            @Override
            public long nanoTime() {
                return System.nanoTime();
            }
        };
        private CompilationObserver observer = new CompilationObserver() {
            @Override
            public void onTiming(CompilationTiming timing) {
                // 生产默认观察器不保存跨请求状态。
            }

            @Override
            public void onStateTransition(SessionStateTransition transition) {
                // 生产默认观察器不保存跨请求状态。
            }
        };
        private CancellationToken cancellationToken = new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        };
        private String compilerVersion = DEFAULT_COMPILER_VERSION;
        private int maxDepth = 16;
        private int maxSources = 1024;
        private long maxTotalBytes = 64L * 1024L * 1024L;

        private Builder() {
        }

        /** 注入应用 ClassLoader。 */
        public Builder classLoader(ClassLoader value) {
            this.classLoader = Objects.requireNonNull(value, "classLoader");
            return this;
        }

        /** 设置唯一允许的 classpath 根。 */
        public Builder allowedRoot(String value) {
            this.allowedRoot = Objects.requireNonNull(value, "allowedRoot");
            return this;
        }

        /** 注入唯一允许的外部 CAS Publisher。 */
        public Builder publisher(ContextPublisher value) {
            this.publisher = Objects.requireNonNull(value, "publisher");
            return this;
        }

        /** 注入单调时钟，供 Deadline 与 Timing 共用。 */
        public Builder clock(MonotonicClock value) {
            this.clock = Objects.requireNonNull(value, "clock");
            return this;
        }

        /** 注入只读 Observer。 */
        public Builder observer(CompilationObserver value) {
            this.observer = Objects.requireNonNull(value, "observer");
            return this;
        }

        /** 注入会话取消令牌。 */
        public Builder cancellationToken(CancellationToken value) {
            this.cancellationToken = Objects.requireNonNull(
                    value,
                    "cancellationToken");
            return this;
        }

        /** 设置参与 Digest 的稳定 Compiler 版本。 */
        public Builder compilerVersion(String value) {
            String checked = Objects.requireNonNull(
                    value,
                    "compilerVersion").trim();
            if (checked.isEmpty()) {
                throw new IllegalArgumentException(
                        "compilerVersion must not be blank");
            }
            this.compilerVersion = checked;
            return this;
        }

        /** 覆盖 Source discovery 的生产预算。 */
        public Builder sourceBudgets(
                int maxDepth,
                int maxSources,
                long maxTotalBytes) {
            if (maxDepth < 0 || maxSources <= 0 || maxTotalBytes <= 0L) {
                throw new IllegalArgumentException(
                        "invalid source discovery budgets");
            }
            this.maxDepth = maxDepth;
            this.maxSources = maxSources;
            this.maxTotalBytes = maxTotalBytes;
            return this;
        }

        /** 校验必填基础设施并创建不可变 Bootstrap。 */
        public CompilerBootstrap build() {
            Objects.requireNonNull(allowedRoot, "allowedRoot");
            Objects.requireNonNull(publisher, "publisher");
            return new CompilerBootstrap(this);
        }

        /** 优先使用线程上下文 ClassLoader，并提供稳定回退。 */
        private static ClassLoader defaultClassLoader() {
            ClassLoader value = Thread.currentThread().getContextClassLoader();
            return value == null
                    ? CompilerBootstrap.class.getClassLoader()
                    : value;
        }
    }
}
