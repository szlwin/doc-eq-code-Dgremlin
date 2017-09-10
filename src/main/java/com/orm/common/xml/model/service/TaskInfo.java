package com.orm.common.xml.model.service;

import java.util.List;

import com.orm.common.xml.model.config.ConfigBaseData;

public class TaskInfo extends ConfigBaseData{

	public static final String NAME = "name";
	
	public static final String TYPE = "type";
	
	public static final String DEPEND = "depend";
	
	public static final String NODE_TASK = "task";
	
	private String name;
	
	private String type;
	
	private String depend;

	private List<TaskData> taskList;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDepend() {
		return depend;
	}

	public void setDepend(String depend) {
		this.depend = depend;
	}

	public List<TaskData> getTaskList() {
		return taskList;
	}

	public void setTaskList(List<TaskData> taskList) {
		this.taskList = taskList;
	}
	
	
}
