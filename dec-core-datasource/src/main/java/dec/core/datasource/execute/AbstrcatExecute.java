package dec.core.datasource.execute;

import dec.core.datasource.execute.dom.ExecuteInfo;

public abstract class AbstrcatExecute<E,R,C> implements Execute<E,ExecuteInfo,C>{

	protected String cmd;
	
	protected String keyType;
	
	protected Object keyValue;

	protected ExecuteInfo executeInfo = new ExecuteInfo();
	
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}

	public void setKeyValue(Object keyValue) {
		this.keyValue = keyValue;
	}

	public ExecuteInfo getResult() {
		return executeInfo;
	}
}
