package dec.expand.declare.executer.subscribe;

import dec.expand.declare.executer.Executer;
import dec.expand.declare.fun.Function;
import dec.expand.declare.service.ExecuteResult;

public class Subscriber<T> extends Executer<T>{

	private String name;
	
	private Function<ExecuteResult,T> fun;
	
	private String[] dataTypes;
	
	public Subscriber(){
		
	}
	
	public Subscriber(String name){
		this(name, null);
	}
	
	public Subscriber(String name, Function<ExecuteResult,T> fun){
		this.name = name;
		this.fun = fun;
	}
	
	public ExecuteResult execute(T t) throws Exception{
		return fun.execute(t);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Function<ExecuteResult, T> getFun() {
		return fun;
	}

	public void setFun(Function<ExecuteResult, T> fun) {
		this.fun = fun;
	}

	public String[] getDataTypes() {
		return dataTypes;
	}

	public void setDataTypes(String[] dataTyps) {
		this.dataTypes = dataTyps;
	}
	
	
}
