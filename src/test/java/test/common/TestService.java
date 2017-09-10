package test.common;
import java.util.Map;

import com.orm.common.xml.Config;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ModelData;
import com.orm.service.ServiceContainer;
import com.orm.service.ServiceResult;


public class TestService {

	public static void main(String[] args) throws Exception {
		init();
	}
	
	public static void init() throws Exception{
		Config config = new Config("config/orm-config.xml");
		config.initContext();
		
		testService();
		
	}
	
	public static void testService() throws Exception{

		
		ServiceContainer serviceContainer = new ServiceContainer();
		
		ServiceResult serviceResult = serviceContainer.execute("SAVE_ORDER", "1.0",createData());
		/*Iterator<String> it = serviceResult.getErrorMap().keySet().iterator();
		
		while(it.hasNext()){
			String key = it.next();
			Exception e = serviceResult.getErrorMap().get(key);
			System.out.println(key+":"+e.getMessage());
		}*/
		
	}
	
	public static Map<String,Object> createData() throws Exception{
		ModelData order = DataUtil.createViewData("OrderInfo");
		
		BaseData userData = insertUserData();
		
		BaseData productData1 = createProductData("p1",10,20);
		
		BaseData productData2 = createProductData("p1",5,30);
		
		DataUtil.addDataToView("userT",order, userData);
		
		DataUtil.addDataToView("productList",order, productData1);
		
		DataUtil.addDataToView("productList",order, productData2);
		
		order.setValue("productCount", 50);
		order.setValue("totalPrice", 350);
		return order.getAllValues();
	}
	
	public static BaseData insertUserData() throws Exception{
		
		BaseData userData = DataUtil.createBaseData("user");
		//userData.setValue("id", 14);
		userData.setValue("name", "test");
		userData.setValue("password", "test");
		
		//SimpleSession session = SessionFactory.createSimpleSession();
		//session.begian();
		//session.save(userData);
		//session.commit();
		//session.close();
		
		return userData;
	}
	
	public static BaseData createProductData(String name,int count,double price) throws Exception{
		
		BaseData productData = DataUtil.createBaseData("product");
		
		productData.setValue("name", name);
		productData.setValue("count", count);
		productData.setValue("price", price);		
		
		return productData;
	}
}
