package dec.core.datasource.datatype.convert;

public interface DataConvertContainer {
	
	public void init();
	
	public Object convert(Object data,String fun,boolean isTo);
	
	public Object convert(Object data,String fun);
	
	public boolean check(String origType,String targetType);
}
