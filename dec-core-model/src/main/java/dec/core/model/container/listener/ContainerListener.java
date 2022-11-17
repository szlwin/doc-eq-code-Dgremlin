package dec.core.model.container.listener;

import dec.core.model.container.ResultInfo;

public interface ContainerListener {

	public ResultInfo notify(ContainerEvent event);
}
