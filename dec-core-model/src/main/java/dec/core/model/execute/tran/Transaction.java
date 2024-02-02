package dec.core.model.execute.tran;

public interface Transaction {

	int PROPAGATION_REQUIRED = 0;

	int PROPAGATION_REQUIRES_NEW = 1;
	
	int PROPAGATION_NOT_SUPPORTED = 2;

	int PROPAGATION_REQUIRED_NESTED = 3;
	
	int PROPAGATION_SUPPORTS = 4;
	
	//public static int PROPAGATION_NEVER = 5;
	
	//public static int PROPAGATION_MANDATORY = 6;
}
