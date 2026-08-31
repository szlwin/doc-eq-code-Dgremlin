package dec.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.context.parse.xml.exception.XMLParseException;
import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.datasource.DataSourceConfigInfo;
import dec.core.context.data.ModelData;
import dec.core.model.utils.DataUtil;
import dec.core.starter.common.ConfigUtil;
import dec.external.datasource.sql.datasource.DBDataSource;
import org.junit.jupiter.api.Test;

/** 验证 P2 统一入口继续兼容没有 System/Business 声明的旧配置。 */
class ConfigUtilCompatibilityTest {

    /** AC-001：每个 ConfigInfo 都拥有独立配置集合。 */
    @Test
    void configInfoInstancesDoNotShareDefinitions() {
        ConfigInfo first = new ConfigInfo();
        ConfigInfo second = new ConfigInfo();
        DataSourceConfigInfo definition = new DataSourceConfigInfo();
        definition.setName("isolated-type");
        definition.setDataSource(DBDataSource.class.getName());

        first.add(Config.DATASOURCE_CONFIG, definition);

        assertSame(definition,
                first.get(Config.DATASOURCE_CONFIG, "isolated-type"));
        assertNull(second.get(Config.DATASOURCE_CONFIG, "isolated-type"));
    }

    /** P2 到 P7 的迁移边界：旧配置只加载 ConfigInfo，不强制进入现代 Compiler。 */
    @Test
    void loadsLegacyConfigurationWithoutSystemDeclarations() throws Exception {
        ConfigUtil.addDataSourceConfig("MySQL", DBDataSource.class.getName());
        ConfigUtil.parseConfigInfo("classpath:model/orm-config.xml");

        ModelData order = DataUtil.createViewData("OrderInfo");
        assertEquals("OrderInfo", order.getName());
        assertTrue(order.getValues().containsKey("totalPrice"));
        assertTrue(order.getValues().containsKey("productList"));
    }

    /** XML 与 YAML 使用同一个业务门面，调用方不创建或接收 ConfigInfo。 */
    @Test
    void loadsLegacyYamlThroughTheUnifiedFacade() throws Exception {
        ConfigUtil.addDataSourceConfig("MySQL", DBDataSource.class.getName());
        ConfigUtil.parseConfigInfo("classpath:yaml/model/orm-config.yaml");

        ModelData order = DataUtil.createViewData("OrderInfo");
        assertEquals("OrderInfo", order.getName());
        assertTrue(order.getValues().containsKey("totalPrice"));
        assertTrue(order.getValues().containsKey("productList"));
    }

    /** P2 不得把包含 System/Business 的现代 YAML 当作旧配置部分安装。 */
    @Test
    void rejectsModernYamlBeforeReplacingTheInstalledConfiguration() throws Exception {
        ConfigUtil.addDataSourceConfig("MySQL", DBDataSource.class.getName());
        ConfigUtil.parseConfigInfo("classpath:model/orm-config.xml");
        ConfigInfo installed = ConfigManager.getInstance().getInstalledConfigInfo();

        ConfigUtil.addDataSourceConfig("MySQL", DBDataSource.class.getName());
        XMLParseException failure = assertThrows(
                XMLParseException.class,
                () -> ConfigUtil.parseConfigInfo(
                        "classpath:yaml/modern/orm-config.yaml"));

        assertTrue(failure.getCause().getMessage().contains("P8"));
        assertSame(installed, ConfigManager.getInstance().getInstalledConfigInfo());
    }

    /** AC-002：XML 语法错误不得污染已经安装的配置。 */
    @Test
    void malformedXmlKeepsTheInstalledConfiguration() throws Exception {
        ConfigUtil.addDataSourceConfig("MySQL", DBDataSource.class.getName());
        ConfigUtil.parseConfigInfo("classpath:model/orm-config.xml");
        ConfigInfo installed = ConfigManager.getInstance().getInstalledConfigInfo();

        ConfigUtil.addDataSourceConfig("MySQL", DBDataSource.class.getName());
        assertThrows(XMLParseException.class,
                () -> ConfigUtil.parseConfigInfo(
                        "classpath:invalid/malformed-config.xml"));

        assertSame(installed, ConfigManager.getInstance().getInstalledConfigInfo());
    }

    /** AC-002：YAML 语法错误与 XML 具有相同的失败原子性。 */
    @Test
    void malformedYamlKeepsTheInstalledConfiguration() throws Exception {
        ConfigUtil.addDataSourceConfig("MySQL", DBDataSource.class.getName());
        ConfigUtil.parseConfigInfo("classpath:model/orm-config.xml");
        ConfigInfo installed = ConfigManager.getInstance().getInstalledConfigInfo();

        ConfigUtil.addDataSourceConfig("MySQL", DBDataSource.class.getName());
        assertThrows(XMLParseException.class,
                () -> ConfigUtil.parseConfigInfo(
                        "classpath:invalid/malformed-config.yaml"));

        assertSame(installed, ConfigManager.getInstance().getInstalledConfigInfo());
    }
}
