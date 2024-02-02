package dec.core.session;

public interface Loader<E> {

	void load(String con,E e);
	
	void load(E e);
	
}
