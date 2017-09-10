package test.common;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.orm.common.xml.Config;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ModelData;
import com.orm.model.container.ModelLoader;
import com.orm.model.container.manager.TranAdvanceContainerManager;
import com.orm.model.container.manager.TranContainerManager;
import com.orm.model.execute.tran.TransactionContainer;
import com.orm.model.execute.tran.advance.TranAdvanceContainer;


public class TestTranRule {

	/**
	 * @param args
	 * void
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		init();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		format.format(new Date());
		//testAdvanceThreadOrderRule(20);
		testOrderRule(1);
		format.format(new Date());
	}

	public static void testOrderRule(int count) throws Exception{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		System.out.println(format.format(new Date()));
		
		TransactionContainer container = TranContainerManager.getTransactionContainer("test");
		
		for(int i = 0; i < count;i++){
			ModelData order = creatOrder();
			ModelLoader loader = new ModelLoader();
			loader.load("save-Order", order);
			container.load(loader);
		}
		/*
		for(int i = 0; i < count/2;i++){
			ModelData order = creatOrder();
			ModelLoader loader = new ModelLoader();
			loader.load("save-Order", order,"con2");
			container.load(loader);
		}*/
		
		container.execute();
		//System.out.println(format.format(new Date()));
		//System.out.println(order);
		//System.out.println(order.getValue("u_id"));
		System.out.println(format.format(new Date()));
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
		//userData.setValue("id", flag);
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
		Config config = new Config("config/orm-config.xml");
		config.initContext();
	}
	
	public static void testAdvanceThreadOrderRule(int count) throws Exception{
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		System.out.println(format.format(new Date()));
		
		TranAdvanceContainer container = TranAdvanceContainerManager.getTranAdvanceContainer("test");
		TranAdvanceContainer container1 = new TranAdvanceContainer("test111");
		for(int i = 0; i < count;i++){
			ModelData order = creatOrder();
			ModelLoader loader = new ModelLoader();
			loader.load("save-Order", order);
			container.load(loader);
			
			ModelData order1 = creatOrder();
			ModelLoader loader1 = new ModelLoader();
			loader1.load("save-Order", order1);
			container1.load(loader1);
		}
		
		for(int i = 0; i < count/2;i++){
			ModelData order = creatOrder();
			ModelLoader loader = new ModelLoader();
			loader.load("save-Order", order,"con1");
			container.load(loader);
		}
		container.execute();
		
		container1.execute();
		System.out.println(format.format(new Date()));
		//System.out.println(order);
		//System.out.println(order.getValue("u_id"));
	}
	
	public static ModelData creatOrder() throws Exception{
		ModelData order = DataUtil.createViewData("OrderInfo");
		
		BaseData userData = insertUserData();
		
		BaseData productData1 = createProductData("p1",10,20);
		
		BaseData productData2 = createProductData("p1",5,30);
		
		DataUtil.addDataToView(order, userData);
		
		DataUtil.addDataToView(order, productData1);
		
		DataUtil.addDataToView(order, productData2);
		
		order.setValue("productCount", 50);
		order.setValue("totalPrice", 350);
		
		return order;
	}
}
