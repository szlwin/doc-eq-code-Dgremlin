package com.orm.common.xml.model.service;

public class TaskData {

	public static final String EXECUTE = "execute";
	
	public static final String CON = "con";
	
	public static final String PATTERN = "pattern";
	
	private String execute;
	
	private String con;
	
	private String pattern;

	public String getExecute() {
		return execute;
	}

	public void setExecute(String execute) {
		this.execute = execute;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getCon() {
		return con;
	}

	public void setCon(String con) {
		this.con = con;
	}
	
	
}
