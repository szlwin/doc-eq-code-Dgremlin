package dec.core.datasource.datatype.convert;

public interface DataConvert<E,R> {

	public R convert(E data);
}
