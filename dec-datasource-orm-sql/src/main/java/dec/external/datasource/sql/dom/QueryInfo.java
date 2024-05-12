package dec.external.datasource.sql.dom;

import java.util.Collection;
import java.util.Map;

import dec.core.datasource.dom.DataInfo;

public class QueryInfo {

	private String sql;
	
	private Map<String,Object> paramMap;

	private String baseDataName;
	
	private Object con;
	
	private Collection<DataInfo> dataInfoCollection;
	
	private String type;
	
	private String keyType;
	
	private Object keyValue;
	
	private Map<String,String> conParamMap;
	
	public QueryInfo(){
		
	}
	
	public QueryInfo(String sql,Map<String,Object> paramMap,String dataName){
		this.sql = sql;
		this.paramMap = paramMap;
		this.baseDataName = dataName;
	}
	
	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Map<String, Object> getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map<String, Object> paramMap) {
		this.paramMap = paramMap;
	}

	public String getBaseDataName() {
		return baseDataName;
	}

	public void setBaseDataName(String baseDataName) {
		this.baseDataName = baseDataName;
	}
	public Object getCon() {
		return con;
	}
	public void setCon(Object con) {
		this.con = con;
	}
	public Collection<DataInfo> getDataInfoCollection() {
		return dataInfoCollection;
	}
	public void setDataInfoCollection(Collection<DataInfo> dataInfoCollection) {
		this.dataInfoCollection = dataInfoCollection;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getKeyType() {
		return keyType;
	}
	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}
	public Object getKeyValue() {
		return keyValue;
	}
	public void setKeyValue(Object keyValue) {
		this.keyValue = keyValue;
	}

	public Map<String, String> getConParamMap() {
		return conParamMap;
	}

	public void setConParamMap(Map<String, String> conParamMap) {
		this.conParamMap = conParamMap;
	}
	
}
