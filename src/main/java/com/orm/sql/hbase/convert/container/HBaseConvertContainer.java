package com.orm.sql.hbase.convert.container;

import java.util.HashMap;
import java.util.Map;

import com.orm.sql.dom.ConvertParam;
import com.orm.sql.hbase.convert.single.SingleUpsertConvert;
import com.orm.sql.mysql.convert.single.SingleInsertConvert;
import com.orm.sql.convert.SqlConvert;
import com.orm.sql.convert.common.DeleteSQLConvert;
import com.orm.sql.convert.common.UpdateSQLConvert;
import com.orm.sql.convert.common.QuerySQLConvert;
import com.orm.sql.convert.container.AbstractSqlConvertContainer;
import com.orm.sql.convert.orign.OrignSQLConvert;
import com.orm.sql.convert.single.SingleDeleteConvert;
import com.orm.sql.convert.single.SingleGetConvert;
import com.orm.sql.convert.single.SingleUpdateConvert;

public class HBaseConvertContainer extends AbstractSqlConvertContainer{

	private Map<String,Class<? extends SqlConvert>> convertMap = new HashMap<String,Class<? extends SqlConvert>>(3);
	
	private Map<String,Class<? extends SqlConvert>> convertSimpleMap = new HashMap<String,Class<? extends SqlConvert>>(4);
	
	//private Map<String,Class<? extends SqlConvert>> convertOrignMap = new HashMap<String,Class<? extends SqlConvert>>(1);
	
	public void init() {
		convertMap.put("delete", DeleteSQLConvert.class);
		convertMap.put("query", QuerySQLConvert.class);
		//convertMap.put("update", UpdateSQLConvert.class);
		convertMap.put("get", QuerySQLConvert.class);
		
		convertSimpleMap.put("insert", SingleUpsertConvert.class);
		convertSimpleMap.put("delete", SingleDeleteConvert.class);
		convertSimpleMap.put("get", SingleGetConvert.class);
		convertSimpleMap.put("update", SingleUpsertConvert.class);
		
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
			e1.printStackTrace();
		}
		convert.setDataSource(e.getDataSource());
		return convert;
	}
}
