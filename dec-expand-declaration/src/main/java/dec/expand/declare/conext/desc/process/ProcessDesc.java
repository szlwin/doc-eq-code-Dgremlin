package dec.expand.declare.conext.desc.process;

public class ProcessDesc {

	private String data;
	
	private boolean onErrorStop;
	
	private boolean onExceptionStop;
	
	private String transactionGroup;
	
	private RollBackPolicy rollBackPolicy;
	
	private TransactionPolicy transaction;

	private String system;
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public boolean isOnErrorStop() {
		return onErrorStop;
	}

	public void setOnErrorStop(boolean onErrorStop) {
		this.onErrorStop = onErrorStop;
	}

	public boolean isOExceptionStop() {
		return onExceptionStop;
	}

	public void setOnExceptionStop(boolean oExceptionStop) {
		this.onExceptionStop = oExceptionStop;
	}

	public String getTransactionGroup() {
		return transactionGroup;
	}

	public void setTransactionGroup(String transactionGroup) {
		this.transactionGroup = transactionGroup;
	}

	public RollBackPolicy getRollBackPolicy() {
		return rollBackPolicy;
	}

	public void setRollBackPolicy(RollBackPolicy rollBackPolicy) {
		this.rollBackPolicy = rollBackPolicy;
	}

	public TransactionPolicy getTransaction() {
		return transaction;
	}

	public void setTransaction(TransactionPolicy transaction) {
		this.transaction = transaction;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}
	
}
