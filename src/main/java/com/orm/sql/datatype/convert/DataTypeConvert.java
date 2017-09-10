package com.orm.sql.datatype.convert;

import com.orm.context.data.DataUtil;
import com.orm.sql.util.Util;

public class DataTypeConvert {

	//private static final ThreadLocal<ConvertContainer> typeContainerLocal = new ThreadLocal<ConvertContainer>();
	
	public static Object convert(Object data,String fun,String dataSource,boolean isTo){
		if(data == null)
			return data;
		
		String conName = Util.getDataSourceByName(dataSource).getConName();
		
		ConvertContainer convertContainer = DataUtil.getConfigInfo().getConnection(conName)
				.getConnectionInfo().getDataConvertContainer();
		//return typeContainerLocal.get()
		//		.convert(data, fun, isTo);
		
		return convertContainer.convert(data, fun, isTo);		
	}
	
	public static Object convert(Object data,String fun,String dataSource){
		return convert(data,fun,dataSource,true);
	}
	
	//public static void setDataSourceType(String dataSource){
	//	String type = DataUtil.getConfigInfo().getDataSource(dataSource).getType();

	//	typeContainerLocal.set(DataTypeConvertFactory.getConvertContainer(type));
	//}
}
