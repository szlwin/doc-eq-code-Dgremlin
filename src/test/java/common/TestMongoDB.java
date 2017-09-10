package test.common;
import com.orm.common.xml.Config;
import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ModelData;
import com.orm.model.container.ModelContainer;
import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;
import com.orm.model.container.manager.ContainerManager;
import com.orm.model.execute.rule.exception.ExecuteRuleException;


public class TestMongoDB {

	public static void main(String[] args) throws Exception {
		init();
		insertUser();
		//getUser();
		//updateUser();
		//deleteUser();
	}

	
	public static void init() throws XMLParseException{
		Config config = new Config("config/orm-config.xml");
		config.initContext();
	}
	
	public static void insertUser() throws XMLParseException, DataNotDefineException, ExecuteRuleException{
		ModelData user = DataUtil.createViewData("UserMInfo");
		user.setValue("account", "test0005");
		user.setValue("name","test11");
		user.setValue("score",1);
		
		ModelContainer container = ContainerManager.getCurrentModelContainer();
		ModelLoader loader = new ModelLoader();
		loader.load("save-User-m", user,"con6");
		container.load(loader);
		
		container.execute();
		
		System.out.println(user.getAllValues());
	}
	
	
	public static void getUser() throws XMLParseException, DataNotDefineException, ExecuteRuleException{
		ModelData user = DataUtil.createViewData("UserMInfo");
		user.setValue("account", "test0005");
		
		ModelContainer container = ContainerManager.getCurrentModelContainer();
		ModelLoader loader = new ModelLoader();
		loader.load("get-User-m", user,"con6");
		container.load(loader);
		
		container.execute();
		
		ResultInfo r = container.getResult();
		
		System.out.println(user.getAllValues());
	}
	
	
	public static void updateUser() throws XMLParseException, DataNotDefineException, ExecuteRuleException{
		ModelData user = DataUtil.createViewData("UserMInfo");
		user.setValue("account", "test0005");
		user.setValue("address", "address5");
		user.setValue("score", "4");
		user.setValue("name", "testnew");
		
		ModelContainer container = ContainerManager.getCurrentModelContainer();
		ModelLoader loader = new ModelLoader();
		loader.load("update-User-m", user,"con6");
		container.load(loader);
		
		container.execute();
		
		ResultInfo r = container.getResult();
		
		System.out.println(user.getAllValues());
	}
	
	public static void deleteUser() throws XMLParseException, DataNotDefineException, ExecuteRuleException{
		ModelData user = DataUtil.createViewData("UserMInfo");
		user.setValue("account", "test0003");
		
		ModelContainer container = ContainerManager.getCurrentModelContainer();
		ModelLoader loader = new ModelLoader();
		loader.load("delete-User-m", user,"con6");
		container.load(loader);
		
		container.execute();
		
		ResultInfo r = container.getResult();
		
		System.out.println(user.getAllValues());
	}
}
