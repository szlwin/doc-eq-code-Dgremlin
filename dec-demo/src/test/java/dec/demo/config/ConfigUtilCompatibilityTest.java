package dec.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.data.ModelData;
import dec.core.model.utils.DataUtil;
import dec.core.starter.common.ConfigUtil;
import dec.external.datasource.sql.datasource.DBDataSource;
import org.junit.jupiter.api.Test;

/** 验证 P2 统一入口继续兼容没有 System/Business 声明的旧配置。 */
class ConfigUtilCompatibilityTest {

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
}
