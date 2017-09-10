package com.orm.common.xml.model.rule;

public class RuleCheckData extends RuleDefineInfo{

	public static String SQL = "sql";
	
	public static String PATTERN = "pattern";
	
	private String sql;
	
	private String pattern;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	
	
}
