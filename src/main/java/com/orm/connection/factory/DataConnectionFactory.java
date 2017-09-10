package com.orm.connection.factory;

import com.orm.common.xml.model.config.ConnectionInfo;
import com.orm.common.xml.model.config.connection.Connection;
import com.orm.connection.DataConnection;
import com.orm.connection.exception.ConectionException;
import com.orm.context.data.DataUtil;
import com.orm.sql.util.Util;

public class DataConnectionFactory {

	private static final DataConnectionFactory connectionFactory 
		= new DataConnectionFactory();
	
	public static DataConnectionFactory getInstance()
	{
		return connectionFactory;
	}
	
	public DataConnection getConnection() throws ConectionException {
		String conName = Util.getDefaultCon();
		return getConnection(conName);
	}

	public DataConnection getConnection(String conName) throws ConectionException {
		
		Connection connection = DataUtil.getConfigInfo().getConnection(conName);
		
		ConnectionInfo connectionInfo = connection.getConnectionInfo();
		DataConnection dataConnection = null;
		
		String dataSource = connection.getDataSourceInfo().getDataSource().getName();
		
		try {
			dataConnection = (DataConnection) connectionInfo.getConClass().newInstance();
		} catch (Exception e) {
			throw new ConectionException(e);
		}
		
		dataConnection.setConName(conName);
		dataConnection.setDataSource(dataSource);
		
		dataConnection.setConvertContainer(connectionInfo.getConvertContainer());
		dataConnection.setExecuteContainer(connectionInfo.getExecuteContainer());
	
		//convertContainer.init();
		//convertContainer.setDataSourceName(dataSource);
		//dataConnection.setConvertContainer(convertContainer);
		
		
		//executeContainer.init();
		
		//dataConnection.setExecuteContainer(executeContainer);
		return dataConnection;
	}
}
