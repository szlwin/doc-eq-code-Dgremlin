package dec.core.context.config.model.config;

import dec.core.context.config.model.config.data.ConfigBaseData;

public interface Config<E extends ConfigBaseData> {

	public int DATASOURCE = 0;
	
	public int CONNECTION = 1;
	
	public int DATA = 2;
	
	public int RELATION = 3;
	
	public int VIEWDATA = 4;
	
	public int RULE = 5;
	
	public int SERVICE = 6;
	
	public int DATASOURCE_CONFIG = 7;
	
	public int CONNECTION_CONFIG = 8;
	
	public int SIZE = 9;
	
	@SuppressWarnings("hiding")
	public <E extends ConfigBaseData> void add(E v);
	
	public E get(String name);
}
