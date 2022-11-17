package dec.context.parse.xml.parse.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.xml.parse.AbstarctElementsParser;
import dec.core.context.config.model.service.ServiceInfo;
import dec.core.context.config.model.service.TaskData;
import dec.core.context.config.model.service.TaskInfo;

//import com.orm.common.xml.exception.XMLParseException;
//import com.orm.common.xml.model.service.ServiceInfo;
//import com.orm.common.xml.model.service.TaskData;
//import com.orm.common.xml.model.service.TaskInfo;
//import com.orm.common.xml.parse.AbstarctElementsParser;

public class ServiceParser extends AbstarctElementsParser{

	private final static Logger log = LoggerFactory.getLogger(ServiceParser.class);
	
	public ServiceInfo parse(Element element) throws XMLParseException {
		ServiceInfo serviceInfo = new ServiceInfo();
		Iterator<Element> list = element.elementIterator(ServiceInfo.TSAKINFO);
		
		String name = element.attributeValue(ServiceInfo.NAME);
		String version = element.attributeValue(ServiceInfo.VERSION);
		
		log.info("Load the service:"+name+" "+version+" start!");
		
		serviceInfo.setName(name);
		serviceInfo.setVersion(version);
		serviceInfo.setModel(element.attributeValue(ServiceInfo.MODEL));

		List<TaskInfo> taskInfoList = new ArrayList<TaskInfo>();
		
		while(list.hasNext()){
			Element taskInfoElement = (Element)list.next();
			TaskInfo taskInfo = parserTaskInfo(taskInfoElement);
			taskInfoList.add(taskInfo);
		}
		serviceInfo.setTaskList(taskInfoList);
		log.info("Load the service:"+serviceInfo.getName()+" success!");
		return serviceInfo;
	}

	private TaskInfo parserTaskInfo(Element element){
		TaskInfo taskInfo = new TaskInfo();
		
		taskInfo.setName(element.attributeValue(TaskInfo.NAME));
		taskInfo.setType(element.attributeValue(TaskInfo.TYPE));
		taskInfo.setDepend(element.attributeValue(TaskInfo.DEPEND));
		
		Iterator<Element> list = element.elementIterator(TaskInfo.NODE_TASK);
		List<TaskData> taskList = new ArrayList<TaskData>();
		
		while(list.hasNext()){
			Element taskElement = (Element)list.next();
			TaskData taskData = parserTask(taskElement);
			taskList.add(taskData);
		}
		taskInfo.setTaskList(taskList);
		return taskInfo;
		
	}
	private TaskData parserTask(Element element){
		TaskData taskData = new TaskData();
		
		taskData.setCon(element.attributeValue(TaskData.CON));
		taskData.setExecute(element.attributeValue(TaskData.EXECUTE));
		taskData.setPattern(element.attributeValue(TaskData.PATTERN));
		return taskData;
	}

}
