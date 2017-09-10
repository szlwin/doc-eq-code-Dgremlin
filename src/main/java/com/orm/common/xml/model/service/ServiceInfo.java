package com.orm.common.xml.model.service;

import java.util.List;

import com.orm.common.xml.model.config.ConfigBaseData;

public class ServiceInfo extends ConfigBaseData{

	public final static String NODE_NAME = "service";

	public static final String TSAKINFO = "task-info";
	
	public static final String NAME = "name";
	
	public static final String VERSION = "version";
	
	public static final String MODEL = "model";
	
	private String name;
	
	private String version;
	
	private String model;

	private List<TaskInfo> taskList;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public List<TaskInfo> getTaskList() {
		return taskList;
	}

	public void setTaskList(List<TaskInfo> taskList) {
		this.taskList = taskList;
	}
	
	
}
