package com.orm.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.common.config.Config;
import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.model.service.ServiceInfo;
import com.orm.common.xml.model.service.TaskInfo;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ModelData;
import com.orm.model.execute.rule.exception.ExecuteRuleException;

public class ServiceContainer {
	
	private Log log = LogFactory.getLog(ServiceContainer.class);
	
	public ServiceResult execute(String name,String version,Map<String,Object> dataMap) throws DataNotDefineException, ExecuteRuleException{
		ServiceInfo serviceInfo = (ServiceInfo) DataUtil.getConfigInfo().get(Config.SERVICE, name+"_"+version);
		
		List<TaskInfo> taskList = serviceInfo.getTaskList();
		
		ServiceResult serviceResult = new ServiceResult();
		serviceResult.setName(name);
		serviceResult.setVersion(version);
		
		log.info("start service: "+name+" "+version);
		for(int i = 0;i < taskList.size();i++){

			TaskInfo taskInfo = taskList.get(i);
			
			String depend = taskInfo.getDepend();
			if( depend!= null && !"".equals(depend)){
				if(!serviceResult.isSuccess(taskInfo.getName())){
					log.info("task: "+taskInfo.getName()+" skip!");
					continue;
				}
			}
			log.info("task: "+taskInfo.getName()+" start!");
			
			ModelData mdata = DataUtil.createViewData(serviceInfo.getModel());
			mdata.getAllValues().putAll(dataMap);
			
			executeTask(taskInfo,mdata,serviceResult);
			
			if(serviceResult.isSuccess(taskInfo.getName())){
				log.info("task: "+taskInfo.getName()+" success!");
			}else{
				log.info("task: "+taskInfo.getName()+" error!");
			}
			
		}
		log.info("end service: "+name+" "+version);
		return serviceResult;
		
	}
	
	private void executeTask(TaskInfo taskInfo,ModelData mdata,ServiceResult serviceResult) throws ExecuteRuleException{
		TaskExecuter taskExecuter = new TaskExecuter();
		
		taskExecuter.setModelData(mdata);
		taskExecuter.setTaskInfo(taskInfo);
		taskExecuter.setServiceResult(serviceResult);
		taskExecuter.execute();
	}
}
