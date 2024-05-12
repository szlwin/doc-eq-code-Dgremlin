package dec.core.model.container.listener;

import dec.core.model.container.ResultInfo;

public interface ViewListener {

	ResultInfo notify(ViewEvent event);
}
