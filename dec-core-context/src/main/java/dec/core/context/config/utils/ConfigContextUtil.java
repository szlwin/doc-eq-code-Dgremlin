package dec.core.context.config.utils;

import java.util.Map;

import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.data.Column;
import dec.core.context.config.model.data.Data;
import dec.core.context.config.model.data.DataTable;
import dec.core.context.config.model.data.TableInfo;
import dec.core.context.config.model.datasource.DataSource;

/*
import com.orm.api.dom.FieldInfo;
import com.orm.common.xml.model.config.datasource.DataSource;
import com.orm.common.xml.model.data.Column;
import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.data.DataTable;
import com.orm.common.xml.model.data.TableInfo;
import com.orm.context.config.manager.ConfigManager;
import com.orm.context.config.model.config.ConfigInfo;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.context.data.NullData;
import com.orm.context.data.ToolUtil;
import com.orm.sql.datatype.convert.DataTypeConvert;
import com.orm.sql.dom.DataInfo;*/


public class ConfigContextUtil {
	
	public static ConfigInfo getConfigInfo(){
		return ConfigManager.getInstance().getConfigInfo();
	}

	public static DataTable getTableInfo(String name,String dataSourceName){
		
		//��ȡ��ӳ���ϵ
		ConfigInfo configInfo = getConfigInfo();
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
	
	
	public static String getSqlType(String name){
		DataSource<?> dataSource = getConfigInfo().getDataSource(name);
		
		return dataSource.getType();
	}
	
	public static String getDefaultCon(){
		return ConfigManager.getInstance().getDefaultConName();
	}
	
	public static DataSource<?> getDataSourceByCon(String conName){
		return ConfigManager.getInstance().getDataSourceByCon(conName);
	}
	
	public static DataSource<?> getDataSourceByName(String dataSrcName){
		return ConfigManager.getInstance().getDataSourceByName(dataSrcName);
	}
	
	public static DataSource<?> getDataSource(){
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
		ConfigInfo configInfo = getConfigInfo();
		Data dataConfig = configInfo.getData(name);
		
		return dataConfig;
	}
	
	
	public static String getTableName(String name,String dataSourceName){
		return getTableInfo(name, dataSourceName).getName();
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
