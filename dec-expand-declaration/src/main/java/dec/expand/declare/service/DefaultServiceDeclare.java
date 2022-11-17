package dec.expand.declare.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dec.core.model.container.ModelContainer;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.manager.ContainerManager;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import dec.expand.declare.ModelLoaderFun;
import dec.expand.declare.conext.DataStorage;
import dec.expand.declare.conext.desc.conumer.ConumerDesc;
import dec.expand.declare.conext.desc.subscribe.SubscriberDesc;
import dec.expand.declare.executer.conume.Conumer;
import dec.expand.declare.executer.subscribe.Subscriber;
import dec.expand.declare.fun.FinalFun;
import dec.expand.declare.fun.Function;

public class DefaultServiceDeclare implements ServiceDeclare {

	private String name;

	private DataStorage dataStorage = new DataStorage();

	private Map<String, Object> processMap = new HashMap<>();

	private Map<String, List<ConumerDesc>> conumerMap = new HashMap<>();
	
	private List<Object> processList = new ArrayList<>();

	private Object currentProcessObject;
	
	private FinalFun onSuccess;

	private FinalFun onError;

	private FinalFun onException;

	private FinalFun onStop;

	public FinalFun onFinsh;

	private ExecuteResult executeResult;

	private boolean stopWithError = false;
	
	private boolean stopWithException = false;
	
	private boolean isStop = false;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public <T> ServiceDeclare addEntitys(String type, List<T> t) {
		dataStorage.add(type, t);
		return this;
	}

	@Override
	public <T> ServiceDeclare addEntity(String type, T t) {
		dataStorage.add(type, t);
		return this;
	}

	@Override
	public ServiceDeclare load(ModelLoader modelLoader) {
		processList.add(modelLoader);
		return this;
	}

	@Override
	public ServiceDeclare load(ModelLoaderFun fun) {
		processList.add(fun.create());
		return this;
	}

