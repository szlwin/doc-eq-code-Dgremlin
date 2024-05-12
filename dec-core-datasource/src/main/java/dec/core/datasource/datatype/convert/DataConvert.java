package dec.core.datasource.datatype.convert;

public interface DataConvert<E,R> {

	R convert(E data);
}
