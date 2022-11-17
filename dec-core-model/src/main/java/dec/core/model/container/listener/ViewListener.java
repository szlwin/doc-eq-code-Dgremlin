package dec.core.model.container.listener;

import dec.core.model.container.ResultInfo;

public interface ViewListener {

	public ResultInfo notify(ViewEvent event);
}
