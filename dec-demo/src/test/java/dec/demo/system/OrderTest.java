package dec.demo.system;

import dec.core.context.data.ModelData;
import dec.core.model.container.ModelContainer;
import dec.core.model.container.ModelLoader;
import dec.core.model.utils.DataUtil;
import dec.demo.model.SimpleViewListener;
import dec.demo.support.DemoMySqlTestSupport;
import dec.demo.system.dom.Order;
import dec.demo.system.dom.OrderDetail;
import dec.demo.system.dom.PayDetail;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 基于 dev_all OrderTest，验证订单业务模型会在同一事务中写入订单、明细、支付和支付明细。
 */
@Tag("mysql-it")
public class OrderTest {

    @Test
    void saveOrderWritesCompleteAggregate() throws Exception {
        try (DemoMySqlTestSupport mysql =
                DemoMySqlTestSupport.load("system/orm-config.xml")) {
            resetAggregateTables(mysql);

            Order orderModel = new Order();
            orderModel.setOrderDetailList(Arrays.asList(new OrderDetail()));
            orderModel.getPayInfo().setPayDetailList(Arrays.asList(new PayDetail()));

            ModelData order = DataUtil.createViewData("OrderInfo", orderModel);
            ModelContainer container = new ModelContainer();
            container.load(new ModelLoader()
                    .load("save-Order", order, "con1")
                    .addListener(new SimpleViewListener()));
            container.execute();

            assertEquals(1, count(mysql, "order_info"));
            assertEquals(1, count(mysql, "order_detail_info"));
            assertEquals(1, count(mysql, "pay_info"));
            assertEquals(1, count(mysql, "pay_detail_info"));
            assertTrue(singleInt(mysql, "SELECT o_orderId FROM order_detail_info LIMIT 1") > 0);
            assertTrue(singleInt(mysql, "SELECT o_orderId FROM pay_info LIMIT 1") > 0);
            assertTrue(singleInt(mysql, "SELECT o_payId FROM pay_detail_info LIMIT 1") > 0);

            mysql.recordExecution("OrderTest.saveOrderWritesCompleteAggregate");
        }
    }

    private void resetAggregateTables(DemoMySqlTestSupport mysql) throws Exception {
        try (Connection connection = mysql.primaryConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM pay_detail_info");
            statement.executeUpdate("DELETE FROM pay_info");
            statement.executeUpdate("DELETE FROM order_detail_info");
            statement.executeUpdate("DELETE FROM order_info");
        }
    }

    private int count(DemoMySqlTestSupport mysql, String table) throws Exception {
        return singleInt(mysql, "SELECT COUNT(*) FROM " + table);
    }

    private int singleInt(DemoMySqlTestSupport mysql, String sql) throws Exception {
        try (Connection connection = mysql.primaryConnection();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next(), "SQL query must return one row: " + sql);
            return result.getInt(1);
        }
    }
}
