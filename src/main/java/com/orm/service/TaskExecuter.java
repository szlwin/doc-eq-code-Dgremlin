package com.orm.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.common.xml.model.service.TaskData;
import com.orm.common.xml.model.service.TaskInfo;
import com.orm.context.data.ModelData;
import com.orm.model.container.Container;
import com.orm.model.container.ContainerFactory;
import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;
import com.orm.model.execute.rule.exception.ExecuteRuleException;

public class TaskExecuter {
	
	private Log log = LogFactory.getLog(TaskExecuter.class);
	
	private TaskInfo taskInfo;
	
	private ModelData modelData;
	
	private ServiceResult serviceResult;
	public void execute() throws ExecuteRuleException{
		
		Container container = ContainerFactory.getContainer(taskInfo.getType());
		
		List<TaskData> taskDatalist = taskInfo.getTaskList();
		for(int i = 0;i < taskDatalist.size();i++){
			TaskData taskData = taskDatalist.get(i);
			ModelLoader modelLoader = new ModelLoader();
			
			modelLoader.load(taskData.getExecute(), modelData, taskData.getCon());
			
			container.load(modelLoader);
		}
		
		try{
			container.execute();
		}catch(ExecuteRuleException e){
			serviceResult.addError(e,taskInfo.getName());
			log.error(e);
		}

		ResultInfo resultInfo = container.getResult();
		if(!resultInfo.isSuccess()){
			ExecuteRuleException executeRuleException = new ExecuteRuleException(resultInfo.getErrorMsg(),resultInfo.getErrorName(),"");
			serviceResult.addError(executeRuleException,taskInfo.getName());
		}

	}

	public void setTaskInfo(TaskInfo taskInfo) {
		this.taskInfo = taskInfo;
	}

	public void setModelData(ModelData modelData) {
		this.modelData = modelData;
	}

	public ServiceResult getServiceResult() {
		return serviceResult;
	}

	public void setServiceResult(ServiceResult serviceResult) {
		this.serviceResult = serviceResult;
	}
	
	
}
