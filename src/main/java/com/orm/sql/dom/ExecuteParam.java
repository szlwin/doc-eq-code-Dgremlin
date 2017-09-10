package com.orm.sql.dom;

public class ExecuteParam {

	private String cmd;
	
	private Object value;
	
	private String type;

	private boolean isOrign;
	
	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
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
	
}
