package com.orm.model.container;

import com.orm.common.xml.util.Constanst;

public class ContainerFactory {

	public static Container getContainer(String type){
		
		if(type.equals(Constanst.CONTAINER_TYPE_COMMIT))
			return new ModelContainer();
		
		if(type.equals(Constanst.CONTAINER_TYPE_SYN))
			return new SynContainer();
		return null;
	}
}
