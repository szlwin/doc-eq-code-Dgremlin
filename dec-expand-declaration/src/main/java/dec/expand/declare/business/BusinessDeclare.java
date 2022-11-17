package dec.expand.declare.business;

import java.util.List;

import dec.expand.declare.conext.DataStorage;
import dec.expand.declare.conext.desc.process.RollBackPolicy;
import dec.expand.declare.conext.desc.process.TransactionPolicy;
import dec.expand.declare.executer.produce.Produce;
import dec.expand.declare.fun.FinalFun;
import dec.expand.declare.fun.Function;
import dec.expand.declare.service.ExecuteResult;

public interface BusinessDeclare {

	public String getName();
	
	/**
	 * 添加处理数据
	 * @param t
	 * @param clz
	 * @return
	 */
	public <T> BusinessDeclare addEntitys(String type, List<T> t);
	
	public <T> BusinessDeclare addEntity(String type, T t);
	
	public BusinessDeclare build(String name);
	
	public BusinessDeclare execute();
	
	public BusinessDeclare onSuccess(FinalFun fun);
	
	public BusinessDeclare onError(FinalFun fun);
	
	public BusinessDeclare onStop(FinalFun fun);
	
	public BusinessDeclare onException(FinalFun fun);
	
	public BusinessDeclare onFinsh(FinalFun fun);
	
	public BusinessDeclare data(String data);
	
	public BusinessDeclare data(String data, String system);
	
	public BusinessDeclare transaction(TransactionPolicy transactionPolicy, RollBackPolicy rollBackPolicy);
	
	public BusinessDeclare transaction(TransactionPolicy transactionPolicy, RollBackPolicy rollBackPolicy, String group);
	
	public ExecuteResult getExecuteResult();

	BusinessDeclare addProduce(Produce<DataStorage> produce);

	BusinessDeclare addProduce(String name, Function<ExecuteResult, DataStorage> fun);

	


	
}
