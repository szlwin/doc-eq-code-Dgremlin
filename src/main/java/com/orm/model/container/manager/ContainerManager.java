package com.orm.model.container.manager;

import com.orm.model.container.ModelContainer;

public class ContainerManager {

	private static final ThreadLocal<ModelContainer> modelContainerLoacl = new ThreadLocal<ModelContainer>();
	
	public static synchronized ModelContainer getCurrentModelContainer(){
		ModelContainer modelContainer = modelContainerLoacl.get();
		
		if(modelContainer == null){
			//modelContainer = new ModelContainer();
			modelContainerLoacl.set(new ModelContainer());
		}
		return modelContainerLoacl.get();
	}
}
