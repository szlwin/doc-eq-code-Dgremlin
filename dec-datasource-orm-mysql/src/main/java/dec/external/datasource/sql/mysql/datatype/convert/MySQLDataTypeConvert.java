package dec.external.datasource.sql.mysql.datatype.convert;

import java.util.Map;

import dec.core.datasource.datatype.convert.DataConvertContainer;
import javolution.util.FastMap;
import dec.core.datasource.datatype.convert.DataConvert;

//import com.orm.sql.datatype.convert.ConvertContainer;
//import com.orm.sql.datatype.convert.DataConvert;

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
		
		
		funMap.put("date_time", new DateToSqlTime());
		funMap.put("date_timestamp", new DateToSqlTimestamp());
		
		funrMap.put("date_date", new SqlDateToDate());
		funrMap.put("date_time", new SqlTimeToDate());
		funrMap.put("date_timestamp", new SqlTimestampToDate());
	}
	
	public Object convert(Object data,String fun,boolean isTo){
		if(data == null)
			return data;
		DataConvert convert = null;
		
		
		if(isTo){
			convert = funMap.get(fun);
			//throw new RuntimeException();
		}else{
			convert = funrMap.get(fun);
			//throw new RuntimeException();
		}
		
		return convert.convert(data);
		
	}
	
	public Object convert(Object data,String fun){
		return convert(data,fun,true);
	}

	public void init() {

	}
	
	public boolean check(String oriType, String targetType) {
		// TODO Auto-generated method stub
		return funMap.containsKey(oriType+"_"+targetType);
	}
}
