package dec.core.datasource.convert;

public interface DataConvert<E,R> {
	R convert(E e);
	
	void setDataSource(String dataSource);
	
	String getDataSource();
}
