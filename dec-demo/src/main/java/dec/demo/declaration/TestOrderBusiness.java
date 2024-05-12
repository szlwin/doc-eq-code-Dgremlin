package dec.demo.declaration;

import dec.demo.declaration.datasource.MockDataSourceManager;
import dec.demo.declaration.dom.*;
import dec.demo.system.ConfigInit;
import dec.expand.declare.business.BusinessDeclareFactory;
import dec.expand.declare.business.DefaultBusinessDeclare;
import dec.expand.declare.conext.utils.ContextUtils;
import dec.expand.declare.service.ExecuteResult;
import dec.expand.declare.system.SystemBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class TestOrderBusiness {

    private final static Logger log = LoggerFactory.getLogger(TestOrderBusiness.class);

    public static void main(String[] args) throws Throwable {

        initContext();

        initSystem();

        subscribeOrderBySimple();

        subscribeOrder();

        cancelOrderData();
    }

    public static void subscribeOrderBySimple() {

        //1.创建业务，可只创建一次
        DefaultBusinessDeclare defaultBusinessDeclare = BusinessDeclareFactory
                .createDefaultBusinessDeclare("subscribeOrderDataWithSimple");

        SubscribeOrderData subscribeOrderData = new SubscribeOrderData();

        subscribeOrderData.setProductName("test");

        subscribeOrderData.setAmount(new BigDecimal(1000));

        defaultBusinessDeclare
                //2.添加参数
                .addEntity("$subscribeOrderData", subscribeOrderData)
                //3.添加common系统中$payResultData数据的生产者
                .addProduce("$payResultData", storage -> {

                    PayResultData payResultData = (PayResultData) storage.get("payResultData");

                    PayResultData resultData = new PayResultData();

                    resultData.setStatus(1);

                    return ExecuteResult.success(resultData);
                })
                //4.添加common系统中$payData数据的生产者
                .addProduce("$payData", storage -> {

                    SubscribeOrderData subscribeOrderData1 = (SubscribeOrderData) storage.get("$subscribeOrderData");

                    PayData payData = new PayData();

                    payData.setProductName(subscribeOrderData1.getProductName());

                    payData.setAmount(subscribeOrderData1.getAmount());

                    return ExecuteResult.success(payData);
                }).execute();

        log.info("subscribeOrderDataWithSimple result:" + defaultBusinessDeclare.getExecuteResult().isSuccess());
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
                    return ExecuteResult.success();
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
                    Order order = (Order) storage.get("orderData");
                    System.out.println(storage.getStatus("status"));
                    System.out.println(order.getStatus());
                    return ExecuteResult.success(order);
                }).addProduce("orderData", storage -> {
                    Long orderId = (Long) storage.get("orderId");

                    if (orderId == null) {
                        orderId = (Long) storage.getParam("orderId");

                    }
                    if (orderId == null) {
                        throw new RuntimeException("orderId is null");
                    }

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

                    return ExecuteResult.success("orderId",order.getId());
                })
                .addProduce("cancelOrderData", storage -> {
                    Order order = (Order) storage.get("orderData");
                    return ExecuteResult.success(order);
                })
                .addProduce("orderPayResultData", storage -> {
                    PayResultData payResultData = (PayResultData) storage.get("$payResultData");
                    OrderPayResultData orderPayResultData = new OrderPayResultData();
                    orderPayResultData.setOrderId(payResultData.getOrderId());
                    orderPayResultData.setStatus(payResultData.getStatus());
                    return ExecuteResult.success(orderPayResultData);
                }).addProduce("subscribeOrderDataSimple", storage -> {
                    SubscribeOrderData subscribeOrderData = (SubscribeOrderData) storage.get("$subscribeOrderData");
                    Order order = new Order();

                    order.setId(1l);
                    order.setProductName(subscribeOrderData.getProductName());

                    return ExecuteResult.success("orderId",order.getId());
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
