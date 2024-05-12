package dec.core.model.container.listener;

import java.util.List;

import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;

public class ContainerEvent {

	private List<ModelLoader> loaderList;

	private ContainerEventEnum type;
	
	private ResultInfo resultInfo;
	
	public List<ModelLoader> getLoaderList() {
		return loaderList;
	}

	public void setLoaderList(List<ModelLoader> loaderList) {
		this.loaderList = loaderList;
	}

	public ContainerEventEnum getType() {
		return type;
	}

	public void setType(ContainerEventEnum type) {
		this.type = type;
	}

	public ResultInfo getResultInfo() {
		return resultInfo;
	}

	public void setResultInfo(ResultInfo resultInfo) {
		this.resultInfo = resultInfo;
	}

	
}
