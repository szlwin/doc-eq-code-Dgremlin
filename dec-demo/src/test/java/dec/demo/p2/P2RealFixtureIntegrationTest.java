package dec.demo.p2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.api.CompilationResult;
import dec.core.compiler.api.CompilationStatus;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.PublicationStatus;
import dec.core.compiler.api.PublishedCompilationResult;
import dec.core.compiler.source.SourceReference;
import dec.core.context.EngineContext;
import dec.core.context.model.AccessOperation;
import dec.core.context.model.CompiledModelAccessRule;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.ModelPath;
import dec.core.context.model.SystemKey;
import dec.core.context.model.TargetKey;
import dec.core.context.model.ViewKey;
import dec.core.context.runtime.ProtectedAccessInvocation;
import dec.core.context.runtime.ProtectedAccessResult;
import dec.core.context.runtime.ProtectedInvocationId;
import dec.core.context.runtime.RuntimeCollectionCursorId;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.model.runtime.ProductionContainerKind;
import dec.core.model.runtime.RuntimeModelAccessScope;
import dec.core.model.runtime.RuntimeModelExecutionRoot;
import dec.core.model.runtime.RuntimeModelExecutionRoots;
import dec.core.model.runtime.RuntimeModelLoadRequest;
import dec.core.model.runtime.RuntimeModelLoadResult;
import dec.core.model.runtime.RuntimeModelScopeResult;
import dec.core.starter.CompilerBootstrap;
import dec.core.starter.access.ProtectedAccessComposition;
import dec.core.starter.access.ProtectedAccessCompositionResult;
import dec.core.starter.access.ProtectedAccessRuntimeFactory;
import dec.demo.support.DemoMySqlTestSupport;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * DEV-09 真实 fixture 收口：证明 P2 配置能够从 Compiler 一直进入 MODEL/STARTER 生产链。
 */
class P2RealFixtureIntegrationTest {
    private static final String ROOT = "classpath:mix/orm-config.xml";
    private static final String OPTIONS = "p2-dev09-real-fixture";

    /**
     * 真实 systems.xml 必须产生稳定发布事实，并发布 payment/OrderInfo/status 的精确 READ/WRITE 权限。
     */
    @Test
    @DisplayName("CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001")
    void realFixtureCompilesDeterministicallyWithExactAccessBindings() {
        PublishedCompilationResult first = compileRealFixture();
        PublishedCompilationResult second = compileRealFixture();

        assertEquals(first.digests(), second.digests());
        assertEquals(first.modelSet().sourceManifest(), second.modelSet().sourceManifest());
        assertTrue(first.modelSet().sourceManifest().sources().size() > 1);

        EngineContext context = first.engineContext();
        CompiledModelAccessRule read = rule(context, key(AccessOperation.READ));
        CompiledModelAccessRule write = rule(context, key(AccessOperation.WRITE));
        assertNotNull(read.runtimeBindingPlan());
        assertNotNull(write.runtimeBindingPlan());
        assertEquals(read.runtimeBindingPlan(), write.runtimeBindingPlan());
        assertTrue(context.viewMaterializationIndex().find(new ViewKey("OrderInfo")).isPresent());
    }

    /**
     * 在 MySQL P0 中使用真实 Container 执行 READ/WRITE；WRITE 后再次 READ 必须看到同一 trusted runtime object 的新值。
     */
    @Test
    @Tag("mysql-it")
    @DisplayName("CASE-P2-TD-SOURCE-TO-READ-WRITE-OPERATION-001")
    void realFixtureFlowsThroughModelAndStarterReadWrite() throws Exception {
        Assumptions.assumeTrue(
                System.getenv("DEC_MYSQL_URL") != null,
                "真实 Container effect 只在 MySQL P0 环境执行");

        // ContainerFactory 仍消费现有 declaration 配置；P2 只替换 authority/compiled context，不提前删除兼容入口。
        try (DemoMySqlTestSupport ignored = DemoMySqlTestSupport.load("system/orm-config.xml")) {
            PublishedCompilationResult published = compileRealFixture();
            EngineContext context = published.engineContext();
            ModelAccessRuleKey readKey = key(AccessOperation.READ);
            ModelAccessRuleKey writeKey = key(AccessOperation.WRITE);
            CompiledModelAccessRule writeRule = rule(context, writeKey);

            Map<String, Object> originData = new LinkedHashMap<String, Object>();
            originData.put("id", Long.valueOf(10001L));
            originData.put("status", Integer.valueOf(1));

            RuntimeModelExecutionRoot root = RuntimeModelExecutionRoots.production(
                    context,
                    ProductionContainerKind.SYNCHRONIZED);
            try {
                RuntimeModelLoadResult load = root.load(RuntimeModelLoadRequest.of(
                        writeRule.runtimeBindingPlan(),
                        originData,
                        "orderList",
                        "con1"));
                assertTrue(load.loaded(), String.valueOf(load.failure()));
                RuntimeModelScopeResult scopeResult = root.accessScope();
                assertTrue(scopeResult.available(), String.valueOf(scopeResult.failure()));
                RuntimeModelAccessScope scope = scopeResult.scope().get();

                ProtectedAccessCompositionResult created =
                        ProtectedAccessRuntimeFactory.production(context).create(scope);
                assertTrue(created.created(), String.valueOf(created.failure()));
                ProtectedAccessComposition composition = created.composition().get();
                try {
                    ProtectedAccessResult read = composition.ruleEntry().invoke(
                            ProtectedAccessInvocation.of(
                                    ProtectedInvocationId.of("dev09-real-read"),
                                    readKey,
                                    scope.frame().frameId(),
                                    scope.frame().ownerResolutionId(),
                                    Optional.<RuntimeCollectionCursorId>empty()));
                    assertTrue(read.allowed(), String.valueOf(read.denial()));
                    assertEquals(RuntimeFactValue.integerValue(1L), read.readValue().get().value());

                    ProtectedAccessResult write = composition.changeEntry().invoke(
                            ProtectedAccessInvocation.write(
                                    ProtectedInvocationId.of("dev09-real-write"),
                                    writeKey,
                                    scope.frame().frameId(),
                                    scope.frame().ownerResolutionId(),
                                    Optional.<RuntimeCollectionCursorId>empty(),
                                    RuntimeFactValue.integerValue(2L)));
                    assertTrue(write.allowed(), String.valueOf(write.denial()));
                    assertTrue(write.writeReceipt().isPresent());

                    // 通过同一 scope 上的再次 READ 验证 WRITE 已写入 trusted runtime object，不暴露 MODEL 包内句柄实现。
                    ProtectedAccessResult reread = composition.ruleEntry().invoke(
                            ProtectedAccessInvocation.of(
                                    ProtectedInvocationId.of("dev09-real-reread"),
                                    readKey,
                                    scope.frame().frameId(),
                                    scope.frame().ownerResolutionId(),
                                    Optional.<RuntimeCollectionCursorId>empty()));
                    assertTrue(reread.allowed(), String.valueOf(reread.denial()));
                    assertEquals(RuntimeFactValue.integerValue(2L), reread.readValue().get().value());
                } finally {
                    composition.close();
                }
            } finally {
                root.close();
            }
        }
    }

