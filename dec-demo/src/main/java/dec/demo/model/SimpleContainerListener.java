package dec.demo.model;

import dec.core.model.container.ResultInfo;
import dec.core.model.container.listener.ContainerEvent;
import dec.core.model.container.listener.ContainerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleContainerListener implements ContainerListener{

	private final static Logger log = LoggerFactory.getLogger(SimpleContainerListener.class);
	
	@Override
	public ResultInfo notify(ContainerEvent event) {
		log.info("ContainerEvent:"+event.getType());
		return ResultInfo.success();
	}

}
