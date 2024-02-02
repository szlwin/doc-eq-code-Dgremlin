package dec.core.datasource.convert.container;

public interface ConvertContainer<E,V> {

	V convert(E e);
	/*
	public void addConvert(String name,DataConvert<E, V> cv);*/
	
	void init();
	
	//public void setDataSourceName(String name);
	
}
