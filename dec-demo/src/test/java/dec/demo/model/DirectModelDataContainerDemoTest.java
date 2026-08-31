package dec.demo.model;

import dec.core.context.data.BaseData;
import dec.core.context.data.ModelData;
import dec.core.model.container.ModelContainer;
import dec.core.model.container.ModelLoader;
import dec.core.model.utils.DataUtil;
import dec.demo.config.DemoLoadTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 方案 B 演示：一次加载模型后，调用方直接操作 ModelData 和 ModelContainer。
 *
 * <p>与 RuleTests 相同，ModelData 负责承载业务值，ModelContainer 负责加载并执行规则；
 * 不再经过 RuntimeModelAccessScope、Session、EffectProvider 或 Guarded executor。</p>
 */
@Tag("mysql-it")
public class DirectModelDataContainerDemoTest extends DemoLoadTests {

    @Test
    void directModelDataAndContainerExecution() throws Exception {
        resetOrderTables();

        ModelData order = DataUtil.createViewData("OrderInfo");
        BaseData user = createUserData("direct-user", "direct-password");
        BaseData product = createProductData("direct-product", 2, 30);

        DataUtil.addDataToView("userT", order, user);
        DataUtil.addDataToView("productList", order, product);
        order.setValue("productCount", 2);
        order.setValue("totalPrice", 60);
        order.setValue("dateTime", new Date());

        ModelContainer container = new ModelContainer();
        container.load(new ModelLoader()
                .load("save-Order", order, "con1")
                .addListener(new SimpleViewListener()));
        container.execute();

        assertEquals(2, order.getValue("productCount"));
        assertTrue(((Number) order.getValue("totalPrice")).doubleValue() > 60.0d,
                "DSL rule must update totalPrice before persistence");
        mysql.recordExecution("DirectModelDataContainerDemoTest.directModelDataAndContainerExecution");
    }

    private void resetOrderTables() throws Exception {
        try (java.sql.Connection connection = mysql.primaryConnection();
                java.sql.Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM product_info");
            statement.executeUpdate("DELETE FROM order_info");
            statement.executeUpdate("DELETE FROM user_info");
        }
    }
}
