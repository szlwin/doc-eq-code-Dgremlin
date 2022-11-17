package dec.core.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.core.context.config.exception.DataNotDefineException;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.service.ServiceInfo;
import dec.core.context.config.model.service.TaskInfo;
import dec.core.context.data.ModelData;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import dec.core.model.utils.DataUtil;

/*import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dec.core.common.config.Config;
import dec.core.common.xml.exception.DataNotDefineException;
import dec.core.common.xml.model.service.ServiceInfo;
import dec.core.common.xml.model.service.TaskInfo;
import dec.core.context.data.DataUtil;
import dec.core.context.data.ModelData;
import dec.core.model.execute.rule.exception.ExecuteRuleException;*/

public class ServiceContainer {
	
	private final static Logger log = LoggerFactory.getLogger(ServiceContainer.class);
	
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
