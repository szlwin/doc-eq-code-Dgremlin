package dec.core.model.connection;

import java.util.Map;

import dec.core.context.config.model.connection.Connection;
import dec.core.context.config.model.connection.ConnectionInfo;
import dec.core.context.config.utils.ConfigContextUtil;
import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.connection.exception.ConectionException;
import dec.core.datasource.connection.factory.DBConectionFacory;
import dec.core.datasource.convert.container.ConvertContainer;
import dec.core.datasource.convert.container.factory.ConvertContainerFacory;
import dec.core.datasource.datatype.convert.DataConvertContainer;
import dec.core.datasource.datatype.convert.factory.DataConvertContainerFacory;
import dec.core.datasource.execute.container.ExecuteContainer;
import dec.core.datasource.execute.container.factory.ExecuteContainerFacory;
import javolution.util.FastMap;

//import com.orm.common.xml.model.config.ConnectionInfo;
//import com.orm.common.xml.model.config.connection.Connection;
//import com.orm.connection.DataConnection;
//import com.orm.connection.exception.ConectionException;
//import com.orm.context.data.DataUtil;
//import com.orm.sql.util.Util;

public class DataConnectionFactory {

	private static final DataConnectionFactory connectionFactory 
		= new DataConnectionFactory();
	
	private Map<String, Object[]> connectionMap = new FastMap<>();
	
	public static DataConnectionFactory getInstance()
	{
		return connectionFactory;
	}
	
	public void addConnectionFactory(String name, DBConectionFacory<?,?> factory){
		
		init(name);
		
		connectionMap.get(name)[0] = factory;
		
	}
	
	public void addConvertContainerFactory(String name, ConvertContainerFacory<?,?> factory){
		
		init(name);
		
		connectionMap.get(name)[1] = factory;
	}
	
	public void addDataConvertContainerFacory(String name, DataConvertContainerFacory factory){
		
		init(name);
		
		connectionMap.get(name)[2] = factory;
	}

	public void addExecuteContainerFacory(String name, ExecuteContainerFacory<?,?> factory){
		
		init(name);
		
		connectionMap.get(name)[3] = factory;
	}
	
	public DataConnection<?,?> getConnection() throws ConectionException {
		String conName = ConfigContextUtil.getDefaultCon();
		return getConnection(conName);
	}

	public DataConnection<?,?> getConnection(String conName) throws ConectionException {
		if (conName == null || conName.trim().isEmpty()) {
			throw new ConectionException("连接名称不能为空");
		}
		Connection connection = ConfigContextUtil.getConfigInfo().getConnection(conName);
		if (connection == null) {
			throw new ConectionException("连接不存在: " + conName);
		}
		ConnectionInfo connectionInfo = connection.getConnectionInfo();
		if (connectionInfo == null || connectionInfo.getName() == null) {
			throw new ConectionException("连接未绑定连接类型: " + conName);
		}
		if (connection.getDataSourceInfo() == null
				|| connection.getDataSourceInfo().getDataSource() == null) {
			throw new ConectionException("连接未绑定数据源: " + conName);
		}
		Object[] factories = connectionMap.get(connectionInfo.getName());
		if (factories == null) {
			throw new ConectionException("连接类型尚未注册工厂: " + connectionInfo.getName());
		}
		DataConnection<?,?> dataConnection = null;
		
		String dataSource = connection.getDataSourceInfo().getDataSource().getName();
		
		try {
			if (factories[0] == null) {
				throw new ConectionException("连接工厂未注册: " + connectionInfo.getName());
			}
			dataConnection = ((DBConectionFacory<?,?>) factories[0])
					.getDataConnection();
			
		} catch (Exception e) {
			if (e instanceof ConectionException) {
				throw (ConectionException) e;
			}
			throw new ConectionException("创建连接失败: " + conName, e);
		}
		if (dataConnection == null) {
			throw new ConectionException("连接工厂未返回连接对象: " + connectionInfo.getName());
		}
		
		dataConnection.setConName(conName);
		dataConnection.setDataSource(dataSource);
		
		try {
			if (factories[1] == null) {
				throw new ConectionException("参数转换工厂未注册: " + connectionInfo.getName());
			}
			ConvertContainer<?,?> convertContainer = ((ConvertContainerFacory<?,?>) factories[1])
					.getConvertContainer();
			if (convertContainer == null) {
				throw new ConectionException("参数转换工厂未返回转换器: " + connectionInfo.getName());
			}
					//(ConvertContainer) Class.forName(connectionInfo.getConvertClass()).newInstance();
			
			//convertContainer.init();
			
			dataConnection.setConvertContainer(convertContainer);
			
		} catch (Exception e) {
			if (e instanceof ConectionException) {
				throw (ConectionException) e;
			}
			throw new ConectionException("创建参数转换器失败: " + conName, e);
		}
		
		try {
			if (factories[2] == null) {
				throw new ConectionException("数据类型转换工厂未注册: " + connectionInfo.getName());
			}
			DataConvertContainer convertContainer = ((DataConvertContainerFacory) factories[2])
					.getDataConvertContainer();
			if (convertContainer == null) {
				throw new ConectionException("数据类型转换工厂未返回转换器: " + connectionInfo.getName());
			}
					//(DataConvertContainer) Class.forName(connectionInfo.getDataConvertClass()).newInstance();
			
			//convertContainer.init();
			
			connectionInfo.setDataConvertContainer(convertContainer);
			
		} catch (Exception e) {
			if (e instanceof ConectionException) {
				throw (ConectionException) e;
			}
			throw new ConectionException("创建数据类型转换器失败: " + conName, e);
		}
		
		try {
			if (factories[3] == null) {
				throw new ConectionException("SQL 执行工厂未注册: " + connectionInfo.getName());
			}
			ExecuteContainer<?,?> executeContainer = ((ExecuteContainerFacory<?,?>) factories[3])
					.getExecuteContainer();
			if (executeContainer == null) {
				throw new ConectionException("SQL 执行工厂未返回执行器: " + connectionInfo.getName());
			}
					//(ExecuteContainer) Class.forName(connectionInfo.getExecuteClass()).newInstance();
			
			//executeContainer.init();
			
			dataConnection.setExecuteContainer(executeContainer);
			
		} catch (Exception e) {
			if (e instanceof ConectionException) {
				throw (ConectionException) e;
			}
			throw new ConectionException("创建 SQL 执行器失败: " + conName, e);
		}
		
		//dataConnection.setConvertContainer((ConvertContainer) connectionInfo.getConvertContainer());
		//dataConnection.setExecuteContainer((ExecuteContainer) connectionInfo.getExecuteContainer());
	
		//convertContainer.init();
		//convertContainer.setDataSourceName(dataSource);
		//dataConnection.setConvertContainer(convertContainer);
		
		
		//executeContainer.init();
		
		//dataConnection.setExecuteContainer(executeContainer);
		return dataConnection;
	}
	
	private void init(String name){
		
		Object[] objArr =  connectionMap.get(name);
		
		if(objArr == null){
			objArr = new Object[4];
		}
		connectionMap.put(name, objArr);
	}
	
	//private void initConnection(String name){
		
	//}
}
