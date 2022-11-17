package dec.core.model.container;

import dec.core.context.config.model.config.ConfigConstanst;

//import dec.core.common.xml.util.Constanst;

public class ContainerFactory {

	public static Container getContainer(String type){
		
		if(type.equals(ConfigConstanst.CONTAINER_TYPE_COMMIT))
			return new ModelContainer();
		
		if(type.equals(ConfigConstanst.CONTAINER_TYPE_SYN))
			return new SynContainer();
		return null;
	}
}
