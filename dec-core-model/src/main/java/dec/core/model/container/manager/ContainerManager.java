package dec.core.model.container.manager;

import dec.core.model.container.ModelContainer;

public class ContainerManager {

	private static final ThreadLocal<ModelContainer> modelContainerLoacl = new InheritableThreadLocal<ModelContainer>();
	
	public static ModelContainer getCurrentModelContainer(){
		ModelContainer modelContainer = modelContainerLoacl.get();
		
		if(modelContainer == null){
			//modelContainer = new ModelContainer();
			modelContainerLoacl.set(new ModelContainer());
		}
		return modelContainerLoacl.get();
	}
	
}
