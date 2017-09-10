package com.orm.sql.dom;

import java.util.List;

public class SQLDataInfo {

	private String sql;
	
	private List<DataInfo> dataInfoList;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<DataInfo> getDataInfoList() {
		return dataInfoList;
	}

	public void setDataInfoList(List<DataInfo> dataInfoList) {
		this.dataInfoList = dataInfoList;
	}
	
}
