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

	String getName();
	
	/**
	 * 添加处理数据
	 * @param t
	 * @param clz
	 * @return
	 */
	<T> ServiceDeclare addEntitys(String type, List<T> t);
	
	<T> ServiceDeclare addEntity(String type, T t);
	
	ServiceDeclare load(ModelLoader modelLoader);
	
	ServiceDeclare load(ModelLoaderFun fun);
	
	ServiceDeclare declare(ServiceDeclare serviceDeclare);
	
	ServiceDeclare execute();
	
	ServiceDeclare subscribe(String name, Function<ExecuteResult, DataStorage> fun);
	
	ServiceDeclare subscribe(String name);
	
	ServiceDeclare subscribe(String name, String dataTypes[]);
	
	ServiceDeclare subscribe(Subscriber<?> subscribe);
	
	ServiceDeclare subscribe(SubscriberDesc subscriberDesc);
	
	ServiceDeclare onSuccess(FinalFun fun);
	
	ServiceDeclare onError(FinalFun fun);
	
	ServiceDeclare onException(FinalFun fun);
	
	ServiceDeclare onErrorWithStop(FinalFun fun);
	
	ServiceDeclare onExceptionWithStop(FinalFun fun);
	
	ServiceDeclare onStop(FinalFun fun);
	
	ServiceDeclare onFinsh(FinalFun fun);
	
	ExecuteResult getExecuteResult();
	
	ServiceDeclare consume(String name, Function<ExecuteResult,DataStorage> fun);
	
	ServiceDeclare consume(Conumer<?> conumer);
	
	<T> ServiceDeclare consume(String name, Class<T> clz, Function<ExecuteResult,T> fun);
	
	ServiceDeclare consume(ConumerDesc conumerDesc);
}
