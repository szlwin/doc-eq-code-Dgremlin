package dec.expand.declare.utils;

import dec.expand.declare.conext.DescContext;
import dec.expand.declare.conext.desc.business.BusinessDesc;
import dec.expand.declare.conext.desc.system.SystemDesc;
import dec.expand.declare.system.SystemContext;
import dec.expand.declare.system.System;

public class ContextUtils {
	
	
	public static void load(BusinessDesc businessDesc){
		DescContext.get().addBusiness(businessDesc);
	}
	
	public static void load(SystemDesc systemDesc){
		DescContext.get().addSystem(systemDesc);
	}
	
	
	public static void load(System system){
		SystemContext.get().add(system);
	}
}
