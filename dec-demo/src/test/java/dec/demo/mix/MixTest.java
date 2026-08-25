package dec.demo.mix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.context.parse.xml.exception.XMLParseException;
import dec.core.context.EngineContext;
import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.data.BaseData;
import dec.core.context.data.ModelData;
import dec.core.context.model.ActionKey;
import dec.core.context.model.BusinessScopeKey;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DirectoryKey;
import dec.core.context.model.InformationKey;
import dec.core.context.model.RuleViewKey;
import dec.core.context.model.SystemKey;
import dec.core.model.utils.DataUtil;
import dec.core.starter.common.ConfigUtil;
import dec.external.datasource.sql.datasource.DBDataSource;
import java.io.File;
import java.net.URLClassLoader;
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
        loadMixConfigurationFromTestResources();

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
        loadMixConfigurationFromTestResources();
        EngineContext engineContext = DataUtil.getEngineContext();
        assertSame(engineContext,
                ConfigManager.getInstance().getInstalledConfigInfo()
                        .getEngineContext());
        CompiledModelSet model = engineContext.compiledModelSet();
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

        BusinessScopeKey business = new BusinessScopeKey("order-payment");
        DirectoryKey ordered = new DirectoryKey(business, "ordered");
        assertTrue(model.typedRegistries().businessScopes().find(business).isPresent());
        assertTrue(model.typedRegistries().directories().find(ordered).isPresent());
        assertTrue(model.typedRegistries().actions()
                .find(new ActionKey(ordered, "saveOrder"))
                .isPresent());
        assertTrue(model.typedRegistries().produces().size() > 0);
    }

    /**
     * AC-P2-SYSTEM-RULEVIEW-003：多个 system-file 必须经 ConfigUtil 公共入口统一编译。
     * 来源：project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md#9-验收标准。
     */
    @Test
    void loadsMultipleSystemFilesThroughPublicConfigEntry() throws Exception {
        loadConfigurationFromTestResources(
                "classpath:mix/orm-config-multi-system.xml");

        CompiledModelSet model = DataUtil.getEngineContext().compiledModelSet();
        assertTrue(model.sourceManifest().sources().stream().anyMatch(source ->
                source.sourceId().endsWith("mix/system/systems.xml")));
        assertTrue(model.sourceManifest().sources().stream().anyMatch(source ->
                source.sourceId().endsWith("mix/system/extra-systems.xml")));
        assertEquals(5, model.typedRegistries().systems().size());
        assertTrue(model.typedRegistries().systems()
                .find(new SystemKey("audit"))
                .isPresent());
        assertTrue(model.typedRegistries().businessScopes()
                .find(new BusinessScopeKey("order-payment"))
                .isPresent());
    }

    /**
     * AC-P2-SYSTEM-RULEVIEW-004：候选配置编译失败时不得替换已安装对象。
     * 来源：project_doc/version/V_1.0/doc/COMPILER/COMPILER_design.md#53-失败路径。
     */
    @Test
    void failedCompilationKeepsInstalledConfigAndEngineContext() throws Exception {
        loadMixConfigurationFromTestResources();
        ConfigInfo installedConfig = ConfigManager.getInstance().getInstalledConfigInfo();
        EngineContext installedContext = DataUtil.getEngineContext();

        XMLParseException failure = assertThrows(
                XMLParseException.class,
                () -> loadConfigurationFromTestResources(
                        "classpath:mix/orm-config-invalid-duplicate-system-file.xml"));

        assertTrue(failure.getMessage().contains("配置编译失败"));
        assertSame(installedConfig, ConfigManager.getInstance().getInstalledConfigInfo());
        assertSame(installedContext, DataUtil.getEngineContext());
    }

    /** 只隔离测试资源来源，业务加载仍然只调用 ConfigUtil 公共入口。 */
    private static void loadMixConfigurationFromTestResources() throws Exception {
        loadConfigurationFromTestResources("classpath:mix/orm-config.xml");
    }

    /** 在测试资源 ClassLoader 中通过唯一公共入口加载指定配置。 */
    private static void loadConfigurationFromTestResources(String configPath)
            throws Exception {
        ClassLoader thread = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader testResources = new URLClassLoader(
                new java.net.URL[] {
                        new File("target/test-classes").toURI().toURL()
                },
                null)) {
            Thread.currentThread().setContextClassLoader(testResources);
            ConfigUtil.addDataSourceConfig("MySQL", DBDataSource.class.getName());
            ConfigUtil.parseConfigInfo(configPath);
        } finally {
            Thread.currentThread().setContextClassLoader(thread);
        }
    }
}
