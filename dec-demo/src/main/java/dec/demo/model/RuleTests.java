package dec.demo.model;

import dec.core.context.data.BaseData;
import dec.core.context.data.ModelData;
import dec.core.model.container.ModelContainer;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.manager.ContainerManager;
import dec.core.model.utils.DataUtil;
import dec.demo.config.DemoLoadTests;
import dec.demo.model.dom.Order;
import dec.demo.model.dom.Product;
import dec.demo.model.dom.User;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class RuleTests extends DemoLoadTests {

    public static void main(String args[]) throws Exception {
        RuleTests ruleTests = new RuleTests();
        ruleTests.testInit();
        //ruleTests.testOrderRule();
        ruleTests.testOrderRuleByObj();
    }

    //@Test
    public void testGetRule() throws Exception {
        ModelData order = DataUtil.createViewData("OrderInfo");
        order.setValue("id", 1);
        //order.setValue("id", 30);
        BaseData userData = DataUtil.createBaseData("user");
        userData.setValue("id", 11);
        //userData.setValue("id", 36);
        //userData.setValue("name", "test3");
        DataUtil.addDataToView(order, userData);

        ModelContainer container = ContainerManager.getCurrentModelContainer();
        ModelLoader loader = new ModelLoader();
        loader.load("get-user", order, "con1");

        container.load(loader);
        container.execute();

        System.out.println(order.getAllValues());
    }

    public void testOrderRuleFirst() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        String date = format.format(new Date());


        for (int i = 0; i < 1; i++) {
            testOrderRule();
        }
        System.out.println(date);
        System.out.println(format.format(new Date()));
    }

    //@Test
    public void testOrderRuleByObj() throws Exception {
        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        //System.out.println(format.format(new Date()));

        Order orderData = new Order();

        orderData.setProductCount(50);
        orderData.setTotalPrice(350);
        orderData.setDateTime(new Date());
        User user = new User();
        user.setUname("test");
        user.setUserName("test");
        user.setUpassword("err");
        orderData.setUserT(user);
        Product product = new Product();

        product.setProductName("pq");
        product.setProductCount(10);
        product.setProductPrice(20d);
        orderData.setProductList(Arrays.asList(product));

        ModelData order = DataUtil.createViewData("OrderInfo", orderData);

        ModelContainer container = ContainerManager.getCurrentModelContainer();
        ModelLoader loader = new ModelLoader();
        loader.load("save-Order", order, "con1").addListener(new SimpleViewListener());
        container.load(loader);

        ModelLoader loader1 = new ModelLoader();
        loader1.load("back-Order", order, "con2");
        container.load(loader1).addListener(new SimpleContainerListener());

        //ModelLoader loader2 = new ModelLoader();
        //loader2.load("save-Order", order,"con3");
        //container.load(loader2);
        //System.out.println(format.format(new Date()));
        container.execute();

        System.out.println(orderData.getProductList().get(0).getOrderId());
        //System.out.println(order.getAllValues());
    }

    public void testOrderRule() throws Exception {
        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        //System.out.println(format.format(new Date()));

        ModelData order = DataUtil.createViewData("OrderInfo");

        BaseData userData = insertUserData();

        BaseData productData1 = createProductData("p1", 10, 20);

        BaseData productData2 = createProductData("p1", 5, 30);


        DataUtil.addDataToView("userT", order, userData);

        DataUtil.addDataToView("productList", order, productData1);

        DataUtil.addDataToView("productList", order, productData2);

        order.setValue("productCount", 50);
        order.setValue("totalPrice", 350);
        order.setValue("dateTime", new Date());
        //System.out.println(format.format(new Date()));


        ModelContainer container = ContainerManager.getCurrentModelContainer();
        ModelLoader loader = new ModelLoader();
        loader.load("save-Order", order, "con1").addListener(new SimpleViewListener());
        container.load(loader);

        ModelLoader loader1 = new ModelLoader();
        loader1.load("back-Order", order, "con2");
        container.load(loader1).addListener(new SimpleContainerListener());

        //ModelLoader loader2 = new ModelLoader();
        //loader2.load("save-Order", order,"con3");
        //container.load(loader2);
        //System.out.println(format.format(new Date()));
        container.execute();

        //System.out.println(order.getAllValues());
    }





}
