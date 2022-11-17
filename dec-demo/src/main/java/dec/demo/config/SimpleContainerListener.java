package dec.demo.config;

import dec.core.model.container.ResultInfo;
import dec.core.model.container.listener.ContainerEvent;
import dec.core.model.container.listener.ContainerListener;

public class SimpleContainerListener implements ContainerListener{

	@Override
	public ResultInfo notify(ContainerEvent event) {
		System.out.println("ContainerEvent:"+event.getType());
		return ResultInfo.success();
	}

}
