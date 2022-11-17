package dec.expand.declare.service;

import java.util.List;

import dec.core.model.container.ModelLoader;
import dec.expand.declare.ModelLoaderFun;
import dec.expand.declare.conext.DataStorage;
import dec.expand.declare.conext.desc.conumer.ConumerDesc;
import dec.expand.declare.conext.desc.subscribe.SubscriberDesc;
import dec.expand.declare.executer.conume.Conumer;
import dec.expand.declare.executer.subscribe.Subscriber;
import dec.expand.declare.fun.FinalFun;
import dec.expand.declare.fun.Function;

public interface ServiceDeclare {

	public String getName();
	
	/**
	 * 添加处理数据
	 * @param t
	 * @param clz
	 * @return
	 */
	public <T> ServiceDeclare addEntitys(String type, List<T> t);
	
	public <T> ServiceDeclare addEntity(String type, T t);
	
	public ServiceDeclare load(ModelLoader modelLoader);
	
	public ServiceDeclare load(ModelLoaderFun fun);
	
	public ServiceDeclare declare(ServiceDeclare serviceDeclare);
	
	public ServiceDeclare execute();
	
	public <T> ServiceDeclare subscribe(String name, Function<ExecuteResult, DataStorage> fun);
	
	public <T> ServiceDeclare subscribe(String name);
	
	public ServiceDeclare subscribe(String name, String dataTypes[]);
	
	public ServiceDeclare subscribe(Subscriber<?> subscribe);
	
	public ServiceDeclare subscribe(SubscriberDesc subscriberDesc);
	
	public ServiceDeclare onSuccess(FinalFun fun);
	
	public ServiceDeclare onError(FinalFun fun);
	
	public ServiceDeclare onException(FinalFun fun);
	
	public ServiceDeclare onErrorWithStop(FinalFun fun);
	
	public ServiceDeclare onExceptionWithStop(FinalFun fun);
	
	public ServiceDeclare onStop(FinalFun fun);
	
	public ServiceDeclare onFinsh(FinalFun fun);
	
	public ExecuteResult getExecuteResult();
	
	public ServiceDeclare consume(String name, Function<ExecuteResult,DataStorage> fun);
	
	public ServiceDeclare consume(Conumer<?> conumer);
	
	public <T> ServiceDeclare consume(String name, Class<T> clz, Function<ExecuteResult,T> fun);
	
	public ServiceDeclare consume(ConumerDesc conumerDesc);
}
