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
		
		Connection connection = ConfigContextUtil.getConfigInfo().getConnection(conName);
		
		ConnectionInfo connectionInfo = connection.getConnectionInfo();
		DataConnection<?,?> dataConnection = null;
		
		String dataSource = connection.getDataSourceInfo().getDataSource().getName();
		
		try {
			dataConnection = ((DBConectionFacory<?,?>) connectionMap.get(connectionInfo.getName())[0])
					.getDataConnection();
			
		} catch (Exception e) {
			throw new ConectionException(e);
		}
		
		dataConnection.setConName(conName);
		dataConnection.setDataSource(dataSource);
		
		try {
			ConvertContainer<?,?> convertContainer = ((ConvertContainerFacory<?,?>) connectionMap.get(connectionInfo.getName())[1])
					.getConvertContainer();
					//(ConvertContainer) Class.forName(connectionInfo.getConvertClass()).newInstance();
			
			//convertContainer.init();
			
			dataConnection.setConvertContainer(convertContainer);
			
		} catch (Exception e) {
			throw new ConectionException(e);
		}
		
		try {
			DataConvertContainer convertContainer = ((DataConvertContainerFacory) connectionMap.get(connectionInfo.getName())[2])
					.getDataConvertContainer();
					//(DataConvertContainer) Class.forName(connectionInfo.getDataConvertClass()).newInstance();
			
			//convertContainer.init();
			
			connectionInfo.setDataConvertContainer(convertContainer);
			
		} catch (Exception e) {
			throw new ConectionException(e);
		}
		
		try {
			ExecuteContainer<?,?> executeContainer = ((ExecuteContainerFacory<?,?>) connectionMap.get(connectionInfo.getName())[3])
					.getExecuteContainer();
					//(ExecuteContainer) Class.forName(connectionInfo.getExecuteClass()).newInstance();
			
			//executeContainer.init();
			
			dataConnection.setExecuteContainer(executeContainer);
			
		} catch (Exception e) {
			throw new ConectionException(e);
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
