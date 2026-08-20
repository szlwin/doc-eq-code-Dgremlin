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
import dec.demo.system.dom.Order;
import dec.demo.system.dom.OrderDetail;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
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
    private static final String LEGACY_ORDER_RULE = "dev09-save-Order";
    private static final long DEV09_USER_ID = 909001L;

    /**
     * 真实 systems.xml 必须产生稳定发布事实，并发布业务 owner 对共享模型的精确 READ/WRITE 权限。
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

        // order System 真实拥有 OrderInfo.status 的读写权限，READ/WRITE 必须绑定同一 trusted root。
        CompiledModelAccessRule orderRead = rule(
                context,
                key("order", "status", AccessOperation.READ));
        CompiledModelAccessRule orderWrite = rule(
                context,
                key("order", "status", AccessOperation.WRITE));
        assertNotNull(orderRead.runtimeBindingPlan());
        assertNotNull(orderWrite.runtimeBindingPlan());
        assertEquals(orderRead.runtimeBindingPlan(), orderWrite.runtimeBindingPlan());

        // payment System 只写 payInfo；禁止把 payment/status/WRITE 当成真实业务权限。
        CompiledModelAccessRule paymentRead = rule(
                context,
                key("payment", "payInfo", AccessOperation.READ));
        CompiledModelAccessRule paymentWrite = rule(
                context,
                key("payment", "payInfo", AccessOperation.WRITE));
        assertNotNull(paymentRead.runtimeBindingPlan());
        assertNotNull(paymentWrite.runtimeBindingPlan());
        assertEquals(paymentRead.runtimeBindingPlan(), paymentWrite.runtimeBindingPlan());

        assertTrue(context.viewMaterializationIndex().find(new ViewKey("OrderInfo")).isPresent());
    }

    /**
     * 在 MySQL P0 中使用真实 Container 执行 order/status READ/WRITE；WRITE 后再次 READ 与数据库都必须看到新值。
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
            ModelAccessRuleKey readKey = key("order", "status", AccessOperation.READ);
            ModelAccessRuleKey writeKey = key("order", "status", AccessOperation.WRITE);
            CompiledModelAccessRule writeRule = rule(context, writeKey);

            // 使用真实 Order 聚合提供 legacy View 所需 relation shape；one-to-many 必须保持 Collection 语义。
            Order originOrder = new Order();
            originOrder.setUserId(Long.valueOf(DEV09_USER_ID));
            originOrder.setStatus(Integer.valueOf(1));
            originOrder.setOrderDetailList(Collections.singletonList(new OrderDetail()));

            RuntimeModelExecutionRoot root = RuntimeModelExecutionRoots.production(
                    context,
                    ProductionContainerKind.SYNCHRONIZED);
            try {
                // ruleName 指向专用真实 insert rule；它仍走 legacy Container/MySQL，只排除与 status 无关的集合写规则。
                RuntimeModelLoadResult load = root.load(RuntimeModelLoadRequest.of(
                        writeRule.runtimeBindingPlan(),
                        originOrder,
                        LEGACY_ORDER_RULE,
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

                    // 独立 JDBC 查询证明真实 Container effect 已写入 MySQL，而不是只改了内存 ModelData。
                    assertOrderStatusPersisted(DEV09_USER_ID, 2);
                } finally {
                    composition.close();
                }
            } finally {
                root.close();
            }
        }
    }

    /** 使用独立连接验证 DEV-09 唯一业务键对应的订单状态已真实落库。 */
    private static void assertOrderStatusPersisted(long userId, int expectedStatus) throws Exception {
        try (Connection connection = DriverManager.getConnection(
                System.getenv("DEC_MYSQL_URL"),
                System.getenv("DEC_MYSQL_USER"),
                System.getenv("DEC_MYSQL_PASSWORD"));
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT o_status FROM order_info WHERE o_userId = ? ORDER BY o_id DESC LIMIT 1")) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "DEV-09 Container WRITE 未产生 order_info 数据库记录");
                assertEquals(expectedStatus, resultSet.getInt(1));
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

    /** 构造 System 下 OrderInfo 精确 path/op 授权 Key，禁止裸名称或 owner fallback。 */
    private static ModelAccessRuleKey key(
            String systemName,
            String path,
            AccessOperation operation) {
        return ModelAccessRuleKey.of(
                new SystemKey(systemName),
                TargetKey.of(new ViewKey("OrderInfo")),
                ModelPath.of(path),
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
