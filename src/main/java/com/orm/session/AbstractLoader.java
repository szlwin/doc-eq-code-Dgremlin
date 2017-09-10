package com.orm.session;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLoader<E,V> implements Loader<E> {

	protected List<V> list = new ArrayList<V>();
	
	public abstract void load(String con, E e);

	public abstract void load(E e);
	
	protected List<V> get(){
		return list;
	}

}
