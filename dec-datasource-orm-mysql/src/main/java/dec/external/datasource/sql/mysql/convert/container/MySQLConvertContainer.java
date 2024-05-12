package dec.external.datasource.sql.mysql.convert.container;

import java.util.Map;

import dec.external.datasource.sql.convert.SqlConvert;
import dec.external.datasource.sql.convert.common.QuerySQLConvert;
import dec.external.datasource.sql.convert.common.UpdateSQLConvert;
import dec.external.datasource.sql.convert.container.AbstractSqlConvertContainer;
import dec.external.datasource.sql.convert.origin.OrignSQLConvert;
import dec.external.datasource.sql.convert.single.SingleDeleteConvert;
import dec.external.datasource.sql.convert.single.SingleGetConvert;
import dec.external.datasource.sql.convert.single.SingleUpdateConvert;
import dec.external.datasource.sql.dom.ConvertParam;
import dec.external.datasource.sql.mysql.convert.single.SingleInsertConvert;
import javolution.util.FastMap;


public class MySQLConvertContainer extends AbstractSqlConvertContainer{

	private static Map<String,Class<? extends SqlConvert>> convertMap = new FastMap<String,Class<? extends SqlConvert>>();
	
	private static Map<String,Class<? extends SqlConvert>> convertSimpleMap = new FastMap<String,Class<? extends SqlConvert>>();
	
	static{
		convertMap.put("delete", dec.external.datasource.sql.mysql.convert.common.DeleteSQLConvert.class);
		convertMap.put("query", MySQLConvert.class);
		convertMap.put("update", UpdateSQLConvert.class);
		convertMap.put("get", QuerySQLConvert.class);
		
		convertSimpleMap.put("insert", SingleInsertConvert.class);
		convertSimpleMap.put("delete",SingleDeleteConvert.class);
		convertSimpleMap.put("get", SingleGetConvert.class);
		convertSimpleMap.put("update", SingleUpdateConvert.class);
	}
	//private Map<String,Class<? extends SqlConvert>> convertOrignMap = new HashMap<String,Class<? extends SqlConvert>>(1);
	
	public void init() {

		
		//convertOrignMap.put("common", OrignSQLConvert.class);
	}



	@Override
	protected SqlConvert getConvert(ConvertParam e) {
		SqlConvert convert = null;
		Class<? extends SqlConvert> convertClass = null;
		
		if(e.getIsOrign()){
			convertClass = OrignSQLConvert.class;
		}else if(e.getSql() == null || "".equals(e.getSql()))
			convertClass = convertSimpleMap.get(e.getType());
		else
			convertClass = convertMap.get(e.getType());
		try {
			convert = convertClass.newInstance();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		convert.setDataSource(e.getDataSource());
		return convert;
	}
}
