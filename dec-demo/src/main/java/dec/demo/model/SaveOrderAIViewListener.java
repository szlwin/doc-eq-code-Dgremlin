package dec.demo.model;

import dec.core.context.data.ModelData;
import dec.core.model.container.ResultInfo;
import dec.core.model.container.listener.ViewEvent;
import dec.core.model.container.listener.ViewEventEnum;
import dec.core.model.container.listener.ViewListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SaveOrderAIViewListener implements ViewListener {

    private final static Logger log = LoggerFactory.getLogger(SaveOrderAIViewListener.class);

    @Override
    public ResultInfo notify(ViewEvent event) {

        //1.检查产品数量是否大于10
        if(event.getType() == ViewEventEnum.VIEW_START && event.getRuleName().equals("ai-demo1")){
            ModelData modelData = event.getModelData();
            List<Map<String,Object>> productList = (List<Map<String, Object>>) modelData.getValue("productList");
            if(productList.size()>10){
                return ResultInfo.fail("C001","产品数量不能大于10","1");
            }
        }

        //2.对产品总金额求和
        if(event.getType() == ViewEventEnum.VIEW_START && event.getRuleName().equals("ai-demo2")){
            ModelData modelData = event.getModelData();
            List<Map<String,Object>> productList = (List<Map<String, Object>>) modelData.getValue("productList");
            double totalPrice = 0d;
            for(Map<String,Object> product : productList){
                totalPrice  = totalPrice+ ((Integer) product.get("productCount")) * ((Double) product.get("productPrice"));
            }
            modelData.setValue("totalPrice",totalPrice);
        }

        return ResultInfo.success();
    }
}