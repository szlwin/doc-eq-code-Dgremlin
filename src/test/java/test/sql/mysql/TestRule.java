package test.sql.mysql;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.orm.common.xml.Config;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ModelData;
import com.orm.model.container.ModelContainer;
import com.orm.model.container.ModelLoader;
import com.orm.model.container.manager.ContainerManager;


public class TestRule {

	/**
	 * @param args
	 * void
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		init();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		format.format(new Date());
		testOrderRule();
		//testGetRule();
		format.format(new Date());
	}

	public static void testGetRule() throws Exception{
		ModelData order = DataUtil.createViewData("OrderInfo");
		order.setValue("id", 6458);
		//order.setValue("id", 30);
		BaseData userData = DataUtil.createBaseData("user");
		userData.setValue("id", 7590);
		//userData.setValue("id", 36);
		//userData.setValue("name", "test3");
		DataUtil.addDataToView(order, userData);
		
		ModelContainer container = ContainerManager.getCurrentModelContainer();
		ModelLoader loader = new ModelLoader();
		loader.load("get-user", order,"con1");
		
		container.load(loader);
		container.execute();
		
		System.out.println(order.getAllValues());
	}
	
	public static void testOrderRule() throws Exception{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		System.out.println(format.format(new Date()));
		
		ModelData order = DataUtil.createViewData("OrderInfo");
		
		BaseData userData = insertUserData();
		
		BaseData productData1 = createProductData("p1",10,20);
		
		BaseData productData2 = createProductData("p1",5,30);
		
		DataUtil.addDataToView("userT",order, userData);
		
		DataUtil.addDataToView("productList",order, productData1);
		
		DataUtil.addDataToView("productList",order, productData2);
		
		order.setValue("productCount", 50);
		order.setValue("totalPrice", 350);
		order.setValue("dateTime", new Date());
		System.out.println(format.format(new Date()));
		
		
		ModelContainer container = ContainerManager.getCurrentModelContainer();
		ModelLoader loader = new ModelLoader();
		loader.load("save-Order", order,"con1");
		container.load(loader);

		ModelLoader loader1 = new ModelLoader();
		loader1.load("back-Order", order,"con2");
		container.load(loader1);
		
		//ModelLoader loader2 = new ModelLoader();
		//loader2.load("save-Order", order,"con3");
		//container.load(loader2);
		
		System.out.println(format.format(new Date()));
		container.execute();
		
		System.out.println(order.getAllValues());
		//System.out.println(order.getValue("userId"));
		//System.out.println(((Collection)order.getValue("productList")).size());
	}
	
	public static BaseData createProductData(String name,int count,double price) throws Exception{
		
		BaseData productData = DataUtil.createBaseData("product");
		
		productData.setValue("name", name);
		productData.setValue("count", count);
		productData.setValue("price", price);		
		
		return productData;
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
	
	public static void init() throws XMLParseException{
		Config config = new Config("test-config/orm-config.xml");
		config.initContext();
		
		System.out.println();
	}
	

}
