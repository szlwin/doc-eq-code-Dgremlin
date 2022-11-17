package dec.core.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.core.context.config.model.service.TaskData;
import dec.core.context.config.model.service.TaskInfo;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

//import dec.core.common.xml.model.service.TaskData;
//import dec.core.common.xml.model.service.TaskInfo;
import dec.core.context.data.ModelData;
import dec.core.model.container.Container;
import dec.core.model.container.ContainerFactory;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;
import dec.core.model.execute.rule.exception.ExecuteRuleException;

public class TaskExecuter {
	
	private final static Logger log = LoggerFactory.getLogger(TaskExecuter.class);
	
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
			log.error("Execute rule error",e);
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
