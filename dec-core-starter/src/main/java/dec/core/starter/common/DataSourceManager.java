package dec.core.starter.common;

import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.datasource.DataSourceConfigInfo;
import dec.core.context.config.utils.ConfigContextUtil;
import dec.core.datasource.connection.factory.DBConectionFacory;
import dec.core.datasource.convert.container.factory.ConvertContainerFacory;
import dec.core.datasource.datatype.convert.factory.DataConvertContainerFacory;
import dec.core.datasource.execute.container.factory.ExecuteContainerFacory;
import dec.core.model.connection.DataConnectionFactory;

public class DataSourceManager {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addDataSource(String name, Object dataSource) throws Exception{
		
		dec.core.context.config.model.datasource.DataSource dataSourceModel 
			=  ConfigContextUtil.getConfigInfo().getDataSource(name);
		
		dec.core.datasource.connection.datasource.DataSource dataSourceContainer;
		
		try {
			
			ConfigInfo configInfo = ConfigContextUtil.getConfigInfo();
			
			DataSourceConfigInfo dataSourceConfigInfo 
				= (DataSourceConfigInfo) configInfo.get(Config.DATASOURCE_CONFIG,dataSourceModel.getType());
			
			String dClass = dataSourceConfigInfo.getDataSource();
			
			dataSourceContainer = (dec.core.datasource.connection.datasource.DataSource) Class.forName(dClass).newInstance();
			
			dataSourceContainer.setDataSource(dataSource);
			
			dataSourceModel.setDataSource(dataSourceContainer);
		} catch (Exception e) {
			throw e;
		} 
		
	}
	
	
	public static void addConnectionFactory(String name, DBConectionFacory<?,?> factory){
		
		DataConnectionFactory.getInstance().addConnectionFactory(name, factory);
		
		
	}
	
	public static void addConvertContainerFactory(String name, ConvertContainerFacory<?,?> factory){
		
		DataConnectionFactory.getInstance().addConvertContainerFactory(name, factory);
	}
	
	public static void addDataConvertContainerFacory(String name, DataConvertContainerFacory factory){
		
		DataConnectionFactory.getInstance().addDataConvertContainerFacory(name, factory);
	}

	public static void addExecuteContainerFacory(String name, ExecuteContainerFacory factory){
		
		DataConnectionFactory.getInstance().addExecuteContainerFacory(name, factory);
	}
}
