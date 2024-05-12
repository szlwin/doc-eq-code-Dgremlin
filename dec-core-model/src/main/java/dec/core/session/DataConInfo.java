package dec.core.session;

import dec.core.context.data.BaseData;

public class DataConInfo {

	private BaseData data;
	
	private String connectionName;

	public BaseData getData() {
		return data;
	}

	public void setData(BaseData data) {
		this.data = data;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}
	
	
}
