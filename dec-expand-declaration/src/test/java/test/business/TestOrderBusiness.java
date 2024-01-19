package test.business;

import dec.expand.declare.business.DefaultBusinessDeclare;
import dec.expand.declare.conext.desc.data.DataTypeEnum;
import dec.expand.declare.conext.desc.data.ValueDesc;
import dec.expand.declare.conext.desc.process.TransactionPolicy;
import dec.expand.declare.conext.desc.system.SystemDescBuilder;
import dec.expand.declare.conext.utils.DataUtils;
import dec.expand.declare.service.ExecuteResult;
import dec.expand.declare.system.SystemBuilder;
import dec.expand.declare.utils.ContextUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;

public class TestOrderBusiness {

    public static void main(String[] args) throws Throwable {


        Long date = System.currentTimeMillis();
        Order order = new Order();
        OrderOne orderOne = new OrderOne();
        orderOne.setOrderTwo(new OrderTwo());
        order.setOrderOne(orderOne);
        Field field = order.getClass().getDeclaredField("orderOne");
        field.setAccessible(true);
        Object orderOneObject = field.get(order);
        System.out.println(orderOneObject);
        field = orderOneObject.getClass().getDeclaredField("orderTwo");
        field.setAccessible(true);
        Object orderTwoObject = field.get(orderOneObject);
        field = orderTwoObject.getClass().getDeclaredField("name");
        field.setAccessible(true);
        field.set(orderTwoObject, "bbbb");
        System.out.println(order.getOrderOne().getOrderTwo().getName());

        ValueDesc valueDesc = new ValueDesc();
        valueDesc.setProperty(new String[]{"orderOne", "orderTwo", "status"});
        valueDesc.setValue(1);
        DataUtils.setValue(order, Arrays.asList(valueDesc));
        System.out.println(order.getOrderOne().getOrderTwo().getStatus());

        valueDesc = new ValueDesc();
        valueDesc.setProperty(new String[]{"amount"});
        valueDesc.setValue(BigDecimal.valueOf(100.12));
        DataUtils.setValue(order, Arrays.asList(valueDesc));
        System.out.println(order.getAmount());


        for (int i = 0; i < 100000; i++) {
            order.setId(1L);
        }
        System.out.println(System.currentTimeMillis() - date);

        date = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {

            Field f2 = order.getClass().getDeclaredField("id");
            f2.setAccessible(true);
            f2.set(order, 1l);
            //PropertyDescriptor descriptor = new PropertyDescriptor("id", order.getClass());
            //descriptor.getWriteMethod().invoke(order, 1l);
            //Method m1= order.getClass().getMethod("setId", Long.class);
            //m1.invoke(order,1l);
        }
        System.out.println(System.currentTimeMillis() - date);

        date = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            order.getId();
        }
        System.out.println(System.currentTimeMillis() - date);

        date = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            Field f2 = order.getClass().getDeclaredField("id");
            f2.setAccessible(true);
            f2.get(order);

