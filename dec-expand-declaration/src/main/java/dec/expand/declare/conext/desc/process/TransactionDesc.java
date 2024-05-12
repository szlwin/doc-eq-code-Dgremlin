package dec.expand.declare.conext.desc.process;

public class TransactionDesc {

    private String transactionGroup;

    private RollBackPolicy rollBackPolicy;

    private TransactionPolicy transaction;

    private int index;

    private int begin;

    private int end;

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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
