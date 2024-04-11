package dec.external.datasource.sql.mysql.datatype.convert;

import java.util.Map;

import dec.core.datasource.datatype.convert.DataConvertContainer;
import javolution.util.FastMap;
import dec.core.datasource.datatype.convert.DataConvert;

public class MySQLDataTypeConvert implements DataConvertContainer{

	private final static Map<String, DataConvert<?,?>> funMap
		= new FastMap<String, DataConvert<?,?>>();
	
	private final static Map<String, DataConvert<?,?>> funrMap
		= new FastMap<String, DataConvert<?,?>>();
	
	static{
		
		 
		funMap.put("date_time", new DateToSqlTime());
		funMap.put("date_timestamp", new DateToSqlTimestamp());
		
		funrMap.put("date_date", new SqlDateToDate());
		funrMap.put("date_time", new SqlTimeToDate());
		funrMap.put("date_timestamp", new SqlTimestampToDate());

	}

	public Object convertToDataSource(Object data,String type, String originType){
		return convert(data, type, originType, true);
	}

	public Object convertFromDataSource(Object data,String type, String originType){
		return convert(data, type, originType, false);
	}

	private Object convert(Object data,String type, String originType, boolean isTo){
		if(data == null)
			return null;
		DataConvert convert = null;

		if(isTo){
			convert = funMap.get(originType+"_"+type);
		}else{
			convert = funrMap.get(originType+"_"+type);
		}
		
		return convert.convert(data);
		
	}
	
	/*public Object convert(Object data,String fun){
		return convert(data,fun,true);
	}*/
}
