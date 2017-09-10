package com.orm.connection.exception;

public class ConectionException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ConectionException(String s){
		super(s);
	}

	public ConectionException(Exception e){
		super(e);
	}
}
