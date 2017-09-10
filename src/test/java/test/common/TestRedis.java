package test.common;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.orm.common.xml.Config;
import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.session.SimpleSession;
import com.orm.session.factory.SessionFactory;


public class TestRedis {

	public static void main(String[] args) throws Exception {
		init();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		format.format(new Date());
		//testSaveRule();
		testGetRule();
		format.format(new Date());
	}
	
	private static void testGetRule() throws Exception {
		SimpleSession session = SessionFactory.getSimpleSession("con5");
		
		session.begian();
		BaseData baseData = DataUtil.createBaseData("userR");
		baseData.setValue("u_id", "utest");
		BaseData rSdata = session.get(baseData);
		
		session.close();
		
		System.out.println(rSdata.getValues());
		
		
	}

	private static void testSaveRule() throws Exception {
		SimpleSession session = SessionFactory.getSimpleSession("con5");
		session.begian();
		
		BaseData baseData = DataUtil.createBaseData("userR");
		baseData.setValue("u_id", "utest");
		baseData.setValue("u_name", "utest_name222");
		baseData.setValue("u_account", "utest_account222");
		session.save(baseData);
		session.commit();
		session.close();
	}

	public static void init() throws XMLParseException{
		Config config = new Config("config/orm-config.xml");
		config.initContext();
		
		System.out.println();
	}
}
