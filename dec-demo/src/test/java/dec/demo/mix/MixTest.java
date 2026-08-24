package dec.demo.mix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import dec.core.context.data.BaseData;
import dec.core.context.data.ModelData;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.InformationKey;
import dec.core.context.model.RuleViewKey;
import dec.core.context.model.SystemKey;
import dec.core.model.utils.DataUtil;
import dec.core.starter.CompilerBootstrap;
import dec.core.starter.common.ConfigUtil;
import dec.external.datasource.sql.datasource.DBDataSource;
import java.io.File;
import java.net.URLClassLoader;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 验证 mix 配置可以通过不暴露 ConfigInfo 的公共入口完成加载。
 */
public class MixTest {

    /**
     * AC-P2-SYSTEM-RULEVIEW-002：加载入口只接收配置路径，加载后 Data 和 View 可以直接使用。
     * 完整 System、Information 和 Business 编译语义由 MixCompilerRegressionTest 覆盖。
     */
    @Test
    void loadsMixConfigurationAndCreatesDeclaredModels() throws Exception {
        ConfigUtil.addDataSourceConfig("MySQL", DBDataSource.class.getName());
        ConfigUtil.parseConfigInfo("classpath:mix/orm-config.xml");

        ModelData order = DataUtil.createViewData("OrderInfo");
        assertEquals("OrderInfo", order.getName());
        assertNotNull(order.getViewInfo());
        assertTrue(order.getValues().containsKey("totalPrice"));
        assertTrue(order.getValues().containsKey("orderDetailList"));
        assertTrue(order.getValues().containsKey("payInfo"));

        ModelData userView = DataUtil.createViewData("UserInfo");
        assertEquals("UserInfo", userView.getName());
        assertTrue(userView.getValues().containsKey("activeTime"));
        assertTrue(userView.getValues().containsKey("certified"));

        BaseData user = DataUtil.createBaseData("user");
        assertEquals("user", user.getName());
        assertTrue(user.getValues().containsKey("name"));
        assertTrue(user.getValues().containsKey("status"));
    }

    /**
     * AC-P2-SYSTEM-RULEVIEW-003：根配置必须发现 test resources 中的 systems.xml，
     * 并按 (system, name) 复合身份发布 System、RuleView 和 Information。
     */
    @Test
    void loadsSystemsFileFromTestResources() throws Exception {
        CompilationResult result;
        // 隔离到 test 输出目录，避免 main/test 两套同名 mix 资源造成来源歧义。
        try (URLClassLoader testResources = new URLClassLoader(
                new java.net.URL[] {
                        new File("target/test-classes").toURI().toURL()
                },
                null)) {
            result = CompilerBootstrap.builder()
                    .classLoader(testResources)
                    .allowedRoot("classpath:mix/")
                    .publisher(alwaysPublish())
                    .build()
                    .compileAndPublish(
                            new SourceReference("classpath:mix/orm-config.xml"),
                            new CompilationOptions("1.0", "mix-test-systems"),
                            Optional.<EngineContext>empty());
        }

        assertEquals(
                CompilationStatus.PUBLISHED,
                result.status(),
                result.diagnostics().toString());
        CompiledModelSet model = ((PublishedCompilationResult) result).modelSet();
        assertTrue(model.sourceManifest().sources().stream().anyMatch(source ->
                source.sourceId().endsWith("mix/system/systems.xml")));

        SystemKey user = new SystemKey("user");
        SystemKey order = new SystemKey("order");
        SystemKey payment = new SystemKey("payment");
        SystemKey common = new SystemKey("common");
        assertEquals(4, model.typedRegistries().systems().size());
        assertTrue(model.typedRegistries().systems().find(user).isPresent());
        assertTrue(model.typedRegistries().systems().find(order).isPresent());
        assertTrue(model.typedRegistries().systems().find(payment).isPresent());
        assertTrue(model.typedRegistries().systems().find(common).isPresent());

        assertTrue(model.typedRegistries().ruleViews()
                .find(new RuleViewKey(order, "save-Order"))
                .isPresent());
        assertTrue(model.typedRegistries().ruleViews()
                .find(new RuleViewKey(payment, "isPaySuccess"))
                .isPresent());
        assertTrue(model.typedRegistries().information()
                .find(new InformationKey(order, "payable"))
                .isPresent());
        assertTrue(model.typedRegistries().information()
                .find(new InformationKey(common, "paySuccess"))
                .isPresent());
    }

    private static ContextPublisher alwaysPublish() {
        return new ContextPublisher() {
            @Override
            public PublicationResult publish(
                    Optional<EngineContext> expectedCurrent,
                    EngineContext candidate) {
                return new PublicationResult() {
                    @Override
                    public PublicationStatus status() {
                        return PublicationStatus.PUBLISHED;
                    }
                };
            }
        };
    }
}