            //Method m1= order.getClass().getMethod("getId");
            //m1.invoke(order);
        }
        System.out.println(System.currentTimeMillis() - date);

        initContext();

        initSystem();

        //subscribeOrder();

        cancelOrderData();
    }

    public static void subscribeOrder() {
        DefaultBusinessDeclare defaultBusinessDeclare = new DefaultBusinessDeclare();

        SubscribeOrderData subscribeOrderData = new SubscribeOrderData();

        subscribeOrderData.setProductName("test");

        subscribeOrderData.setAmount(new BigDecimal(1000));

        defaultBusinessDeclare.build("subscribeOrder")
                .addEntity("$subscribeOrderData", subscribeOrderData)
                .transactionManager(new MockDataSourceManager())
                .beginTx()
                .data("order", "subscribeOrderData")
                .beginTx(TransactionPolicy.NEW)
                .data("$payData")
                .beginTx(TransactionPolicy.NESTED)
                .data("pay", "payResultData")
                .data("$payResultData")
                .endTx()
                .endTx()
                .data("order", "orderPayResultData")
                .endTx()
                .addProduce("$payResultData", storage -> {

                    PayResultData payResultData = (PayResultData) storage.get("payResultData");

                    System.out.println("Produce $payResultData");

                    PayResultData resultData = new PayResultData();

                    resultData.setStatus(1);

                    return ExecuteResult.success(resultData);

                }).addProduce("$payData", storage -> {

                    System.out.println("Produce $payData");

                    SubscribeOrderData subscribeOrderData1 = (SubscribeOrderData) storage.get("$subscribeOrderData");

                    PayData payData = new PayData();

                    payData.setProductName(subscribeOrderData1.getProductName());

                    payData.setAmount(subscribeOrderData1.getAmount());

                    return ExecuteResult.success(payData);

                }).execute();
    }

    public static void cancelOrderData() {
        DefaultBusinessDeclare defaultBusinessDeclare = new DefaultBusinessDeclare();

        SubscribeOrderData subscribeOrderData = new SubscribeOrderData();

        subscribeOrderData.setProductName("test");
        subscribeOrderData.setOrderId(11l);
        subscribeOrderData.setAmount(new BigDecimal(1000));

        defaultBusinessDeclare.build("cancelOrder")
                .addEntity("$cancelOrderData", subscribeOrderData)
                .transactionManager(new MockDataSourceManager())
                .beginTx()
                .data("order", "cancelOrderData")
                .data("$payData")
                .data("pay", "payResultData")
                .data("$payResultData")
                .data("order", "orderPayResultData")
                .addProduce("order", "cancelOrderData", storage -> {
                    Long cancelOrderData = (Long) storage.get("$cancelOrderData");
                    Order order = new Order();
                    order.setId(cancelOrderData);
                    return ExecuteResult.success(order);
                }).addProduce("$payResultData", storage -> {

                    PayResultData payResultData = (PayResultData) storage.get("payResultData");

                    System.out.println("Produce $payResultData");

                    PayResultData resultData = new PayResultData();

                    resultData.setStatus(1);

                    return ExecuteResult.success(resultData);

                }).addProduce("$payData", storage -> {

                    System.out.println("Produce $payData");

                    SubscribeOrderData subscribeOrderData1 = (SubscribeOrderData) storage.get("$cancelOrderData");

                    PayData payData = new PayData();

                    payData.setProductName(subscribeOrderData1.getProductName());

                    payData.setAmount(subscribeOrderData1.getAmount());

                    return ExecuteResult.success(payData);

                })
                .endTx()
                .execute();
    }

    public static void initContext() {
        //ContextUtils.load(new OrderBusiness());


        ContextUtils.load(SystemDescBuilder.create()
                .build("common", "common")
                .data("$subscribeOrderData", "")
                .data("$payData", "")
                .data("$payResultData", "")
                .getSystem());

        ContextUtils.load(SystemDescBuilder.create()
                .build("order", "订单")
                .data("orderData", "订购订单数据")
                .type(DataTypeEnum.PERSISTENT)
                .cachePrior(false)
                .data("subscribeOrderData", "订购订单数据")
                .depend("$subscribeOrderData").init("status:1")
                .data("cancelOrderData", "取消订单数据")
                    .depend("orderData")
                        .condition("status=1 or status=2")
                        .param("orderId:$cancelOrderData.orderId")
                        .change("status:3")
                .data("orderPayResultData", "订单支付状态数据")
                .type(DataTypeEnum.PERSISTENT)
                .cachePrior(false)
                .depend("$payResultData")
                .depend("orderData")
                .getSystem());

        ContextUtils.load(SystemDescBuilder.create()
                .build("pay", "支付")
                .data("payCmdData", "支付指令数据")
                .depend("$payData")
                .data("payResultData", "支付结果数据")
                .depend("payCmdData")
                .type(DataTypeEnum.PERSISTENT)
                .cachePrior(true)
                .getSystem());


        /**.addMapping("saveOrderData", "order", "generateOrder")
         .addMapping("saveOrderData", "order", "saveOrder")
         .addMapping("savePay", "order", "generateOrderPayStatus")
         .addMapping("savePay", "order", "saveOrderPayResutl")
         .addMapping("operateWithPay", "pay", "generatePayCmd")
         .addMapping("operateWithPay", "pay", "executePayCmd")
         .addMapping("operateWithPay", "pay", "savePayCmdResutl")
         */

    }

    public static void initSystem() {
        SystemBuilder systemBuilder = SystemBuilder.create()
                .build("order")
                .addChange("orderData", storage -> {
                    Order order = (Order)storage.get("orderData");
                    System.out.println("change:"+order.getStatus());
                    return ExecuteResult.success();
                }).addProduce("orderData", storage -> {

                    System.out.println("Produce orderData");

                    Long orderId = (Long) storage.getParam("orderId");

                    System.out.println("OrderId:"+orderId);
                    Order order = new Order();

                    order.setId(orderId);
                    order.setProductName("Product");
                    order.setStatus(1);
                    return ExecuteResult.success(order);
                })
                .addProduce("subscribeOrderData", storage -> {

                    System.out.println("Produce subscribeOrderData");

                    SubscribeOrderData subscribeOrderData = (SubscribeOrderData) storage.get("$subscribeOrderData");
                    System.out.println("subscribeOrderData.getStatus()" + subscribeOrderData.getStatus());
                    Order order = new Order();

                    order.setId(1l);
                    order.setProductName(subscribeOrderData.getProductName());

                    return ExecuteResult.success(subscribeOrderData);
                })
                .addProduce("cancelOrderData", storage -> {
                    java.lang.System.out.println("produce cancelOrderData");
                    Order order = (Order) storage.get("orderData");
                    //java.lang.System.out.println(order.getId());
                    return ExecuteResult.success(order);
                })
                .addProduce("orderPayResultData", storage -> {

                    System.out.println("Produce orderPayResultData");

                    Order order = (Order) storage.get("orderData");

                    PayResultData payResultData = (PayResultData) storage.get("$payResultData");

                    return ExecuteResult.success();
                });


        SystemBuilder systemPayBuilder = SystemBuilder.create()
                .build("pay")
                .addProduce("payCmdData", storage -> {

                    System.out.println("Produce payCmdData");

                    PayData payData = (PayData) storage.get("$payData");

                    PayCmdData payCmdData = new PayCmdData();

                    payCmdData.setAmount(payData.getAmount());

                    return ExecuteResult.success(payCmdData);

                }).addProduce("payResultData", storage -> {
                    System.out.println("Produce payResultData");
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
