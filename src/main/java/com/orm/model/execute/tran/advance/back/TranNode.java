package com.orm.model.execute.tran.advance.back;

import java.io.Serializable;
import java.util.List;

import com.orm.model.container.ModelLoader;

public class TranNode implements Cloneable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	
	private List<ModelLoader> modelList;

	private long id;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ModelLoader> getModelList() {
		return modelList;
	}

	public void setModelList(List<ModelLoader> modelList) {
		this.modelList = modelList;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
}
