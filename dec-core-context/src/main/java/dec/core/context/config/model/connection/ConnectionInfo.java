package dec.core.context.config.model.connection;

import dec.core.context.config.model.config.data.ConfigBaseData;

public class ConnectionInfo<C,E,D> extends ConfigBaseData{

	public static final String NAME = "name";
	
	public static final String TYPE = "type";
	
	public static final String CONNECTION = "connection";
	
	public static final String CONVERT = "convert";
	
	public static final String EXECUTER = "executer";
	
	public static final String DATA_CONVERT = "data-convert";
	
	private String name;
	
	private String type;
	
	//private String connectionClass;
	
	//private String convertClass;
	
	//private String executeClass;

	//private String dataConvertClass;
	
	//private ConvertContainer convertContainer;
	
	//private ExecuteContainer executeContainer;
	
	//private com.orm.sql.datatype.convert.ConvertContainer dataConvertContainer;
	
	//private C convertContainer;
	
	//private E executeContainer;
	
	private D dataConvertContainer;
	
	//private Class conClass;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/*public ConvertContainer getConvertContainer() {
		return convertContainer;
	}

	public void setConvertContainer(ConvertContainer convertContainer) {
		this.convertContainer = convertContainer;
	}

	public ExecuteContainer getExecuteContainer() {
		return executeContainer;
	}

	public void setExecuteContainer(ExecuteContainer executeContainer) {
		this.executeContainer = executeContainer;
	}*/

	public D getDataConvertContainer() {
		return dataConvertContainer;
	}

	public void setDataConvertContainer(D dataConvertContainer) {
		this.dataConvertContainer = dataConvertContainer;
	}

	/*public com.orm.sql.datatype.convert.ConvertContainer getDataConvertContainer() {
		return dataConvertContainer;
	}

	public void setDataConvertContainer(com.orm.sql.datatype.convert.ConvertContainer dataConvertContainer) {
		this.dataConvertContainer = dataConvertContainer;
	}*/
	
	
	
}
