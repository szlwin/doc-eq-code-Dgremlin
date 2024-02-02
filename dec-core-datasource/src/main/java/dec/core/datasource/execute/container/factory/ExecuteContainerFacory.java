package dec.core.datasource.execute.container.factory;

import dec.core.datasource.execute.container.ExecuteContainer;

public interface ExecuteContainerFacory<K,V> {

	ExecuteContainer<K,V> getExecuteContainer();
}
