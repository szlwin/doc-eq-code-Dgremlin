package dec.demo.declaration;

import dec.demo.declaration.datasource.MockDataSourceManager;
import dec.demo.declaration.dom.*;
import dec.demo.system.ConfigInit;
import dec.expand.declare.business.DefaultBusinessDeclare;
import dec.expand.declare.business.BusinessDeclareFactory;
import dec.expand.declare.conext.utils.ContextUtils;
import dec.expand.declare.service.ExecuteResult;
import dec.expand.declare.system.SystemBuilder;

import java.math.BigDecimal;

public class TestOrderBusiness {

    public static void main(String[] args) throws Throwable {

        initContext();

        initSystem();

        subscribeOrderBySimple();

        subscribeOrder();

        cancelOrderData();
    }

    public static void subscribeOrderBySimple() {

        DefaultBusinessDeclare defaultBusinessDeclare = BusinessDeclareFactory
                .createDefaultBusinessDeclare("subscribeOrderDataWithSimple");

        SubscribeOrderData subscribeOrderData = new SubscribeOrderData();

        subscribeOrderData.setProductName("test");

        subscribeOrderData.setAmount(new BigDecimal(1000));

        defaultBusinessDeclare
                .addEntity("$subscribeOrderData", subscribeOrderData)
                .transactionManager(new MockDataSourceManager())
                .addProduce("$payResultData", storage -> {

                    PayResultData payResultData = (PayResultData) storage.get("payResultData");

                    PayResultData resultData = new PayResultData();

                    resultData.setStatus(1);

                    return ExecuteResult.success(resultData);

                }).addProduce("$payData", storage -> {

                    SubscribeOrderData subscribeOrderData1 = (SubscribeOrderData) storage.get("$subscribeOrderData");

                    PayData payData = new PayData();

                    payData.setProductName(subscribeOrderData1.getProductName());

                    payData.setAmount(subscribeOrderData1.getAmount());

                    return ExecuteResult.success(payData);

                }).execute();
    }

    public static void subscribeOrder() {

        DefaultBusinessDeclare defaultBusinessDeclare = BusinessDeclareFactory
                .createDefaultBusinessDeclare("subscribeOrder");

        SubscribeOrderData subscribeOrderData = new SubscribeOrderData();

        subscribeOrderData.setProductName("test");

        subscribeOrderData.setAmount(new BigDecimal(1000));

        defaultBusinessDeclare
                .addEntity("$subscribeOrderData", subscribeOrderData)
                .transactionManager(new MockDataSourceManager())
                .addProduce("$payResultData", storage -> {

                    PayResultData payResultData = (PayResultData) storage.get("payResultData");

                    PayResultData resultData = new PayResultData();

                    resultData.setStatus(1);

                    return ExecuteResult.success(resultData);

                }).addProduce("$payData", storage -> {

                    SubscribeOrderData subscribeOrderData1 = (SubscribeOrderData) storage.get("$subscribeOrderData");

                    PayData payData = new PayData();

                    payData.setProductName(subscribeOrderData1.getProductName());

                    payData.setAmount(subscribeOrderData1.getAmount());

                    return ExecuteResult.success(payData);

                }).execute();
    }

    public static void cancelOrderData() {
        DefaultBusinessDeclare defaultBusinessDeclare = BusinessDeclareFactory
                .createDefaultBusinessDeclare("cancelOrder");

        SubscribeOrderData subscribeOrderData = new SubscribeOrderData();

        subscribeOrderData.setProductName("test");
        subscribeOrderData.setOrderId(11l);
        subscribeOrderData.setAmount(new BigDecimal(1000));

        defaultBusinessDeclare
                .addEntity("$cancelOrderData", subscribeOrderData)
                .transactionManager(new MockDataSourceManager())

                .addProduce("order", "cancelOrderData", storage -> {
                    Long cancelOrderData = (Long) storage.get("$cancelOrderData");
                    Order order = new Order();
                    order.setId(cancelOrderData);
                    order.setStatus(1);
                    return ExecuteResult.success(order);
                }).addProduce("$payResultData", storage -> {

                    PayResultData payResultData = (PayResultData) storage.get("payResultData");

                    PayResultData resultData = new PayResultData();

                    resultData.setStatus(1);

                    return ExecuteResult.success(resultData);

                }).addProduce("$payData", storage -> {

                    SubscribeOrderData subscribeOrderData1 = (SubscribeOrderData) storage.get("$cancelOrderData");

                    PayData payData = new PayData();

                    payData.setProductName(subscribeOrderData1.getProductName());

                    payData.setAmount(subscribeOrderData1.getAmount());

                    return ExecuteResult.success(payData);

                })
                .execute();
    }

    public static void initContext() throws Exception {
        ConfigInit.init();
        ContextUtils.loadConfig(new String[]{"classpath:declaration/declare-config.xml"});

    }

    public static void initSystem() {

        SystemBuilder systemBuilder = SystemBuilder.create()
                .build("order")
                .addChange("orderData", storage -> {
                    Order order = (Order)storage.get("orderData");
                    return ExecuteResult.success(order);
                }).addProduce("orderData", storage -> {
                    Long orderId = (Long) storage.getParam("orderId");

                    Order order = new Order();

                    order.setId(orderId);
                    order.setProductName("Product");
                    order.setStatus(1);
                    return ExecuteResult.success(order);
                })
                .addProduce("subscribeOrderData", storage -> {
                    SubscribeOrderData subscribeOrderData = (SubscribeOrderData) storage.get("$subscribeOrderData");
                    Order order = new Order();

                    order.setId(1l);
                    order.setProductName(subscribeOrderData.getProductName());

                    return ExecuteResult.success(subscribeOrderData);
                })
                .addProduce("cancelOrderData", storage -> {
                    Order order = (Order) storage.get("orderData");
                    return ExecuteResult.success(order);
                })
                .addProduce("orderPayResultData", storage -> {

                    Order order = (Order) storage.get("orderData");

                    PayResultData payResultData = (PayResultData) storage.get("$payResultData");

                    return ExecuteResult.success();
                }).addProduce("subscribeOrderDataSimple", storage -> {
                    SubscribeOrderData subscribeOrderData = (SubscribeOrderData) storage.get("$subscribeOrderData");
                    Order order = new Order();

                    order.setId(1l);
                    order.setProductName(subscribeOrderData.getProductName());

                    return ExecuteResult.success(subscribeOrderData);
                });


        SystemBuilder systemPayBuilder = SystemBuilder.create()
                .build("pay")
                .addProduce("payCmdData", storage -> {
                    PayData payData = (PayData) storage.get("$payData");

                    PayCmdData payCmdData = new PayCmdData();

                    payCmdData.setAmount(payData.getAmount());

                    return ExecuteResult.success(payCmdData);

                }).addProduce("payResultData", storage -> {
                    PayResultData payResultData = new PayResultData();
                    return ExecuteResult.success(payResultData);
                });

        SystemBuilder systemCommonBuilder = SystemBuilder.create()
                .build("common");

        ContextUtils.load(systemCommonBuilder.getSystem());

        ContextUtils.load(systemBuilder.getSystem());

        ContextUtils.load(systemPayBuilder.getSystem());
    }
}
