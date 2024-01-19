package dec.expand.declare.business;

import dec.expand.declare.conext.DataStorage;
import dec.expand.declare.conext.desc.process.TransactionPolicy;
import dec.expand.declare.datasorce.DataSourceManager;
import dec.expand.declare.executer.produce.Produce;
import dec.expand.declare.fun.FinalFun;
import dec.expand.declare.fun.Function;
import dec.expand.declare.service.ExecuteResult;

import java.util.List;

public interface BusinessDeclare {

    String getName();

    /**
     * 添加处理数据
     *
     * @param t
     * @param
     * @return
     */
    <T> BusinessDeclare addEntitys(String type, List<T> t);

    <T> BusinessDeclare addEntity(String type, T t);

    BusinessDeclare beginTx();

    BusinessDeclare beginTx(TransactionPolicy transactionPolicy);

    BusinessDeclare rollback();

    BusinessDeclare endTx();

    ExecuteResult getExecuteResult();

    BusinessDeclare build(String name);

    BusinessDeclare execute();

    BusinessDeclare onSuccess(FinalFun fun);

    BusinessDeclare onError(FinalFun fun);

    BusinessDeclare onStop(FinalFun fun);

    BusinessDeclare onException(FinalFun fun);

    BusinessDeclare onFinsh(FinalFun fun);

    BusinessDeclare data(String data);

    BusinessDeclare data(String system, String data);

    //BusinessDeclare transaction(TransactionPolicy transactionPolicy, RollBackPolicy rollBackPolicy);

    //BusinessDeclare transaction(TransactionPolicy transactionPolicy, RollBackPolicy rollBackPolicy, String group);

    BusinessDeclare addProduce(Produce<DataStorage> produce);

    BusinessDeclare addProduce(String system, String dataName, Function<ExecuteResult, DataStorage> fun);

    BusinessDeclare addProduce(String dataName, Function<ExecuteResult, DataStorage> fun);

    BusinessDeclare transactionManager(DataSourceManager dataSourceManager);


}
