package com.orm.common.xml.parse.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.orm.common.config.Config;
import com.orm.common.config.DataSourceConfig;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.ConnectionInfo;
import com.orm.common.xml.model.config.datasource.DataSource;
import com.orm.common.xml.model.data.Column;
import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.data.DataProperty;
import com.orm.common.xml.model.data.DataTable;
import com.orm.common.xml.model.data.PropertyInfo;
import com.orm.common.xml.model.data.TableInfo;
import com.orm.common.xml.parse.AbstarctElementsParser;
import com.orm.common.xml.util.Constanst;
import com.orm.context.data.DataUtil;

public class DataParser extends AbstarctElementsParser{

	private static Map<String,Object> dataTypeMap = new HashMap<String,Object>();
	
	static{
		dataTypeMap.put("int", null);
		dataTypeMap.put("long", null);
		dataTypeMap.put("double", null);
		dataTypeMap.put("float", null);
		dataTypeMap.put("string", null);
		dataTypeMap.put("date", null);
		dataTypeMap.put("boolean", null);
		dataTypeMap.put("char", null);
	}
	
	Log log = LogFactory.getLog(DataParser.class);
	
	public Data parse(Element element) throws XMLParseException {
		
		Data data = new Data();
		
		data.setName(element.attributeValue(Data.NAME));
		
		log.info("Load the data:"+data.getName()+" start!");
		
		Object dataObj = DataUtil.getConfigInfo().get(Config.DATA, data.getName());
		
		if(dataObj != null){
			throw new XMLParseException("The data:"+data.getName()+" is existed!");
		}
		
		data.setClassName(Data.CLASS);
		
		PropertyInfo propertyInfo = parsePropertyInfo(element);
		
		data.setPropertyInfo(propertyInfo);
		
		TableInfo tableInfo = parseTableInfo(propertyInfo,element);
		
		data.setTableInfo(tableInfo);
		
		log.info("Load the data:"+data.getName()+" success!");
		return data;
	}
	
	private boolean checkDataType(String type){
		return dataTypeMap.containsKey(type);
	}
	
	@SuppressWarnings("rawtypes")
	private PropertyInfo parsePropertyInfo(Element element) throws XMLParseException{
		PropertyInfo propertyInfo = new PropertyInfo();
		Element propertyInfoElement = element.element(Data.PROPERTY_INFO);
		
		Iterator propertyList  = propertyInfoElement.elementIterator(PropertyInfo.PROERTY);
		while(propertyList.hasNext()){
			Element protertyElement = (Element)propertyList.next();
			DataProperty property = new DataProperty();
			
			property.setName(protertyElement.attributeValue(DataProperty.NAME));
			
			String type = protertyElement.attributeValue(DataProperty.TYPE);
			
			if(!checkDataType(type)){
				throw new XMLParseException("The type:"+type+" is not supported!");
			}
			
			property.setType(type);
			
			propertyInfo.addProperty(property);
		}
		
		return propertyInfo;
		
	}

	@SuppressWarnings("rawtypes")
	private TableInfo parseTableInfo(PropertyInfo propertyInfo,Element element) throws XMLParseException{
		TableInfo tableInfo = new TableInfo();
		Element tableInfoElement = element.element(Data.TABLE_INFO);
		
		Iterator propertyList = tableInfoElement.elementIterator(TableInfo.TABLE);
		
		while(propertyList.hasNext()){
			Element tableElement = (Element)propertyList.next();
			DataTable table = new DataTable();
			table.setName(tableElement.attributeValue(DataTable.NAME));
			table.setKey(tableElement.attributeValue(DataTable.KEY));
			
			String keyType = tableElement.attributeValue(DataTable.KEY_TYPE);
			
			if(keyType == null && "".equals(keyType))
				table.setKeyType(Constanst.KEY_TYPE_DAFAULT);
			else
				table.setKeyType(keyType);
			
			if(keyType != null && keyType.equals(Constanst.KEY_TYPE_SEQ)){
				table.setSeq(tableElement.attributeValue(DataTable.SEQ));
			}
			
			//table.setCon(tableElement.attributeValue("con"));
			
			//table.setDataSourceName(Util.getDataSourceByCon(table.getCon()).getName());
			String dataSourceName = tableElement.attributeValue(DataTable.DATA_SOURCE);
			
			if(DataSourceConfig.getInstance().get(dataSourceName) == null){
				throw new XMLParseException("The data source:"+dataSourceName+" is not existed!");
			}
			
			table.setDataSourceName(dataSourceName);
			
			Iterator columnList = tableElement.elementIterator(DataTable.COLUMN);
			
			while(columnList.hasNext())
			{
				Element columnElement = (Element)columnList.next();
				Column column = parseColumn(dataSourceName,propertyInfo,columnElement);
				if(column.getName().equals(table.getKey())){
					table.setPropertyKey(column.getRefproperty());
				}
				table.addColumn(column);
			}
			
			tableInfo.addTable(table);
			
		}
		
		return tableInfo;
		
	}
	
	private Column parseColumn(String dataSourceName,PropertyInfo propertyInfo,Element element) throws XMLParseException{
		Column column = new Column();
		
		column.setName(element.attributeValue(Column.NAME));
		column.setRefproperty(element.attributeValue(Column.REF_PROPERTY));
		
		String type = element.attributeValue(Column.TYPE);
		
		if(type != null && !"".equals(type)){
			String oriType = propertyInfo.getProperty(column.getRefproperty()).getType();
			
			if(!checkIsCanConvert(dataSourceName,oriType,type))
				throw new XMLParseException("The data source:"+dataSourceName+", the type:"+oriType+" is can't be convert to:"+type+" for column:"+column.getName());
			
			column.setType(type);
			column.setConvertFun(oriType+"_"+type);
		}
		return column;
	}
	
	private boolean checkIsCanConvert(String dataSoureName,String oriType,String targetType){
		DataSource dataSource = (DataSource) DataUtil.getConfigInfo().get(Config.DATASOURCE, dataSoureName);
		ConnectionInfo connectionInfo = (ConnectionInfo) DataUtil.getConfigInfo().get(Config.CONNECTION_CONFIG, dataSource.getType());
		return connectionInfo.getDataConvertContainer().check(oriType, targetType);
	}
}
