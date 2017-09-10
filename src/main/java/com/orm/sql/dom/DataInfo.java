package com.orm.sql.dom;

/**
 * @author pc
 *
 */
public class DataInfo {

	private String column;
	
	private Object value;

	private String proterty;
	
	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getProterty() {
		return proterty;
	}

	public void setProterty(String proterty) {
		this.proterty = proterty;
	}
}
