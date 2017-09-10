package com.orm.api.dom;

/**
 * @author pc
 *
 */
public class ConditionInfo {

	private String column;
	
	private Object value;

	private String flag;
	
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

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
	
	
}
