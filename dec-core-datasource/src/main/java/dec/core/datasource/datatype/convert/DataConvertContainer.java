package dec.core.datasource.datatype.convert;

public interface DataConvertContainer {

	Object convertToDataSource(Object data,String type, String originType);

	Object convertFromDataSource(Object data,String type, String originType);

}
