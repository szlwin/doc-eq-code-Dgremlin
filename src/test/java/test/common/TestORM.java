package test.common;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orm.common.xml.Config;
import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ModelData;
import com.orm.query.SimpleDataQuery;
import com.orm.query.factory.QueryFactory;
import com.orm.session.SimpleSession;
import com.orm.session.factory.SessionFactory;
import com.orm.sql.convert.common.DeleteSQLConvert;
import com.orm.sql.convert.common.QuerySQLConvert;
import com.orm.sql.convert.common.UpdateSQLConvert;
import com.orm.sql.dom.ConvertParam;

public class TestORM {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		init();
		//testQuery1();
		//testQuery2();
		
		test2();
		test3();
		test4();
		test5();
		//testInsert();
		//testUpdate();
		//testDelete();
		//testGet();
		
		//testOrignUpdate1();
		//testOrignUpdate2();
		//testOrignUpdate3();
		//testNumber();
	}
	
	public static void init() throws XMLParseException{
		Config config = new Config("config/orm-config.xml");
		config.initContext();
	}
	
	public static void test() throws XMLParseException, DataNotDefineException{

		
		BaseData order = DataUtil.createBaseData("order");
		
		order.setValue("id", 1);
		order.setValue("userId", 1);
		order.setValue("count", 2);
		order.setValue("totalPrice", 25);
		
		ModelData orderInfo = DataUtil.createViewData("OrderInfo");
		
		DataUtil.addDataToView(orderInfo, order);
		
		int id = (Integer) orderInfo.getValue("id");
		
		System.out.println(id);
	}
	
	public static void testInsert() throws Exception{
		
		BaseData order = DataUtil.createBaseData("order");
		
		//order.setValue("id", 2);
		order.setValue("userId", 1);
		order.setValue("count", 2);
		order.setValue("totalPrice", 25);
		
		SimpleSession session = SessionFactory.getSimpleSession();
		
		session.begian();
		session.save(order);
		session.commit();
		session.close();
		
		System.out.println(order.getValue("id"));
	}
	
	public static void testUpdate() throws Exception{
		
		BaseData order = DataUtil.createBaseData("order");
		
		order.setValue("id", 1);
		order.setValue("totalPrice", 28);
		
		SimpleSession session = SessionFactory.getSimpleSession();
		
		session.begian();
		session.update(order);
		session.commit();
		session.close();
	}
	
	public static void testDelete() throws Exception{
		
		BaseData order = DataUtil.createBaseData("order");
		
		order.setValue("id", 6365);
		
		SimpleSession session = SessionFactory.getSimpleSession();
		
		session.begian();
		session.delete(order);
		session.commit();
		session.close();
	}
	
	public static void testGet() throws Exception{
		
		BaseData order = DataUtil.createBaseData("order");
		
		order.setValue("id", 40);
		order.setValue("userId", 85);
		SimpleSession session = SessionFactory.getSimpleSession();
		
		session.begian();
		BaseData data =  session.get(order);
		System.out.println(data.getValue("id"));
		System.out.println(data.getValue("userId"));
		System.out.println(data.getValue("count"));
		System.out.println(data.getValue("totalPrice"));
		session.close();
	}
	
	public static void test2() throws XMLParseException{
		
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("id", 1);
		
		QuerySQLConvert mySQLQueryConvert = new QuerySQLConvert();
		mySQLQueryConvert.setDataSource("data1");
		
		String sql = "select a.id as id,b.name as name,(select d.userId from order d where d.id = a.id) from user as b,order as a where a.userId = b.id " +
				"[and a.id = #dd ] and a.id in (select b.id from user as b) [and b.id = #id] ";
		
		ConvertParam convertParam = new ConvertParam();
		convertParam.setSql(sql);
		convertParam.setData(map);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
		System.out.println(format.format(new Date()));
		
		sql = mySQLQueryConvert.convert(convertParam).getCmd();
		
		System.out.println(sql);
		
		System.out.println(format.format(new Date()));
	}
	
	public static void test3() throws XMLParseException{
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("name", "szl");
		QuerySQLConvert mySQLQueryConvert = new QuerySQLConvert();
		mySQLQueryConvert.setDataSource("data1");
		
		mySQLQueryConvert.setReplaceColumnFlag(true);
		String sql = "select b.id,b.name from user as b where [b.name = #name] ";
		
		ConvertParam convertParam = new ConvertParam();
		convertParam.setSql(sql);
		convertParam.setData(map);
		
		
		sql = mySQLQueryConvert.convert(convertParam).getCmd();
		
		System.out.println(sql);
	}

	public static void test4() throws XMLParseException{
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("name", "szl");
		DeleteSQLConvert mySQLQueryConvert = new DeleteSQLConvert();
		mySQLQueryConvert.setDataSource("data1");
		
		String sql = "delete from user as a,order as b where  [and b.id = #id] [ a.name = #name ]";
		ConvertParam convertParam = new ConvertParam();
		convertParam.setSql(sql);
		convertParam.setData(map);
		
		
		sql = mySQLQueryConvert.convert(convertParam).getCmd();
		
		System.out.println(sql);
	}
	
	public static void test5() throws XMLParseException{
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("name", "szl");
		UpdateSQLConvert mySQLQueryConvert = new UpdateSQLConvert();
		mySQLQueryConvert.setDataSource("data1");
		
		String sql = "update user set name = '1' where [ name = #name and ]  id in (select o.id from order as o ) ";
		ConvertParam convertParam = new ConvertParam();
		convertParam.setSql(sql);
		convertParam.setData(map);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
		System.out.println(format.format(new Date()));
		
		sql = mySQLQueryConvert.convert(convertParam).getCmd();
		
		System.out.println(sql);
		
		System.out.println(format.format(new Date()));
	}
	
	public static void testQuery1() throws Exception{
		
		BaseData order = DataUtil.createBaseData("order");
		
		order.setValue("id", 1);
		order.setValue("userId", 2);
		SimpleDataQuery query = QueryFactory.createSimpleDataQuery();
		List<BaseData> list = new ArrayList<BaseData>();
		
		query.addQueryInfo("select b.id,b.userId from order as b where b.count = 2", order, list);
		query.executeQuery();
		System.out.println(list.size());
	}
	
	public static void testQuery2() throws Exception{
		
		BaseData order = DataUtil.createBaseData("order");
		
		order.setValue("id", 1);
		order.setValue("userId", 2);
		
		SimpleDataQuery query = QueryFactory.createSimpleDataQuery();
		
		List<BaseData> list = new ArrayList<BaseData>();
		
		query.addQueryInfo("select b.id,b.userId from order as b where [b.count = #id*2]", order, list);
		query.executeQuery();
		
		System.out.println(list.size());
	}
	
	public static void testOrignUpdate1() throws Exception{
		
		SimpleSession session = SessionFactory.getSimpleSession();
		
		
		session.begian();
		session.executeOrignUpdate("update orderinfo set o_count = 3");
		session.commit();
		session.close();
	}
	
	public static void testOrignUpdate2() throws Exception{
		
		SimpleSession session = SessionFactory.getSimpleSession();

		Map<String,Object> map = new HashMap<String,Object>();
		map.put("count", 4);
		
		session.begian();
		session.executeOrignUpdate("update orderinfo set o_count = #count",map);
		session.commit();
		session.close();
	}
	
	public static void testOrignUpdate3() throws Exception{
		
		SimpleSession session = SessionFactory.getSimpleSession("con1");

		
		
		session.begian();
		session.executeOrignUpdate("delete from productinfo");
		session.executeOrignUpdate("delete from orderinfo");
		session.executeOrignUpdate("delete from userinfo");
		
		session.commit();
		session.close();
	}
	/*public static void testNumber(){
		Number i = 1;
		Number j= -1;
		
		NotEqual notEqual = new NotEqual();
		notEqual.check(1, 1);
		System.out.println(notEqual.check(1, -1));
	}*/
}
