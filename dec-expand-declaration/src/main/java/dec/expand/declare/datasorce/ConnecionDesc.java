package dec.expand.declare.datasorce;

import dec.expand.declare.conext.desc.process.RollBackPolicy;
import dec.expand.declare.conext.desc.process.TransactionPolicy;

public class ConnecionDesc {

    private String group;

    private TransactionPolicy transactionPolicy;


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public TransactionPolicy getTransactionPolicy() {
        return transactionPolicy;
    }

    public void setTransactionPolicy(TransactionPolicy transactionPolicy) {
        this.transactionPolicy = transactionPolicy;
    }
}
