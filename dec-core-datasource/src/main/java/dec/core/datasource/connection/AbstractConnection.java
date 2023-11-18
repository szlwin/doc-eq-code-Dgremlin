package dec.core.datasource.connection;

import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.convert.container.ConvertContainer;
import dec.core.datasource.execute.container.ExecuteContainer;


public abstract class AbstractConnection<E,V> implements DataConnection<E,V>{

	protected String conName;
	
	protected String dataSource;
	
	protected Object con;
	
	protected ConvertContainer sqlConvertContainer;

	protected ExecuteContainer sqlExecuteContainer;
	

	public void setConvertContainer(ConvertContainer convertContainer) {
		this.sqlConvertContainer =  convertContainer;
		
	}
	
	public void setExecuteContainer(ExecuteContainer sqlExecuteContainer) {
		this.sqlExecuteContainer =  sqlExecuteContainer;
		
	}
	
	public String getConName() {
		return conName;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setConName(String name) {
		this.conName = name;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
}