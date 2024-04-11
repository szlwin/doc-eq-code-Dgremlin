package dec.core.datasource.datatype.convert;

public interface DataConvertContainer {
	
	void init();

	Object convertToDataSource(Object data,String type, String originType);

	Object convertFromDataSource(Object data,String type, String originType);

	//Object convert(Object data,String fun,boolean isTo);
	
	//Object convert(Object data,String fun);
}