	@Override
	public ServiceDeclare declare(ServiceDeclare serviceDeclare) {
		processList.add(serviceDeclare);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ServiceDeclare execute() {

		ModelContainer container = ContainerManager.getCurrentModelContainer();

		boolean isStart = false;

		ExecuteResult executeResult = null;
		
		Exception ex = null;
		
		doBeforeExecute();
		
		for (Object processObject : processList) {
			
			//this.dataStorage.setDataTypes(null);
			
			currentProcessObject = processObject;
			
			if (processObject instanceof ModelLoader) {

				if (!isStart) {
					isStart = true;
				}

				container.load((ModelLoader) processObject);

			} else {

				if (isStart) {
					try {
						
						container.execute().getResult();
						
						
					} catch (ExecuteRuleException e) {
						
						ex = e;
						
					} finally {
						isStart = false;
					}
				}

				if (processObject instanceof ServiceDeclare) {

					ServiceDeclare serviceDeclare = (ServiceDeclare) processObject;

					try {
						serviceDeclare.execute();
						
						executeResult = serviceDeclare.getExecuteResult();
						
						executeResult.setProcessName(serviceDeclare.getName());
						
					} catch (Exception e) {
						ex = e;
					}
					
				} else if (processObject instanceof SubscriberDesc) {

					
					try {
						SubscriberDesc subscriberDesc = (SubscriberDesc) processObject;
						
						Subscriber<Object> subscriber =null; //(Subscriber<Object>) subscriberDesc.getExecuter();
						
						if(subscriber.getFun() != null){
							executeResult = subscriber.execute(dataStorage);
							
							executeResult.setProcessName(subscriber.getName());
						}else{
							executeResult = ExecuteResult.success();
						}
						
						
						
					} catch (Exception e) {
						ex = e;
					}
				}
				
				executeResult.setDeclareName(this.getName());
			}
			
			handleProcess(executeResult, ex);
			
			if(isStop){
				break;
			}
		}
		
		doWithAfterExecute();
		
		return this;
	}
	
	protected void handleProcess(ExecuteResult executeResult, Exception  ex){
		
		if(ex != null){
			Error error = new Error();
			
			error.setException(ex);
			
			executeResult.setError(error);
		}

		handleExecute(executeResult);
		
	}
	
	protected void handleException(Exception  ex){
		
		Error error = new Error();
		
		error.setException(ex);
		
		this.executeResult.setError(error);
		
		if(this.onException != null){
			this.onException.execute(executeResult);
		}
		
		if(stopWithException){
			
			handleStop();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void handleExecute(ExecuteResult executeResult){
		
		this.executeResult = executeResult;
		
		if(!executeResult.isSuccess()){
			
			if(executeResult.getError().getException() != null){
				if(this.onException != null){
					this.onException.execute(executeResult);
				}
				
				if(stopWithException){
					
					handleStop();
				}
			}else{
				if(this.onError != null){
					this.onError.execute(null);
				}
				
				if(stopWithError){
					handleStop();
				}
			}
			
			this.executeResult.setData(this.dataStorage);
		}else{
			if(executeResult.getData() != null){
				if(executeResult.getDataType() != null){
					dataStorage.add(executeResult.getDataType(), executeResult.getData());
				}else if(executeResult.getProcessName() != null){
					dataStorage.add(executeResult.getProcessName(), executeResult.getData());
				}
			}
			
			String[] dataTypes = null;
			
			//if(currentProcessObject instanceof SubscriberDesc){
			//	dataTypes = ((Subscriber<?>) ((SubscriberDesc)currentProcessObject)
			//			.getExecuter()).getDataTypes();
			//}
			
			if(conumerMap.containsKey(executeResult.getProcessName())){
				List<ConumerDesc> conumers = conumerMap.get(executeResult.getProcessName());
				
				for(ConumerDesc conumerDesc:conumers){
					Conumer conumer = (Conumer) conumerDesc.getExecuter();
					if(!conumer.isGetAllData()){
						
						//dataStorage.setDataTypes(dataTypes);
						Object data = this.dataStorage.get(conumer.getName());
						
						conumer.setDataTypes(dataTypes);
						
						try {
							conumer.execute(data);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						try {
							conumer.execute(this.dataStorage);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				
			}
		}
	}
	
	protected void doBeforeExecute(){
		
	}
	
	protected void doAfterExecute(){
		
	}
	
	protected void doWithAfterExecute(){
		
		doAfterExecute();
		executeResult.setData(this.dataStorage);
		if(executeResult.isSuccess()){
			
			if(this.onSuccess != null){
				this.onSuccess.execute(executeResult);
			}
		}
		
		if(onFinsh != null){
			this.onFinsh.execute(executeResult);
		}
		
	}
	
	protected void handleStop(){
		this.isStop = true;
		
		if(this.onStop != null){
			this.onStop.execute(executeResult);
		}
	}
	
	@Override
	public <T> ServiceDeclare subscribe(String name, Function<ExecuteResult,DataStorage> fun) {

		Subscriber<?> subscriber = new Subscriber<>(name, fun);
		
		this.subscribe(subscriber);
		
		return this;
	}
	
	@Override
	public ServiceDeclare onSuccess(FinalFun fun) {
		this.onSuccess = fun;
		return this;
	}

	@Override
	public ServiceDeclare onError(FinalFun fun) {
		this.onError = fun;
		return this;
	}

	@Override
	public ServiceDeclare onErrorWithStop(FinalFun fun) {
		this.onError = fun;
		return this;
	}

	@Override
	public ServiceDeclare onException(FinalFun fun) {
		this.onException = fun;
		return this;
	}

	@Override
	public ServiceDeclare onExceptionWithStop(FinalFun fun) {
		this.onException = fun;
		return this;
	}

	@Override
	public ServiceDeclare onStop(FinalFun fun) {
		this.onStop = fun;
		return this;
	}

	@Override
	public ServiceDeclare onFinsh(FinalFun fun) {
		this.onFinsh = fun;
		return this;
	}

	@Override
	public ExecuteResult getExecuteResult() {
		return executeResult;
	}

	protected Object getData(String type) {
		return dataStorage.get(type);
	}

	@Override
	public ServiceDeclare consume(String name, Function<ExecuteResult, DataStorage> fun) {
		
		Conumer<DataStorage> conumer = new Conumer<DataStorage>(name, fun);
		
		conumer.setGetAllData(true);
		
		consume(conumer);
		
		return this;
	}

	@Override
	public <T> ServiceDeclare subscribe(String name) {
		Subscriber<?> subscriber = new Subscriber<>(name);
		
		this.subscribe(subscriber);
		return this;
	}

	//@Override
	public <T> ServiceDeclare consume(String name, Class<T> clz, Function<ExecuteResult, T> fun) {
		Conumer<T> conumer = new Conumer<T>(name, fun);

		conumer.setGetAllData(false);
		
		consume(conumer);
		
		return this;
	}

	@Override
	public ServiceDeclare consume(Conumer<?> conumer) {
		
		ConumerDesc conumerDesc = new ConumerDesc();
		
		conumerDesc.setName(conumer.getName());
		conumerDesc.setExecuter(conumer);
		
		this.consume(conumerDesc);
		
		return this;
	}

	@Override
	public ServiceDeclare subscribe(Subscriber<?> subscriber) {
		
		SubscriberDesc  subscriberDesc = new SubscriberDesc();
		
		//subscriberDesc.setExecuter(subscriber);
		subscriberDesc.setName(subscriberDesc.getName());
		
		subscribe(subscriberDesc);
		
		return this;
	}

	@Override
	public ServiceDeclare subscribe(String name, String[] dataTypes) {
		
		Subscriber<?> subscriber = new Subscriber<>(name);

		subscriber.setDataTypes(dataTypes);
		
		this.subscribe(subscriber);
		return this;
	}

	@Override
	public ServiceDeclare subscribe(SubscriberDesc subscriberDesc) {
		
		processMap.put(subscriberDesc.getName(), subscriberDesc);
		processList.add(subscriberDesc);
		
		return this;
	}

	@Override
	public ServiceDeclare consume(ConumerDesc conumerDesc) {
		
		List<ConumerDesc> list = conumerMap.getOrDefault(conumerDesc.getName(), new ArrayList<>(4));
		
		list.add(conumerDesc);

		if(!conumerMap.containsKey(conumerDesc.getName())){
			conumerMap.put(conumerDesc.getName(), list);
		}
		
		return this;
	}


}
