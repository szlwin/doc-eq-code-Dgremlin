package com.orm.common.xml.model.rule;

public class RuleExecuteInfo extends RuleDefineInfo{
	
	public static String SQL = "sql";
	
	private String sql;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
}
