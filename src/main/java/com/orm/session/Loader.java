package com.orm.session;

public interface Loader<E> {

	public void load(String con,E e);
	
	public void load(E e);
	
}
