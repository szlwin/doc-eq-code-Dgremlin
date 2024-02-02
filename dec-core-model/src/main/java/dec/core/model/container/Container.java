package dec.core.model.container;

import dec.core.model.container.listener.ContainerListener;
import dec.core.model.execute.rule.exception.ExecuteRuleException;

public interface Container {

	Container load(ModelLoader modelLoader);
	
	Container execute() throws ExecuteRuleException;
	
	Container addListener(ContainerListener listener);
	
	ResultInfo getResult();
}
