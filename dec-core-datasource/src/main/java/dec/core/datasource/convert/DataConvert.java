package dec.core.datasource.convert;

public interface DataConvert<E,R> {
	public R convert(E e);
	
	public void setDataSource(String dataSource);
	
	public String getDataSource();
}
