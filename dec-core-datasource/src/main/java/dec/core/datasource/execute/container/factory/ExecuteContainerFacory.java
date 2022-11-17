package dec.core.datasource.execute.container.factory;

import dec.core.datasource.execute.container.ExecuteContainer;

public interface ExecuteContainerFacory<K,V> {

	public ExecuteContainer<K,V> getExecuteContainer();
}
