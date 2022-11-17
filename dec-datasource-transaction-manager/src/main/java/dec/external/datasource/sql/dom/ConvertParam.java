package dec.external.datasource.sql.dom;

import java.util.Map;

public class ConvertParam {

	private String sql;
	
	private Object data;

	private String type;
	
	private String dataSource;
	
	private boolean isOrign;
	
	private Map<String,String> conParamMap;
	
	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean getIsOrign() {
		return isOrign;
	}

	public void setOrign(boolean isOrign) {
		this.isOrign = isOrign;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public Map<String, String> getConParamMap() {
		return conParamMap;
	}

	public void setConParamMap(Map<String, String> conParamMap) {
		this.conParamMap = conParamMap;
	}
	
	
}
