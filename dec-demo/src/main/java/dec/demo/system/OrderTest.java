package dec.demo.system;

import dec.core.context.config.exception.DataNotDefineException;
import dec.core.context.data.ModelData;
import dec.core.model.container.ModelContainer;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.manager.ContainerManager;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import dec.core.model.utils.DataUtil;
import dec.demo.model.SimpleViewListener;
import dec.demo.system.dom.Order;
import dec.demo.system.dom.OrderDetail;
import dec.demo.system.dom.PayDetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrderTest {

    public static void main(String args[]) throws Exception {
        init();
        saveOrder();
    }

    public static void init() throws Exception {
        ConfigInit.init();
    }

    public static void saveOrder() throws DataNotDefineException, ExecuteRuleException {
        Order orderModel = new Order();
        orderModel.setOrderDetailList(Arrays.asList(new OrderDetail()));
        orderModel.getPayInfo().setPayDetailList(Arrays.asList(new PayDetail()));

        ModelData order = DataUtil.createViewData("OrderInfo", orderModel);

        ModelContainer container = ContainerManager.getCurrentModelContainer();
        ModelLoader loader = new ModelLoader();
        loader.load("save-Order", order, "con1").addListener(new SimpleViewListener());
        container.load(loader);
        container.execute();
    }
}
