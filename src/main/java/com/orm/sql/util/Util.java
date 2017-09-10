package com.orm.sql.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.orm.api.dom.FieldInfo;

public class Util {

	public static DataTable getTableInfo(String name,String dataSourceName){
		
		//��ȡ��ӳ���ϵ
		ConfigInfo configInfo = DataUtil.getConfigInfo();
		Data dataConfig = configInfo.getData(name);
		
		//��ȡ������
		TableInfo tableInfo = dataConfig.getTableInfo();
		DataTable dataTable = tableInfo.getTable(dataSourceName);
		
		return dataTable;
		
	}
	
	public static Map<String,Column> convert(String dataName,String dataSourceName){
		
		DataTable dataTable = getTableInfo(dataName,dataSourceName);
		
		Map<String,Column> colMap = dataTable.getColumns();
		return colMap;
	}
	
	
	public static Collection<FieldInfo> convertFieldInfo(BaseData data,String dataSourceName){
		List<FieldInfo> dataSet = new ArrayList<FieldInfo>(20);
		
		DataTable dataTable = getTableInfo(data.getName(),dataSourceName);
		
		//Set<String> keySet = dataMap.keySet();
		Set<String> keySet = dataTable.getColumns().keySet();
		Iterator<String> it = keySet.iterator();
		
		//boolean isIncrement = dataTable.getKeyType().toLowerCase().equals("increment");
		//String keyId = dataTable.getKey();
		
		while(it.hasNext()){
			FieldInfo fieldInfo = new FieldInfo();
			
			String key = it.next();
			
			Column column = dataTable.getColumn(key);
			
			String columnName =column.getName();
			
			fieldInfo.setColumn(columnName);
			fieldInfo.setProperty(column.getRefproperty());
			dataSet.add(fieldInfo);
		}
		return dataSet;
	}
	
	public static Collection<DataInfo> convert(BaseData data,String dataSourceName,boolean isReomveNull){
		List<DataInfo> dataSet = new ArrayList<DataInfo>(20);
		
		DataTable dataTable = getTableInfo(data.getName(),dataSourceName);
		
		Map<String, Object> dataMap = ToolUtil.getValues(data);
		
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
	
	public static Collection<DataInfo> convert(BaseData data,String dataSourceName){
		
		return convert(data,dataSourceName,false);
	}
	
	public static String getSqlValueString(Object value,String type){
		StringBuffer strValue = new StringBuffer();
		
		if(type.equals("string")){
			strValue.append("'");
			strValue.append(value);
			strValue.append("'");
		}else{
			strValue.append(value);
		}
		
		return strValue.toString();
	}
	
	public static String getSqlType(String name){
		DataSource dataSource = DataUtil.getConfigInfo().getDataSource(name);
		
		return dataSource.getType();
	}
	
	public static String getDefaultCon(){
		return ConfigManager.getInstance().getDefaultConName();
	}
	
	public static DataSource getDataSourceByCon(String conName){
		return ConfigManager.getInstance().getDataSourceByCon(conName);
	}
	
	public static DataSource getDataSourceByName(String dataSrcName){
		return ConfigManager.getInstance().getDataSourceByName(dataSrcName);
	}
	
	public static DataSource getDataSource(){
		String connectionName = getDefaultCon();
		return ConfigManager.getInstance().getDataSourceByCon(connectionName);
	}
	/*
	public static String getIdKey(BaseData data,String dataSourceName){
		
		//��ȡ��ӳ���ϵ
		//ConfigInfo configInfo = ConfigManager.getInstance().getConfigInfo();
		Data dataConfig = data.getData();

		//��ȡ������
		TableInfo tableInfo = dataConfig.getTableInfo();
		DataTable dataTable = tableInfo.getTable(dataSourceName);
		
		return dataTable.getPropertyKey();
	}*/
	
	public static Data getDataInfo(String name){
		//��ȡ��ӳ���ϵ
		ConfigInfo configInfo = DataUtil.getConfigInfo();
		Data dataConfig = configInfo.getData(name);
		
		return dataConfig;
	}
	
	public static DataTable getDataTable(BaseData data,String dataSourceName){
		//��ȡ��ӳ���ϵ
		//ConfigInfo configInfo = ConfigManager.getInstance().getConfigInfo();
		Data dataConfig = data.getData();

		//��ȡ������
		TableInfo tableInfo = dataConfig.getTableInfo();
		DataTable dataTable = tableInfo.getTable(dataSourceName);
		
		return dataTable;
	}
	
	public static String getTableName(String name,String dataSourceName){
		return getTableInfo(name, dataSourceName).getName();
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
	
	public static void convertRsToMap(ResultSet rs,Map<String,Object> map) throws SQLException{
		
		//Map<String,Object> dataMap = ToolUtil.getValues(data);
		//Set<String> keySet = dataMap.keySet();
		//Iterator<String> it = keySet.iterator();
		ResultSetMetaData rsMeta = rs.getMetaData();
		int columnCount = rsMeta.getColumnCount();
		
		for(int i = 1; i <= columnCount; i++){
			String key = rsMeta.getColumnLabel(i).toLowerCase();
			
			Object value = rs.getObject(key);
			
			//if(value instanceof oracle.sql.DATE){
			//	value = new java.sql.Date(((oracle.sql.DATE)value).timestampValue().getTime());
			//}else if(value instanceof oracle.sql.TIMESTAMP){
			//	value = new java.sql.Timestamp(((oracle.sql.TIMESTAMP)value).timestampValue().getTime());
			//}
			
			map.put(key, value);
		}
	}
	
	/*
	public static RelationView getRelationView(String name,String propertyName){
		RelationView relationView = getRelationInfo(name).getRelation(propertyName);
		return relationView;
	}*/
	
	/*public static ViewData getViewData(String name){
		ConfigInfo configInfo = ConfigManager.getInstance().getConfigInfo();
		
		return configInfo.getViewData(name);
	}*/
	
	/*
	public static RelationInfo getRelationInfo(String name){

		RelationInfo relationInfo = getViewData(name).getRelationInfo();
		
		return relationInfo;
	}*/
	/*
	public static RuleViewInfo getRuleViewInfo(String name){
		ConfigInfo configInfo = ConfigManager.getInstance().getConfigInfo();
		
		RuleViewInfo ruleViewInfo = configInfo.getRuleViewInfo(name);
		return ruleViewInfo;
	}*/
	/*
	public static String getKeyPropertyName(String dataName,String dataSource){
		Data dataInfo = getDataInfo(dataName);
		return dataInfo.getTableInfo()
					.getTable(dataSource)
					.getPropertyKey();
	}*/
	/*
	public static Relation getRelation(String name){
		ConfigInfo configInfo = ConfigManager.getInstance().getConfigInfo();
		return configInfo.getRelation(name);
	}*/
	
	public static String getDataSourceNameByCon(String conName){
		return getDataSourceByCon(conName).getName();
	}
	/*
	public static String getIdKeyType(String dataName,String dataSourceName){
		DataTable dataTable = getTableInfo(dataName,dataSourceName);
		
		return dataTable.getKeyType();
	}*/
	
	public static String getConNameByDataSource(String dataSourceName){
		return getDataSourceByName(dataSourceName).getConName();
	}
	
	public static String getSeqName(String dataName,String dataSourceName){
		DataTable dataTable = getTableInfo(dataName,dataSourceName);
		return dataTable.getSeq();
	}
}
