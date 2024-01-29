package dec.expand.declare.conext.desc.process;

public class ProcessDesc {

    private String data;


    private String transactionGroup;

    private RollBackPolicy rollBackPolicy;

    private TransactionPolicy transaction;

    private String system;

    private boolean begin = false;

    private boolean end = false;

    private boolean onlyEnd = false;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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

    public boolean isBegin() {
        return begin;
    }

    public void setBegin(boolean begin) {
        this.begin = begin;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

	public boolean isOnlyEnd() {
		return onlyEnd;
	}

	public void setOnlyEnd(boolean onlyEnd) {
		this.onlyEnd = onlyEnd;
	}
}
