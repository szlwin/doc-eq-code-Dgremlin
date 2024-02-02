package dec.core.datasource.datatype.convert;

public interface DataConvertContainer {
	
	void init();
	
	Object convert(Object data,String fun,boolean isTo);
	
	Object convert(Object data,String fun);
	
	boolean check(String origType,String targetType);
}
