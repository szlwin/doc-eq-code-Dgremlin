package dec.core.datasource.convert.container;

public interface ConvertContainer<E,V> {

	public V convert(E e);
	/*
	public void addConvert(String name,DataConvert<E, V> cv);*/
	
	public void init();
	
	//public void setDataSourceName(String name);
	
}
