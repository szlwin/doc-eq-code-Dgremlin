package com.orm.model.container;

import com.orm.sql.util.Util;

public class ModelLoader {

	private String ruleName;
	
	private Object e;
	
	private String conName;

	public void load(String name,Object e){
		load(name,e,Util.getDefaultCon());
	}

	public String getRuleName() {
		return ruleName;
	}

	public Object get() {
		return e;
	}
	
	public void load(String name,Object e,String conName){
		this.ruleName = name;
		this.e = e;
		this.conName = conName;
	}
	
	public String getConName() {
		return conName;
	}
}
