package dec.demo.model;

import dec.core.context.data.BaseData;
import dec.core.context.data.ModelData;
import dec.core.model.container.ModelContainer;
import dec.core.model.container.ModelLoader;
import dec.core.model.utils.DataUtil;
import dec.demo.config.DemoLoadTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 基于 dev_all RuleTests 的订单规则场景，验证 Rule、ORM 和双 MySQL 数据源的真实执行结果。
 */
@Tag("mysql-it")
public class RuleTests extends DemoLoadTests {

    @Test
    void orderRulesWriteExpectedRowsToBothDatabases() throws Exception {
        resetDatabase();

        ModelData order = DataUtil.createViewData("OrderInfo");
        BaseData userData = createUserData("rule-user", "rule-password");
        BaseData productData1 = createProductData("rule-product-20", 10, 20);
        BaseData productData2 = createProductData("rule-product-30", 5, 30);

        DataUtil.addDataToView("userT", order, userData);
        DataUtil.addDataToView("productList", order, productData1);
        DataUtil.addDataToView("productList", order, productData2);
        order.setValue("productCount", 50);
        order.setValue("totalPrice", 350);
        order.setValue("dateTime", new Date());

        ModelContainer container = new ModelContainer();
        container.load(new ModelLoader()
                .load("save-Order", order, "con1")
                .addListener(new SimpleViewListener()));
        container.load(new ModelLoader().load("back-Order", order, "con2"));
        container.execute();

        assertEquals(1, primaryCount(
                "SELECT COUNT(*) FROM user_info WHERE u_name = ?",
                "rule-user"));
        assertEquals(1, primaryCount("SELECT COUNT(*) FROM order_info"));
        assertEquals(1, primaryCount(
                "SELECT COUNT(*) FROM product_info WHERE p_name = ? AND p_price = 30",
                "rule-product-30"));
        assertEquals(0, primaryCount(
                "SELECT COUNT(*) FROM product_info WHERE p_price = 20"));
        assertEquals(2, secondaryCount("SELECT COUNT(*) FROM product_info"));
        assertTrue(primaryDecimal("SELECT MAX(o_totalPrice) FROM order_info") > 350.0d,
                "DSL rule must update totalPrice before persistence");

        mysql.recordExecution("RuleTests.orderRulesWriteExpectedRowsToBothDatabases");
    }

    private void resetDatabase() throws Exception {
        try (Connection connection = mysql.primaryConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM product_info");
            statement.executeUpdate("DELETE FROM order_info");
            statement.executeUpdate("DELETE FROM user_info");
        }
        try (Connection connection = mysql.secondaryConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM product_info");
            statement.executeUpdate("DELETE FROM order_info");
            statement.executeUpdate("DELETE FROM user_info");
        }
    }

    private int primaryCount(String sql, Object... parameters) throws Exception {
        try (Connection connection = mysql.primaryConnection()) {
            return count(connection, sql, parameters);
        }
    }

    private int secondaryCount(String sql, Object... parameters) throws Exception {
        try (Connection connection = mysql.secondaryConnection()) {
            return count(connection, sql, parameters);
        }
    }

    private int count(Connection connection, String sql, Object... parameters) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next(), "Count query must return one row");
                return result.getInt(1);
            }
        }
    }

    private double primaryDecimal(String sql) throws Exception {
        try (Connection connection = mysql.primaryConnection();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next(), "Aggregate query must return one row");
            return result.getDouble(1);
        }
    }
}
