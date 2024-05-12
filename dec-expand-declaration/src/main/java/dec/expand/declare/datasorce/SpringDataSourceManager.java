package dec.expand.declare.datasorce;

import dec.expand.declare.conext.desc.process.TransactionPolicy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.List;

public class SpringDataSourceManager implements DataSourceManager {

    private DataSourceTransactionManager dstManager;

    private int index = 0;
    private List<TransactionStatus> transactionStatusList = new ArrayList<>();

    public void connect(ConnecionDesc connecionDesc) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(convert(connecionDesc.getTransactionPolicy()));
        TransactionStatus transaction = dstManager.getTransaction(def);
        transactionStatusList.add(transaction);
        index = transactionStatusList.size() - 1;
    }

    public void commit() {
        dstManager.commit(transactionStatusList.get(index));
        index = index - 1;
    }


    public void close() {

    }

    public void rollBack() {
        dstManager.rollback(transactionStatusList.get(index));
    }

    public void setDstManager(DataSourceTransactionManager dstManager) {
        this.dstManager = dstManager;
    }

    private int convert(TransactionPolicy transactionPolicy) {
        switch (transactionPolicy){
            case NESTED:
                return TransactionDefinition.PROPAGATION_NESTED;
            case NEW:
                return TransactionDefinition.PROPAGATION_REQUIRES_NEW;
            case NOSUPPORTED:
                return TransactionDefinition.PROPAGATION_NOT_SUPPORTED;
            case REQUIRE:
                return TransactionDefinition.PROPAGATION_REQUIRED;
            case SUPPORTED:
                return TransactionDefinition.PROPAGATION_SUPPORTS;
        }
        return 0;
    }

    public DataSourceTransactionManager getDstManager() {
        return dstManager;
    }

}
