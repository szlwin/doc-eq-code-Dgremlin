package dec.core.model.container.listener;

import dec.core.model.container.ResultInfo;

public interface ContainerListener {

	ResultInfo notify(ContainerEvent event);
}