    /** 编译一次真实 P2 mix 根并返回唯一 published 事实。 */
    private PublishedCompilationResult compileRealFixture() {
        RecordingPublisher publisher = new RecordingPublisher();
        CompilerBootstrap bootstrap = CompilerBootstrap.builder()
                .classLoader(new UniqueMixFixtureClassLoader(getClass().getClassLoader()))
                .allowedRoot("classpath:mix/")
                .publisher(publisher)
                .build();
        CompilationResult result = bootstrap.compileAndPublish(
                new SourceReference(ROOT),
                new CompilationOptions("1.0", OPTIONS),
                Optional.<EngineContext>empty());
        assertEquals(CompilationStatus.PUBLISHED, result.status(), result.diagnostics().toString());
        PublishedCompilationResult published = (PublishedCompilationResult) result;
        assertSame(publisher.current().get(), published.engineContext());
        return published;
    }

    /** 构造 payment System 下 OrderInfo.status 的精确授权 Key，禁止裸名称 fallback。 */
    private static ModelAccessRuleKey key(AccessOperation operation) {
        return ModelAccessRuleKey.of(
                new SystemKey("payment"),
                TargetKey.of(new ViewKey("OrderInfo")),
                ModelPath.of("status"),
                operation);
    }

    /** 从真实发布 Context 中读取 exact rule，不允许测试侧重新编译策略。 */
    private static CompiledModelAccessRule rule(
            EngineContext context,
            ModelAccessRuleKey key) {
        Optional<CompiledModelAccessRule> rule = context.modelAccessPolicyIndex().find(key);
        assertTrue(rule.isPresent(), "missing compiled access rule: " + key);
        return rule.get();
    }

    /**
     * dec-demo 为兼容历史资源同时在 main/test 输出同一 mix fixture；测试显式选取 classpath 中第一份真实 fixture，
     * 避免测试打包结构制造伪 duplicate，而生产 Compiler 的 duplicate fail-closed 行为保持不变。
     */
    private static final class UniqueMixFixtureClassLoader extends ClassLoader {
        private UniqueMixFixtureClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            Enumeration<URL> resources = super.getResources(name);
            if (!name.startsWith("mix/")) {
                return resources;
            }
            List<URL> uniqueFixture = new ArrayList<URL>(1);
            if (resources.hasMoreElements()) {
                uniqueFixture.add(resources.nextElement());
            }
            return Collections.enumeration(uniqueFixture);
        }
    }

    /** 记录真实 CAS 发布，确保测试读取的是 Publisher 实际暴露的 Context。 */
    private static final class RecordingPublisher implements ContextPublisher {
        private final AtomicReference<EngineContext> current = new AtomicReference<EngineContext>();

        @Override
        public PublicationResult publish(
                Optional<EngineContext> expectedCurrent,
                EngineContext candidate) {
            EngineContext expected = expectedCurrent.orElse(null);
            if (!current.compareAndSet(expected, candidate)) {
                return result(PublicationStatus.CONFLICT);
            }
            return result(PublicationStatus.PUBLISHED);
        }

        /** 返回当前实际发布 Context。 */
        private Optional<EngineContext> current() {
            return Optional.ofNullable(current.get());
        }

        /** 创建稳定发布结果，不向 Compiler 注入额外状态。 */
        private static PublicationResult result(final PublicationStatus status) {
            return new PublicationResult() {
                @Override
                public PublicationStatus status() {
                    return status;
                }
            };
        }
    }
}
