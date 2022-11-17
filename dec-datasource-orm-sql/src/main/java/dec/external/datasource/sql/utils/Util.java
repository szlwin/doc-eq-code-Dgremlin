package dec.external.datasource.sql.utils;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dec.core.context.config.manager.ConfigManager;

//import com.orm.common.xml.model.config.datasource.DataSource;
//import com.orm.common.xml.util.ConfigManager;

/*
import com.orm.common.xml.model.config.ConfigInfo;
import com.orm.common.xml.model.config.datasource.DataSource;
import com.orm.common.xml.model.data.Column;
import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.data.DataTable;
import com.orm.common.xml.model.data.TableInfo;
import com.orm.common.xml.util.ConfigManager;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.context.data.NullData;
import com.orm.context.data.ToolUtil;
import com.orm.sql.datatype.convert.DataTypeConvert;
import com.orm.sql.dom.DataInfo;
import com.orm.api.dom.FieldInfo;*/


import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.data.Column;
import dec.core.context.config.model.data.Data;
import dec.core.context.config.model.data.DataTable;
import dec.core.context.config.model.data.TableInfo;
import dec.core.context.config.model.datasource.DataSource;
import dec.core.context.config.utils.ConfigContextUtil;
import dec.core.context.data.BaseData;
import dec.core.context.data.ModelData;
import dec.core.context.data.NullData;
import dec.core.datasource.dom.DataInfo;
import dec.external.datasource.sql.collections.list.SimpleList;
import dec.external.datasource.sql.datatype.convert.DataTypeConvert;



public class Util {

	public static Map<String,Column> convert(String dataName,String dataSourceName){
		
		DataTable dataTable = getTableInfo(dataName,dataSourceName);
		
		Map<String,Column> colMap = dataTable.getColumns();
		return colMap;
	}
	
	public static Collection<DataInfo> convert(BaseData data,String dataSourceName){
		
		return convert(data,dataSourceName,false);
	}
	
	public static Collection<DataInfo> convert(BaseData data,String dataSourceName,boolean isReomveNull){
		List<DataInfo> dataSet = new SimpleList<DataInfo>();
		
		DataTable dataTable = getTableInfo(data.getName(),dataSourceName);
		
		Map<String, Object> dataMap = getValues(data);
		
		//Set<String> keySet = dataMap.keySet();
		Set<String> keySet = dataTable.getColumns().keySet();
		Iterator<String> it = keySet.iterator();
		
		//boolean isIncrement = dataTable.getKeyType().toLowerCase().equals("increment");
		//String keyId = dataTable.getKey();
		
		while(it.hasNext()){
			DataInfo dataInfo = new DataInfo();
			
			String key = it.next();
			
			//DataProperty dataProperty = propertyInfo.getProperty(key);
			
			Column column = dataTable.getColumn(key);
			
			String columnName =column.getName();
			
			//if(columnName.equals(key) && isIncrement)
			//	continue;
			Object value = null;
			
			Object dataValue = dataMap.get(key);

			if(isReomveNull){
				if(dataValue == null)
					continue;
				
				if(value instanceof NullData)
					dataInfo.setValue(null);
			}
			String convertFun = column.getConvertFun();
			
			if(convertFun!=null && !"".equals(convertFun) && dataValue != null){
				value = DataTypeConvert.convert(dataValue, convertFun,dataSourceName);
			}else{
				value = dataValue;
			}
			
			String propertyName = column.getRefproperty();
			//String dataType = dataProperty.getType();
			
			//String strValue = getSqlValueString(value,dataType);
			
			dataInfo.setProterty(propertyName);
			dataInfo.setColumn(columnName);
			dataInfo.setValue(value);
			
			dataSet.add(dataInfo);
		}
		return dataSet;
	}
	
	public static DataTable getTableInfo(String name,String dataSourceName){
		
		//��ȡ��ӳ���ϵ
		ConfigInfo configInfo = ConfigContextUtil.getConfigInfo();
		Data dataConfig = configInfo.getData(name);
		
		//��ȡ������
		TableInfo tableInfo = dataConfig.getTableInfo();
		DataTable dataTable = tableInfo.getTable(dataSourceName);
		
		return dataTable;
		
	}

	public static String getTableName(String name,String dataSourceName){
		return getTableInfo(name, dataSourceName).getName();
	}
	
	public static Data getDataInfo(String name){
		//��ȡ��ӳ���ϵ
		ConfigInfo configInfo = ConfigContextUtil.getConfigInfo();
		Data dataConfig = configInfo.getData(name);
		
		return dataConfig;
	}
	
	@SuppressWarnings("unchecked")
	public static Object getValueByKey(String key,Object obj){
		if(obj instanceof Map)
			return getValueByKey(key,(Map<String,Object>)obj);
		
		if(obj instanceof ModelData)
			return getValueByKey(key,((ModelData)obj).getValues());
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static Object getValueByKey(String key,Map<String,Object> map){
		String keyArray[] = key.split("\\.");
		
		if(keyArray.length == 1){
			if(map.containsKey(key)){
				Object value = map.get(key);
				if(value == null)
					return new NullData();
				else
					return value;
				
			}else{
				return null;
			}
		}
			
		Map<String,Object> valueMap = map;
		for(int i = 0; i < keyArray.length-1; i++){
			if(valueMap.containsKey(keyArray[i]))
				valueMap = (Map<String,Object>)valueMap.get(keyArray[i]);
		}
		
		if(valueMap.containsKey(keyArray[keyArray.length-1])){
			Object value = valueMap.get(keyArray[keyArray.length-1]);
			if(value == null)
				return new NullData();
			else
				return value;
		}
		else
			return null;
	}
	
	public static Map<String,Object> getValues(BaseData data){
		if(data == null)
			return null;
		return data.getValues();
	}
	
	public static DataSource<?> getDataSourceByName(String dataSrcName){
		return ConfigManager.getInstance().getDataSourceByName(dataSrcName);
	}
	
	public static void convertRsToBaseData(ResultSet rs,BaseData data) throws SQLException{
		
		//Map<String,Object> dataMap = ToolUtil.getValues(data);
		//Set<String> keySet = dataMap.keySet();
		//Iterator<String> it = keySet.iterator();
		ResultSetMetaData rsMeta = rs.getMetaData();
		int columnCount = rsMeta.getColumnCount();
		
		for(int i = 1; i <= columnCount; i++){
			String key = rsMeta.getColumnLabel(i);
			Object value = rs.getObject(key);
			
			//if(value instanceof oracle.sql.DATE){
			//	value = new java.sql.Date(((oracle.sql.DATE)value).timestampValue().getTime());
			//}else if(value instanceof oracle.sql.TIMESTAMP){
			//	value = new java.sql.Timestamp(((oracle.sql.TIMESTAMP)value).timestampValue().getTime());
			//}
			
			data.setValue(key, value);
		}
	}
}
