package dec.core.datasource.convert.container.factory;

import dec.core.datasource.convert.container.ConvertContainer;

public interface ConvertContainerFacory<K,V> {

	ConvertContainer<K,V> getConvertContainer();
}
