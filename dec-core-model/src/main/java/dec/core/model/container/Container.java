package dec.core.model.container;

import dec.core.model.container.listener.ContainerListener;
import dec.core.model.execute.rule.exception.ExecuteRuleException;

public interface Container {

	public Container load(ModelLoader modelLoader);
	
	public Container execute() throws ExecuteRuleException;
	
	public Container addListener(ContainerListener listener);
	
	public ResultInfo getResult();
}
