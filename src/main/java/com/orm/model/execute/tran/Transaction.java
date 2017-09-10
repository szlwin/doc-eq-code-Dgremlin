package com.orm.model.execute.tran;

public interface Transaction {

	public int PROPAGATION_REQUIRED = 0;

	public int PROPAGATION_REQUIRES_NEW = 1;
	
	public int PROPAGATION_NOT_SUPPORTED = 2;

	public int PROPAGATION_REQUIRED_NESTED = 3;
	
	public int PROPAGATION_SUPPORTS = 4;
	
	//public static int PROPAGATION_NEVER = 5;
	
	//public static int PROPAGATION_MANDATORY = 6;
}
