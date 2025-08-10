package dec.demo.system;

import dec.core.context.config.exception.DataNotDefineException;
import dec.core.context.data.BaseData;
import dec.core.context.data.ModelData;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import dec.core.model.utils.DataUtil;
import dec.core.session.SimpleSession;
import dec.demo.declaration.datasource.MockDataSourceManager;
import dec.demo.system.dom.*;
import dec.expand.declare.bean.Bean;
import dec.expand.declare.business.BusinessDeclareFactory;
import dec.expand.declare.business.DefaultBusinessDeclare;
import dec.expand.declare.conext.utils.ContextUtils;
import dec.expand.declare.service.ExecuteResult;
import dec.expand.declare.system.SystemBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class OrderTest {

    public static void main(String args[]) throws Exception {
        init();
        //saveUser();
        saveOrder();
    }

    private static void saveUser() throws Exception {
        DefaultBusinessDeclare defaultBusinessDeclare = BusinessDeclareFactory
                .createDefaultBusinessDeclare("saveUser");

        //1.创建业务模型对象
        ModelData userInfo = DataUtil.createViewData("UserInfo");

        //2.创建数据对象
        BaseData userData = insertUserData();

        DataUtil.addDataToView(userInfo, userData);

        User user = new User();
        user.setName("test");
        user.setActiveTime(new Date());
        defaultBusinessDeclare
                .addEntity("$userData", user)
                .setRuleModel("save-User", userInfo)
                .execute();
    }

    public static void init() throws Exception {
        ConfigInit.init();
        ContextUtils.loadConfig(new String[]{"classpath:system/declare-config.xml"});
        initSystem();
    }

    public static void saveOrder() throws DataNotDefineException, ExecuteRuleException {
        DefaultBusinessDeclare defaultBusinessDeclare = BusinessDeclareFactory
                .createDefaultBusinessDeclare("subscribeOrderWithDom");

        SubscribeOrderData subscribeOrderData = new SubscribeOrderData();
        subscribeOrderData.setUserId(1l);
        subscribeOrderData.setProductCount(2);
        subscribeOrderData.setTotalAmount(BigDecimal.valueOf(200));
        subscribeOrderData.setTotalPrice(BigDecimal.valueOf(200));

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setUserId(1l);
        orderDetail.setProductId(1);
        orderDetail.setProductName("p1");
        orderDetail.setProductAmount(BigDecimal.valueOf(200));
        orderDetail.setProductAmount(BigDecimal.valueOf(200));
        orderDetail.setCreateDate(new Date());
        subscribeOrderData.getOrderDetailList().add(orderDetail);

        ModelData orderInfo = DataUtil.createViewData("OrderInfo");

        defaultBusinessDeclare
                .addEntity("$subscribeOrderData", subscribeOrderData)
                .setRuleModel("save-Order", orderInfo)
                .transactionManager(new MockDataSourceManager())
                .addProduce("$payResultData", storage -> {

                    Pay payResultData = (Pay) storage.get("payResultData");

                    PayResultData resultData = new PayResultData();

                    resultData.setStatus(payResultData.getStatus());

                    return ExecuteResult.success(resultData);

                }).addProduce("$payData", storage -> {

                    Order order = (Order) storage.get("subscribeOrderData");

                    Pay payData = new Pay();

                    payData.setOrderId(order.getId());
                    payData.setUserId(order.getUserId());

                    payData.setTotalAmount(order.getTotalAmount());
                    payData.setStatus(1);
                    List<OrderDetail> orderDetailList = order.getOrderDetailList();
                    List<PayDetail> payDetailList = new ArrayList<>();
                    for (OrderDetail orderDetailData : orderDetailList) {
                        PayDetail payDetail = new PayDetail();
                        payDetail.setPayAmount(orderDetailData.getProductAmount());
                        payDetail.setPayPrice(orderDetailData.getProductPrice());
                        payDetail.setProductId(orderDetailData.getProductId());
                        payDetail.setProductName(orderDetailData.getProductName());
                        payDetailList.add(payDetail);
                    }
                    payData.setPayDetailList(payDetailList);
                    return ExecuteResult.success(payData);

                }).execute();
    }

    public static void initSystem() {

        SystemBuilder systemUserBuilder = SystemBuilder.create()
                .build("user")
                .addProduce("userData", storage -> {

                    User user = (User) storage.get("$userData");
                    return ExecuteResult.success(user);
                });

        SystemBuilder systemBuilder = SystemBuilder.create()
                .build("order")
                .addChange("orderData", storage -> {
                    Order order = (Order) storage.get("orderData");
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
                    //order.setProductName("Product");
                    order.setStatus(1);
                    return ExecuteResult.success(order);
                })
                .addProduce("subscribeOrderData", storage -> {
                    SubscribeOrderData subscribeOrderData = (SubscribeOrderData) storage.get("$subscribeOrderData");

                    Order order = new Order();
                    order.setTotalPrice(subscribeOrderData.getTotalPrice());
                    order.setTotalAmount(subscribeOrderData.getTotalAmount());
                    order.setProductCount(subscribeOrderData.getProductCount());
                    order.setCreateDate(new Date());
                    order.setUserId(subscribeOrderData.getUserId());
                    order.setStatus(subscribeOrderData.getStatus());

                    BaseData baseData = createOrderData(order);
                    SimpleSession simpleSession = new SimpleSession("con1");
                    simpleSession.begian();
                    simpleSession.save(baseData);

                    List<OrderDetail> orderDetailList = subscribeOrderData.getOrderDetailList();

                    for (OrderDetail orderDetail : orderDetailList) {
                        orderDetail.setOrderId((Long)baseData.getValue("id"));
                    }
                    simpleSession.commit();
                    simpleSession.close();
                    order.setId((Long)baseData.getValue("id"));
                    order.setOrderDetailList(subscribeOrderData.getOrderDetailList());
                    return ExecuteResult.success(order);
                    //return ExecuteResult.success("orderId", baseData.getValue("id"));
                })
                .addProduce("orderPayResultData", storage -> {
                    Pay payResultData = (Pay) storage.get("$payResultData");
                    OrderPayResultData orderPayResultData = new OrderPayResultData();
                    orderPayResultData.setStatus(payResultData.getStatus());
                    return ExecuteResult.success(orderPayResultData);
                });


        SystemBuilder systemPayBuilder = SystemBuilder.create()
                .build("pay")
                .addProduce("payCmdData", storage -> {
                    Pay payData = (Pay) storage.get("$payData");
                    return ExecuteResult.success(payData);
                }).addProduce("payResultData", storage -> {
                    Pay payResultData = (Pay) storage.get("payCmdData");
                    payResultData.setStatus(2);
                    return ExecuteResult.success(payResultData);
                });

        SystemBuilder systemCommonBuilder = SystemBuilder.create()
                .build("common");

        ContextUtils.load(systemCommonBuilder.getSystem());

        ContextUtils.load(systemBuilder.getSystem());

        ContextUtils.load(systemPayBuilder.getSystem());

        ContextUtils.load(systemUserBuilder.getSystem());
    }

    public static BaseData insertUserData() throws Exception {
        BaseData userData = DataUtil.createBaseData("user");
        return userData;
    }

    public static BaseData createOrderData(Order order) throws Exception {

        BaseData orderData = DataUtil.createBaseData("order");

        Map<String, Object> map = orderData.getValues();

        Bean bean = Bean.get();
        bean.copy(order, map);
        //System.out.println(map);
        return orderData;
    }

    public static BaseData createOrderDetail(OrderDetail orderDetail) throws Exception {

        BaseData orderData = DataUtil.createBaseData("orderDetail");

        Map<String, Object> map = orderData.getValues();

        Bean bean = Bean.get();
        bean.copy(orderDetail, map);
        //System.out.println(map);
        return orderData;
    }
}
