package dec.core.context.config.model.config;

import dec.core.context.config.model.config.data.ConfigBaseData;

public interface Config<E extends ConfigBaseData> {

	int DATASOURCE = 0;
	
	int CONNECTION = 1;
	
	int DATA = 2;
	
	int RELATION = 3;
	
	int VIEWDATA = 4;
	
	int RULE = 5;
	
	int SERVICE = 6;
	
	int DATASOURCE_CONFIG = 7;
	
	int CONNECTION_CONFIG = 8;
	
	int SIZE = 9;
	
	@SuppressWarnings("hiding")
	<E extends ConfigBaseData> void add(E v);
	
	E get(String name);
}
