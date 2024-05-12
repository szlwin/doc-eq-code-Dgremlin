package dec.expand.declare.conext;

import java.util.HashMap;
import java.util.Map;

import dec.expand.declare.conext.desc.business.BusinessDesc;

public class BusinessContext {

	 private Map<String, BusinessDesc> businessMap;
	 
	 public void add(BusinessDesc businessDesc){
		 
		 if(businessMap == null){
			 businessMap = new HashMap<>();
		 }
		 
		 //businessDesc.init();
		 
		 businessMap.put(businessDesc.getName(), businessDesc);
	 }
	 
	 
	 public BusinessDesc get(String name){
		
		return businessMap.get(name);
	 }
}
