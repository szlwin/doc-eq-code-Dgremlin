package test.common;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.orm.common.xml.Config;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ModelData;


public class TestImpo {

	public static void main(String[] args) throws Exception {
		init();
		ModelData order = DataUtil.createViewData("OrderInfo");
		order.addValue("user.id", 1);
	}
	
	public static void init() throws XMLParseException{
		Config config = new Config("config/orm-config.xml");
		config.initContext();
	}
}
